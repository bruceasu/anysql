package com.asql.tools;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.DBConnection;
import com.asql.core.DefaultSQLExecutor;
import com.asql.mysql.MySQLSQLExecutor;
import com.asql.oracle.OracleSQLExecutor;
import com.asql.sybase.SybaseSQLExecutor;
import java.io.IOException;

public class ASQL {
    public static void main(String[] paramArrayOfString)
            throws IOException {
        DBConnection.setLocale("ENGLISH");
        DefaultSQLExecutor sqlExecutor = null;
        String database = "ORACLE";
        String userId = null;
        String file = null;
        for (int i = 0; i < paramArrayOfString.length; i++)
            if (paramArrayOfString[i].toUpperCase().startsWith("--")) {
                database = paramArrayOfString[i].substring(2);
            } else if (paramArrayOfString[i].toUpperCase().startsWith("USERID=")) {
                userId = paramArrayOfString[i].substring(7);
            } else {
                if (!paramArrayOfString[i].toUpperCase().startsWith("START="))
                    continue;
                file = paramArrayOfString[i].substring(6);
            }
        if ("ORACLE".equalsIgnoreCase(database)) {
            sqlExecutor = new OracleSQLExecutor();
        } else if ("SYBASE".equalsIgnoreCase(database)) {
            sqlExecutor = new SybaseSQLExecutor();
        } else if ("MSSQL".equalsIgnoreCase(database)) {
            sqlExecutor = new SybaseSQLExecutor();
        } else if ("MYSQL".equalsIgnoreCase(database)) {
            sqlExecutor = new MySQLSQLExecutor();
        } else {
            sqlExecutor = new OracleSQLExecutor();
        }
        if ((userId != null) && (userId.length() > 0)) {
            String cmd = "CONNECT " + userId;
            Command localCommand = new Command(CMDType.ASQL_SINGLE, CMDType.ASQL_SINGLE, cmd);
            sqlExecutor.run(localCommand);
        }
        if ((file != null) && (file.length() > 0)) {
            String cmd = "@ " + file;
            Command localCommand = new Command(CMDType.ASQL_SQLFILE, CMDType.ASQL_SQLFILE, cmd);
            sqlExecutor.run(localCommand);
        } else {
            sqlExecutor.run();
        }
        if (sqlExecutor.getCommandLog().getLogFile() != null)
            sqlExecutor.getCommandLog().getLogFile().close();
        sqlExecutor.getCommandLog().setLogFile(null);
    }
}
