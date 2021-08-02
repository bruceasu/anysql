package com.asql.mysql.invoker;

import static com.asql.core.CommandExecutor.getInt;
import static com.asql.mysql.MySqlCMDType.*;
import static com.asql.mysql.MySqlCMDType.ASQL_SINGLE_AUTOT;
import static com.asql.mysql.MySqlCMDType.ASQL_SINGLE_AUTOTRACE;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.core.log.OutputCommandLog;
import com.asql.core.util.TextUtils;
import com.asql.mysql.MySqlSQLExecutor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

public class SingleInvoker implements ModuleInvoker
{
    MySqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;

    public SingleInvoker(MySqlSQLExecutor executor)
    {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();

    }

    @Override
    public boolean invoke(Command cmd)
    {
        int k = executor.getSingleID(cmd.command);
        switch (k) {
        case ASQL_SINGLE_CONNECT:
        case ASQL_SINGLE_CONN:
            procConnect(cmd.command);
            break;
        case ASQL_SINGLE_DISCONNECT:
            executor.procDisconnect(cmd.command);
            break;
        case ASQL_SINGLE_DEBUGLEVEL:
            procDebugLevel(cmd.command);
            break;
        case ASQL_SINGLE_PAGESIZE:
            procPageSize(cmd.command);
            break;
        case ASQL_SINGLE_HEADING:
            procHeading(cmd.command);
            break;
        case ASQL_SINGLE_DELIMITER:
            procDelimiter(cmd.command);
            break;
        case ASQL_SINGLE_VAR:
            procVariable(cmd.command);
            break;
        case ASQL_SINGLE_UNVAR:
            procUnVariable(cmd.command);
            break;
        case ASQL_SINGLE_PRINT:
            procPrint(cmd.command);
            break;
        case ASQL_SINGLE_TIMING:
            procTiming(cmd.command);
            break;
        case ASQL_SINGLE_DEFINE:
            procDefine(cmd.command);
            break;
        case ASQL_SINGLE_AUTOCOMMIT:
            procAutocommit(cmd.command);
            break;
        case ASQL_SINGLE_SQLSET:
            procSQLSet(cmd.command);
            break;
        case ASQL_SINGLE_SPOOLAPPEND:
            procSpoolAppend(cmd.command);
            break;
        case ASQL_SINGLE_SPOOL:
            procSpool(cmd.command);
            break;
        case ASQL_SINGLE_READ:
            procRead(cmd.command);
            break;
        case ASQL_SINGLE_HELP:
            procHelp(cmd.command);
            break;
        case ASQL_SINGLE_HOST:
            procHost(cmd.command);
            break;
        case ASQL_SINGLE_USE:
            procUse(cmd.command);
            break;
        case ASQL_SINGLE_SPOOLOFF:
            procSpoolOff(cmd.command);
            break;
        case ASQL_SINGLE_AUTOTRACE:
            procAutoTrace(cmd.command);
            break;
        case ASQL_SINGLE_AUTOT:
            break;
        case ASQL_SINGLE_QUERYONLY:
            procQueryOnly(cmd.command);
            break;
        default:
        }

        return true;
    }

    private void procQueryOnly(String cmdLine) {
        String c = cmdType.getASQLSingle()[ASQL_SINGLE_QUERYONLY];
        int i = TextUtils.getWords(c).size();
        String flag = executor.skipWord(cmdLine, i);
        flag = flag.trim();
        if (flag.length() == 0) {
            if (cmdType != null) {
                out.println("QUERYONLY : " + (cmdType.getQueryOnly() ? "TRUE" : "FALSE"));
            }
            return;
        }
        executor.autoCommit = "TRUE".equalsIgnoreCase(flag);
        if (cmdType != null) {
            cmdType.setQueryOnly(executor.autoCommit);
        }
    }

