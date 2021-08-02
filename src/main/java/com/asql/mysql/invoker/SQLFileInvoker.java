package com.asql.mysql.invoker;

import static com.asql.pgsql.PgSqlCMDType.PGSQL_SQLFILE_0;
import static com.asql.pgsql.PgSqlCMDType.PGSQL_SQLFILE_1;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.ModuleInvoker;
import com.asql.core.io.InputCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVm;
import com.asql.core.util.TextUtils;
import com.asql.mysql.MySqlSQLExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author suk
 * @date 2017/8/13
 */
public class SQLFileInvoker implements ModuleInvoker {

    MySqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;

    public SQLFileInvoker(MySqlSQLExecutor executor) {
        this.executor = executor;
        out           = executor.getCommandLog();
        cmdType       = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
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
        int    i    = TextUtils.getWords(cmdType.getSQLFile()[PGSQL_SQLFILE_0]).size(); // LOADTNS
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: @[@] file");
            return false;
        }
        String path = executor.sysVariable.parseString(str1.trim());
        if ((workDir != null) && (workDir.length() > 0)) {
            path = workDir + JavaVm.FILE_SEPERATOR + path;
        }
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
