package com.asql.tools;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.DBConnection;
import com.asql.core.DefaultSQLExecutor;
import com.asql.core.log.CommandLog;
import com.asql.mysql.MySqlSQLExecutor;
import com.asql.oracle.OracleSQLExecutor;
import com.asql.sybase.SybaseSQLExecutor;
import java.io.IOException;

public class ASQL {
    public static void main(String[] args)
            throws IOException {

        /*proxy*/
        /*socks5*/
        System.getProperties().setProperty("socksProxyHost", "localhost");
        System.getProperties().setProperty("socksProxyPort", "1080");
        /*http*/
//        System.getProperties().setProperty("http.proxyHost", networkBean.getAddress());
//        System.getProperties().setProperty("http.proxyPort", networkBean.getPort());

        DBConnection.setLocale("ENGLISH");
        DefaultSQLExecutor sqlExecutor = null;
        String database = "ORACLE";
        String userId = null;
        String file = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].toUpperCase().startsWith("--")) {
                database = args[i].substring(2);
            } else if (args[i].toUpperCase().startsWith("USERID=")) {
                userId = args[i].substring(7);
            } else if (args[i].toUpperCase().startsWith("START=")) {
                file = args[i].substring(6);

            }
        }

        sqlExecutor = createSQLExecutor(database);

        if ((userId != null) && (userId.length() > 0)) {
            String cmd = "CONNECT " + userId;
            Command localCommand = new Command(CMDType.ASQL_SINGLE, CMDType.ASQL_SINGLE, cmd);
            sqlExecutor.run(localCommand);
        }

        if ((file != null) && (file.length() > 0)) {
            String cmd = "@ " + file;
            Command localCommand = new Command(CMDType.ASQL_SQL_FILE, CMDType.ASQL_SQL_FILE, cmd);
            sqlExecutor.run(localCommand);
        } else {
            sqlExecutor.run();
        }

        CommandLog commandLog = sqlExecutor.getCommandLog();
        CommandLog logFile = commandLog.getLogFile();
        if (logFile != null) {
            logFile.close();
        }

        commandLog.setLogFile(null);
    }

    private static DefaultSQLExecutor createSQLExecutor(String database)
    {
        DefaultSQLExecutor sqlExecutor;
        if ("ORACLE".equalsIgnoreCase(database)) {
            sqlExecutor = new OracleSQLExecutor();
        } else if ("SYBASE".equalsIgnoreCase(database)) {
            sqlExecutor = new SybaseSQLExecutor();
        } else if ("MSSQL".equalsIgnoreCase(database)) {
            sqlExecutor = new SybaseSQLExecutor();
        } else if ("MYSQL".equalsIgnoreCase(database)) {
            sqlExecutor = new MySqlSQLExecutor();
        } else {
            sqlExecutor = new OracleSQLExecutor();
        }
        return sqlExecutor;
    }
}
