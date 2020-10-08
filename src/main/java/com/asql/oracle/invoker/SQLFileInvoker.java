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

package com.asql.oracle.invoker;

import static com.asql.oracle.OracleCMDType.ORACLE_LOADTNS;
import static com.asql.oracle.OracleCMDType.ORACLE_SQLFILE_0;
import static com.asql.oracle.OracleCMDType.ORACLE_SQLFILE_1;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.ModuleInvoker;
import com.asql.core.io.InputCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVM;
import com.asql.core.util.TextUtils;
import com.asql.oracle.OracleSQLExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author suk
 * @date 2017/8/13
 */
public class SQLFileInvoker implements ModuleInvoker {
    OracleSQLExecutor executor;
    CommandLog out;
    CMDType cmdType;
    public SQLFileInvoker(OracleSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCommandType();
    }

    @Override
    public boolean invoke(Command cmd){
            int i = cmdType.startsWith(cmdType.getSQLFile(), cmd.COMMAND);
            switch (i) {
                case ORACLE_SQLFILE_0:
                    return procRun2("@@ " + cmd.COMMAND.trim().substring(2), cmd.WORKING_DIR);
                case ORACLE_SQLFILE_1:
                    return executor.procRun1("@ " + cmd.COMMAND.trim().substring(1));
            }
            return false;
        }

    boolean procRun2(String cmdLine, String workDir) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ORACLE_LOADTNS]).size(); // LOADTNS
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: @[@] file");
            return false;
        }
        String path = executor.sysVariable.parseString(str1.trim());
        if ((workDir != null) && (workDir.length() > 0))
            path = workDir + JavaVM.FILE_SEPERATOR + path;
        File localFile = new File(path);
        try (FileInputStream stream = new FileInputStream(localFile);
             InputCommandReader reader = new InputCommandReader(stream)) {
            reader.setWorkingDir(localFile.getParent());
            Command localCommand = executor.run(reader);
            reader.close();
            return localCommand.COMMAND != null;
        } catch (IOException localIOException) {
            out.print(localIOException);
            return false;
        }
    }
}
