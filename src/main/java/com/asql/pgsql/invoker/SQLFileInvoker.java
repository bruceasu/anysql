/*
 * Copyright (C) 2017 Bruce Asu<bruceasu@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *  　　
 * 　　The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.asql.pgsql.invoker;

import static com.asql.pgsql.PgSqlCMDType.*;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.ModuleInvoker;
import com.asql.core.io.InputCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVm;
import com.asql.core.util.TextUtils;
import com.asql.pgsql.PgSqlSQLExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author suk
 * @date 2017/8/13
 */
public class SQLFileInvoker implements ModuleInvoker {
    PgSqlSQLExecutor executor;
    CommandLog       out;
    CMDType cmdType;
    public SQLFileInvoker(PgSqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd){
            int i = cmdType.startsWith(cmdType.getSQLFile(), cmd.command);
            switch (i) {
                case PGSQL_SQLFILE_0:
                    return procRun2("@@ " + cmd.command.trim().substring(2), cmd.workingDir);
                case PGSQL_SQLFILE_1:
                    return procRun1("@ " + cmd.command.trim().substring(1));
            }
            return false;
        }


    boolean procRun1(String cmdLine) {
        int    i    = TextUtils.getWords(cmdType.getSQLFile()[PGSQL_SQLFILE_1]).size();
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: @[@] file");
            return false;
        }
        String path = executor.sysVariable.parseString(str1.trim());
        File   file = new File(path);
        try (FileInputStream stream = new FileInputStream(file);
             InputCommandReader reader = new InputCommandReader(stream)) {
            reader.setWorkingDir(file.getParent());
            Command localCommand = executor.run(reader);
            reader.close();
            return localCommand.command != null;
        } catch (IOException localIOException) {
            out.print(localIOException);
        }
        return false;
    }

    boolean procRun2(String cmdLine, String workDir) {
        int i = TextUtils.getWords(cmdType.getSQLFile()[PGSQL_SQLFILE_0]).size(); // LOADTNS
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: @[@] file");
            return false;
        }
        String path = executor.sysVariable.parseString(str1.trim());
        if ((workDir != null) && (workDir.length() > 0))
            path = workDir + JavaVm.FILE_SEPERATOR + path;
        File localFile = new File(path);
        try (FileInputStream stream = new FileInputStream(localFile);
             InputCommandReader reader = new InputCommandReader(stream)) {
            reader.setWorkingDir(localFile.getParent());
            Command localCommand = executor.run(reader);
            reader.close();
            return localCommand.command != null;
        } catch (IOException localIOException) {
            out.print(localIOException);
            return false;
        }
    }
}