    private void procConnect(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[0]).size();
        String params = executor.skipWord(cmdLine, i);
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(params));
        String host = null;
        String username = null;
        String password = null;
        if (arr.length > 0) {
            host = arr[0];
            if (arr.length > 1) {
                username = arr[1];
            }
            if (arr.length > 2) {
                password = arr[2];
                if ("-p".equals(password)) {
                    try {
                        password = executor.getCommandReader().readPassword();
                    } catch (Exception e) {
                        out.println("Read password fail.");
                        return;
                    }
                }
            }
        } else {
            out.println("Usage:");
            out.println("  CONNECT host:port/db username password");
            return;
        }
        
        try {
            executor.disconnect();
            executor.database = DBConnection.getConnection("MYSQL", host, username, password);
            executor.database.setAutoCommit(false);
            executor.autoCommit = executor.database.getAutoCommit();
            out.println("Database connected.");
        } catch (SQLException e) {
            out.print(e);
        }
    }

    private void procDebugLevel(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_DEBUGLEVEL]).size();
        String str = executor.skipWord(cmdLine, i);
        str = str.trim();
        int j = getInt(str, 0);
        executor.setDebugLevel(j);
        out.println("Debug level set to : " + executor.getDebugLevel());
    }

    private void procPageSize(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_PAGESIZE]).size();
        String str = executor.skipWord(cmdLine, i);
        str = str.trim();
        int j = getInt(str, 14);
        out.setPagesize(j);
        out.println("Page size set to : " + out.getPagesize());
    }

    private void procTiming(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_TIMING]).size();
        String str = executor.skipWord(cmdLine, i);
        str = str.trim();
        executor.timing = "ON".equalsIgnoreCase(str);
        out.println("Timing set to : " + (executor.timing ? "ON" : "OFF"));
    }

    private void procSpoolAppend(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_SPOOLAPPEND]).size();
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = executor.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream outputStream = new FileOutputStream(str2, true);
            if (out.getLogFile() != null) {
                out.getLogFile().close();
            }
            out.setLogFile(new OutputCommandLog(executor, outputStream));
        } catch (IOException e) {
            out.print(e);
        }
    }

    private void procSpool(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_SPOOL]).size();
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = executor.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream outputStream = new FileOutputStream(str2);
            if (out.getLogFile() != null) {
                out.getLogFile().close();
            }
            out.setLogFile(new OutputCommandLog(executor, outputStream));
        } catch (IOException e) {
            out.print(e);
        }
    }

    private void procRead(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_READ]).size();
        String param = executor.skipWord(cmdLine, i);
        param = param.trim();
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(param));
        if (arr.length < 2) {
            out.println("Usage: READ varname filename");
            return;
        }
        String varName = arr[0].trim();
        String value = param.substring(varName.length()).trim();
        if (!executor.sysVariable.exists(varName)) {
            out.println("Variable " + varName + " not defined!");
            return;
        }
        try {
            FileReader reader = new FileReader(executor.sysVariable.parseString(value));
            char[] arrayOfChar = new char[65536];
            int j = reader.read(arrayOfChar);
            try {
                executor.sysVariable.setValue(varName, String.valueOf(arrayOfChar, 0, j));
            } catch (Exception e) {
                out.print(e);
            }
            reader.close();
        } catch (IOException e) {
            out.print(e);
        }
    }

    private void procHelp(String cmdLine)
    {
        out.println("Usage: HELP");
        out.println();
        out.println(" CONNECT SHOW SET DESCRIBE VAR UNVAR PRINT DEFINE SQLSET SPOOL READ");
        out.println(" @ @@ LOB LOBEXP");
    }

    private void procHost(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_HOST]).size();
        String param = executor.skipWord(cmdLine, i);
        param = executor.sysVariable.parseString(param.trim());
        if (param.length() > 0) {
            try {
                executor.host(param);
            } catch (IOException e) {
                out.print(e);
            }
        }
    }

    private void procAutocommit(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_AUTOCOMMIT]).size();
        String flag = executor.skipWord(cmdLine, i);
        flag = flag.trim();
        if (executor.checkNotConnected()) {
            return;
        }
        if (flag.length() == 0) {
            try {
                executor.autoCommit = executor.database.getAutoCommit();
                out.println("Autocommit : " + (executor.autoCommit ? "ON" : "OFF"));
            } catch (SQLException e) {
                out.print(e);
            }
            return;
        }
        executor.autoCommit = "ON".equalsIgnoreCase(flag);
        try {
            executor.database.setAutoCommit(executor.autoCommit);
            executor.autoCommit = executor.database.getAutoCommit();
        } catch (SQLException e) {
            out.print(e);
        }
    }

    private void procUse(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[21]).size();
        String str = executor.skipWord(cmdLine, i);
        if (executor.checkNotConnected()) {
            return;
        }
        if (str.trim().length() == 0) {
            out.println("Usage: USE dbname");
            return;
        }
        try {
            executor.database.setCatalog(str.trim());
        } catch (SQLException e) {
            out.print(e);
        }
    }

    private void procSpoolOff(String cmdLine)
    {
        if (out.getLogFile() != null) {
            out.getLogFile().close();
        }
        out.setLogFile(null);
    }

    private void procDefine(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_DEFINE]).size();
        String str1 = executor.skipWord(cmdLine, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            out.println("Usage:");
            out.println("  DEFINE variable=value");
            out.println("  SET    PAGESIZE   pagesize");
            out.println("  SET    DEBUGLEVEL debuglevel");
            out.println("  SET    HEADING    {OFF|ON}");
            out.println("  SET    DELIMITER  delimiter");
            out.println("  SET    TIMING     {ON|OFF}");
            return;
        }
        String varName = str1.substring(0, j).trim();
        String value = str1.substring(j + 1);
        if (varName.trim().length() == 0) {
            out.println("Usage:");
            out.println("  DEFINE variable=value");
            out.println("  SET    PAGESIZE   pagesize");
            out.println("  SET    DEBUGLEVEL debuglevel");
            out.println("  SET    HEADING    {OFF|ON}");
            out.println("  SET    DELIMITER  delimiter");
            out.println("  SET    TIMING     {ON|OFF}");
            return;
        }

        if (!executor.sysVariable.exists(varName)) {
            out.println("Variable " + varName + " not declared.");
            return;
        }

        try {
            if (value.length() == 0) {
                executor.sysVariable.setValue(varName, null);
            } else {
                executor.sysVariable.setValue(varName, value);
            }
        } catch (Exception e) {
            out.print("Invalid format : ");
            out.println(value);
        }
    }

    private void procSQLSet(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_SQLSET]).size();
        String str1 = executor.skipWord(cmdLine, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            out.println("Set query result to variable.");
            out.println("Usage: ");
            out.println("       SQLSET variable=query");
            return;
        }
        String varName = str1.substring(0, j).trim();
        String sql = str1.substring(j + 1);
        if (varName.trim().length() == 0) {
            out.println("Set query result to variable.");
            out.println("Usage: ");
            out.println("       SQLSET variable=query");
            return;
        }
        if (!executor.sysVariable.exists(varName)) {
            out.println("Variable " + varName + " not declared.");
            return;
        }
        if (executor.checkNotConnected()) {
            return;
        }
        try {
            DBRowCache rowCache = executor.executeQuery(executor.database, sql, executor.sysVariable, 1000);
            if ((rowCache.getRowCount() == 1) && (rowCache.getColumnCount() == 1)) {
                try {
                    executor.sysVariable.setValue(varName, rowCache.getItem(1, 1));
                } catch (Exception localException) {
                    out.print("Invalid format : ");
                    out.println(sql);
                }
            } else {
                out.println("Query does not return one row and one column.");
            }
        } catch (SQLException e) {
            out.print(e);
        }
    }

    private void procHeading(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_HEADING]).size();
        String str = executor.skipWord(cmdLine, i);
        str = str.trim();
        boolean bool = "ON".equalsIgnoreCase(str);
        out.setHeading(bool);
        out.println("Heading set to : " + (bool ? "ON" : "OFF"));
    }

    private void procDelimiter(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_DELIMITER]).size();
        String str = executor.skipWord(cmdLine, i);
        str = str.trim();
        if ("TAB".equalsIgnoreCase(str)) {
            str = "\t";
        }
        out.setSeperator(str.length() == 0 ? " " : str);
        out.println("Delimiter set to : " + (str.length() == 0 ? "SPACE" : str));
    }

    private void procVariable(String cmdLine)
    {
        String str1 = "%";
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_VAR]).size();
        String str2 = executor.skipWord(cmdLine, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length < 2) {
            out.println("Usage:");
            out.println("  VAR varname vartype");
            out.println("");
            out.println("  CHAR VARCHAR LONGVARCHAR BINARY VARBINARY LONGVARBINARY");
            out.println("  NUMERIC DECIMAL BIT TINYINT SMALLINT INTEGER BIGINT REAL");
            out.println("  FLOAT DOUBLE DATE TIME TIMESTAMP BLOB CLOB");
            return;
        }
        if (executor.sysVariable.exists(arrayOfString[0])) {
            out.println("Variable " + arrayOfString[0] + " already defined.");
            return;
        }
        executor.sysVariable.add(arrayOfString[0], SQLTypes.getTypeID(arrayOfString[1]));
        out.println("Variable " + arrayOfString[0] + " created.");
    }

    private void procAutoTrace(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_AUTOTRACE]).size();
        String str = executor.skipWord(cmdLine, i);
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(str));
        if (arr.length == 0) {
            out.println("Usage: SET AUTOTRACE {ON | OFF}");
            return;
        }
        if ((!"ON".equalsIgnoreCase(arr[0])) && (!"OFF".equalsIgnoreCase(
                arr[0]))) {
            out.println("Usage: SET AUTOTRACE {ON | OFF}");
            return;
        }
        out.setAutoTrace(arr[0].equalsIgnoreCase("ON"));
        out.println("Autotrace set to : " + str);
    }

    private void procUnVariable(String cmdLine)
    {
        String str1 = "%";
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_UNVAR]).size();
        String param = executor.skipWord(cmdLine, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(param));
        if (arrayOfString.length == 0) {
            out.println("Usage: UNVAR varname");
            return;
        }
        String varName = arrayOfString[0];
        if (!executor.sysVariable.exists(varName)) {
            out.println("Variable " + varName + " not exist.");
            return;
        }
        executor.sysVariable.remove(varName);
        out.println("Variable " + varName + " removed.");
    }

    private void procPrint(String cmdLine)
    {
        String str1 = "%";
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ASQL_SINGLE_PRINT]).size();
        String str2 = executor.skipWord(cmdLine, i);
        out.println(executor.sysVariable.parseString(str2));
    }
}
