package com.asql.sybase;

import com.asql.core.*;
import com.asql.core.io.CommandReader;
import com.asql.core.io.InputCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.log.OutputCommandLog;
import com.asql.core.util.DateOperator;
import com.asql.core.util.JavaVM;
import com.asql.core.util.TextUtils;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPOutputStream;

public class SybaseSQLExecutor extends DefaultSQLExecutor {
    private Command lastCommand = null;
    private DBRowCache loadBuffer = new SimpleDBRowCache();
    public final int ASQL_SINGLE_MSSQL = 0;
    public final int ASQL_SINGLE_DEBUGLEVEL = 1;
    public final int ASQL_SINGLE_PAGESIZE = 2;
    public final int ASQL_SINGLE_HEADING = 3;
    public final int ASQL_SINGLE_DELIMITER = 4;
    public final int ASQL_SINGLE_AUTOTRACE = 5;
    public final int ASQL_SINGLE_TIMING = 6;
    public final int ASQL_SINGLE_DISCONNECT = 7;
    public final int ASQL_SINGLE_SYBASE = 8;
    public final int ASQL_SINGLE_VAR = 9;
    public final int ASQL_SINGLE_UNVAR = 10;
    public final int ASQL_SINGLE_PRINT = 11;
    public final int ASQL_SINGLE_DEFINE = 12;
    public final int ASQL_SINGLE_SQLSET = 13;
    public final int ASQL_SINGLE_SPOOLAPPEND = 14;
    public final int ASQL_SINGLE_SPOOLOFF = 15;
    public final int ASQL_SINGLE_SPOOL = 16;
    public final int ASQL_SINGLE_READ = 17;
    public final int ASQL_SINGLE_HELP = 18;
    public final int ASQL_SINGLE_HOST = 19;
    public final int ASQL_SINGLE_AUTOT = 20;
    public final int ASQL_SINGLE_USE = 21;
    public final int ASQL_SINGLE_AUTOCOMMIT = 22;
    public final int ASQL_SINGLE_BUFFERADD = 23;
    public final int ASQL_SINGLE_BUFFERLIST = 24;
    public final int ASQL_SINGLE_BUFFERRESET = 25;
    private final int ASQL_SINGLE_SQLFILE_1 = 0;
    private final int ASQL_SINGLE_SQLFILE_2 = 1;
    private final int ASQL_MULTIPLE_LOB = 0;
    private final int ASQL_MULTIPLE_LOBEXP = 1;
    private final int ASQL_MULTIPLE_LOBIMP = 2;
    private final int ASQL_MULTIPLE_LOBLEN = 3;
    private final int ASQL_MULTIPLE_UNLOAD = 4;
    private final int ASQL_MULTIPLE_LOAD = 5;

    public SybaseSQLExecutor() {
        super(new SybaseCMDType());
        this.cmdType.setQueryOnly(false);
        setShowComplete(true);
    }

    public SybaseSQLExecutor(CommandReader paramCommandReader, CommandLog paramCommandLog) {
        super(new SybaseCMDType(), paramCommandReader, paramCommandLog);
        this.cmdType.setQueryOnly(false);
        setShowComplete(true);
    }

    @Override
    public final boolean execute(Command paramCommand) {
        long l1, l2 = 0L;
        if (paramCommand == null) {
            this.out.println("No command to execute.");
            return true;
        }
        switch (paramCommand.TYPE1) {
            case 0:
            case 1:
            case 2:
            case 13:
                if (!isConnected()) {
                    this.out.println("Database not connected.");
                    return true;
                }
                l1 = System.currentTimeMillis();
                executeSQL(this.database, paramCommand, this.sysVariable, this.out);
                l2 = System.currentTimeMillis();
                if (this.timing)
                    this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                this.lastCommand = paramCommand;
                break;
            case 3:
                if (!isConnected()) {
                    this.out.println("Database not connected.");
                    return true;
                }
                l1 = System.currentTimeMillis();
                executeScript(this.database, paramCommand, this.sysVariable, this.out);
                l2 = System.currentTimeMillis();
                if (this.timing)
                    this.out.println(" Execute time: " + DBOperation.getElapsed(l2 - l1));
                this.lastCommand = paramCommand;
                break;
            case 7:
                execute(this.lastCommand);
                break;
            case 4:
                if (!isConnected()) {
                    this.out.println("Database not connected.");
                    return true;
                }
                l1 = System.currentTimeMillis();
                executeCall(this.database, new Command(paramCommand.TYPE1, paramCommand.TYPE2, skipWord(paramCommand.COMMAND, 1)), this.sysVariable, this.out);
                l2 = System.currentTimeMillis();
                if (!this.timing)
                    break;
                this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                break;
            case 16:
                int i = this.cmdType.startsWith(this.cmdType.getSQLFile(), paramCommand.COMMAND);
                switch (i) {
                    case ASQL_SINGLE_SQLFILE_1:
                        if (!procRun2("@@ " + paramCommand.COMMAND.trim().substring(2)))
                            break;
                        return false;
                    case ASQL_SINGLE_SQLFILE_2:
                        if (!procRun2("@ " + paramCommand.COMMAND.trim().substring(1)))
                            break;
                        return false;
                }
                break;
            case 6:
                int j = getMultipleID(paramCommand.COMMAND);
                switch (j) {
                    case ASQL_MULTIPLE_LOB:
                        l1 = System.currentTimeMillis();
                        procLOB(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                        break;
                    case ASQL_MULTIPLE_LOBEXP:
                        l1 = System.currentTimeMillis();
                        procLOBEXP(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                        break;
                    case ASQL_MULTIPLE_LOBIMP:
                        l1 = System.currentTimeMillis();
                        procLOBIMP(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                        break;
                    case ASQL_MULTIPLE_LOBLEN:
                        l1 = System.currentTimeMillis();
                        procLOBLEN(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                        break;
                    case ASQL_MULTIPLE_UNLOAD:
                        l1 = System.currentTimeMillis();
                        procUnload(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                        break;
                    case ASQL_MULTIPLE_LOAD:
                        l1 = System.currentTimeMillis();
                        procLoad(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this.out.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                }
                break;
            case 5:
                int k = getSingleID(paramCommand.COMMAND);
                switch (k) {
                    case ASQL_SINGLE_MSSQL:
                        procConnectMSSQL(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_SYBASE:
                        procConnectSybase(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_DISCONNECT:
                        procDisconnect(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_DEBUGLEVEL:
                        procDebugLevel(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_PAGESIZE:
                        procPageSize(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_HEADING:
                        procHeading(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_DELIMITER:
                        procDelimiter(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_VAR:
                        procVariable(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_UNVAR:
                        procUnvariable(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_PRINT:
                        procPrint(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_TIMING:
                        procTiming(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_DEFINE:
                        procDefine(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_SQLSET:
                        procSQLSet(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_SPOOLAPPEND:
                        procSpoolAppend(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_SPOOL:
                        procSpool(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_READ:
                        procRead(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_AUTOCOMMIT:
                        procAutocommit(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_HELP:
                        procHelp(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_USE:
                        procUse(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_HOST:
                        procHost(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_SPOOLOFF:
                        procSpoolOff(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_BUFFERADD:
                        procBUFFERADD(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_BUFFERLIST:
                        procBUFFERLIST(paramCommand.COMMAND);
                        break;
                    case ASQL_SINGLE_BUFFERRESET:
                        procBUFFERRESET(paramCommand.COMMAND);
                    case ASQL_SINGLE_AUTOTRACE:
                    case ASQL_SINGLE_AUTOT:
                }
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
        }
        return true;
    }

    private void procConnectSybase(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[8]).size();
        String str1 = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str1));
        String str2 = null;
        String str3 = null;
        String str4 = null;
        if (arrayOfString.length > 0) {
            str2 = arrayOfString[0];
            if (arrayOfString.length > 1)
                str3 = arrayOfString[1];
            if (arrayOfString.length > 2)
                str4 = arrayOfString[2];
        } else {
            this.out.println("Usage:");
            this.out.println("  SYBASE host:port/db username passwd");
            return;
        }
        try {
            try {
                if (this.database != null)
                    this.database.close();
            } catch (SQLException localSQLException1) {
            }
            this.database = DBConnection.getConnection("JTDSSYB", str2, str3, str4);
            this.database.setAutoCommit(false);
            this.autoCommit = this.database.getAutoCommit();
            this.out.println("Database connected.");
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procConnectMSSQL(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[0]).size();
        String str1 = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str1));
        String str2 = null;
        String str3 = null;
        String str4 = null;
        if (arrayOfString.length > 0) {
            str2 = arrayOfString[0];
            if (arrayOfString.length > 1)
                str3 = arrayOfString[1];
            if (arrayOfString.length > 2)
                str4 = arrayOfString[2];
        } else {
            this.out.println("Usage:");
            this.out.println("  MSSQL host:port/db username passwd");
            return;
        }
        try {
            try {
                if (this.database != null)
                    this.database.close();
            } catch (SQLException localSQLException1) {
            }
            this.database = DBConnection.getConnection("JTDSMSSQL", str2, str3, str4);
            this.database.setAutoCommit(false);
            this.autoCommit = this.database.getAutoCommit();
            this.out.println("Database connected.");
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procBUFFERADD(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[23]).size();
        String str = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        for (int j = 0; j < arrayOfString.length / 2; j++)
            this.loadBuffer.addColumn(arrayOfString[(j * 2)], SQLTypes.getTypeID(arrayOfString[(j * 2 + 1)]));
        this.out.println("Command completed.");
    }

    private void procBUFFERLIST(String paramString) {
        this.out.println("Structure of load buffer:");
        for (int i = 1; i <= this.loadBuffer.getColumnCount(); i++)
            this.out.println("  " + lpad(this.loadBuffer.getColumnName(i), 20) + "    " + SQLTypes.getTypeName(this.loadBuffer.getColumnType(i)));
    }

    private void procBUFFERRESET(String paramString) {
        this.loadBuffer.deleteAllRow();
        this.loadBuffer.removeAllColumn();
        this.out.println("Command completed.");
    }

    private void procLoad(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[5]).size();
        String str1 = skipWord(paramString, i);
        OptionCommand localOptionCommand = new OptionCommand(str1);
        str1 = localOptionCommand.getCommand();
        String str2 = localOptionCommand.getOption("F", "|");
        int j = localOptionCommand.getInt("S", 0);
        String str3 = null;
        String str4 = null;
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        if (j < 0)
            j = 0;
        int k = str1.indexOf("<<");
        if (k >= 0) {
            str4 = str1.substring(0, k).trim();
            str3 = str1.substring(k + 2).trim();
        } else {
            this.out.println("Usage:");
            this.out.println("  LOAD -option val query << file");
            this.out.println("Note :");
            this.out.println("  -F change field seperator(Default:|)");
            this.out.println("  -S skip lines (values > 0)");
            return;
        }
        str3 = str3.trim();
        str4 = str4.trim();
        if ((str4.length() == 0) || (str3.length() == 0)) {
            this.out.println("Usage:");
            this.out.println("  LOAD -option val query << file");
            this.out.println("Note :");
            this.out.println("  -F change field seperator(Default:|)");
            this.out.println("  -S skip lines (values > 0)");
            return;
        }
        str3 = this.sysVariable.parseString(str3);
        SQLStatement localSQLStatement = null;
        BufferedReader localBufferedReader = null;
        int m = 0;
        try {
            localBufferedReader = new BufferedReader(new FileReader(str3));
            for (int n = 0; n < j; n++)
                localBufferedReader.readLine();
        } catch (IOException localIOException1) {
            this.out.println(localIOException1.getMessage());
            return;
        }
        try {
            localSQLStatement = prepareStatement(this.database, str4, this.sysVariable);
            do {
                this.loadBuffer.deleteAllRow();
                this.loadBuffer.read(localBufferedReader, str2, 200);
                localSQLStatement.executeBatch(this.sysVariable, this.loadBuffer, 1, this.loadBuffer.getRowCount());
                this.database.commit();
                m += this.loadBuffer.getRowCount();
            }
            while (this.loadBuffer.getRowCount() == 200);
            this.out.println("Command Completed.");
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        } catch (Exception localException) {
            this.out.println(localException.getMessage());
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if (localBufferedReader != null)
                localBufferedReader.close();
        } catch (IOException localIOException2) {
        }
        this.out.println(m + " rows loaded!");
    }

    private void procDebugLevel(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[1]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        int j = getInt(str, 0);
        setDebugLevel(j);
        this.out.println("Debug level set to : " + getDebugLevel());
    }

    private void procPageSize(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[2]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        int j = getInt(str, 14);
        this.out.setPagesize(j);
        this.out.println("Page size set to : " + this.out.getPagesize());
    }

    private void procTiming(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[6]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        this.timing = "ON".equalsIgnoreCase(str);
        this.out.println("Timing set to : " + (this.timing ? "ON" : "OFF"));
    }

    private void procSpoolAppend(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[14]).size();
        String str1 = skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            this.out.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = this.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(str2, true);
            if (this.out.getLogFile() != null)
                this.out.getLogFile().close();
            this.out.setLogFile(new OutputCommandLog(this, localFileOutputStream));
        } catch (IOException localIOException) {
            this.out.print(localIOException);
        }
    }

    private void procSpool(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[16]).size();
        String str1 = skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            this.out.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = this.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(str2);
            if (this.out.getLogFile() != null)
                this.out.getLogFile().close();
            this.out.setLogFile(new OutputCommandLog(this, localFileOutputStream));
        } catch (IOException localIOException) {
            this.out.print(localIOException);
        }
    }

    private void procRead(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[17]).size();
        String str1 = skipWord(paramString, i);
        str1 = str1.trim();
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str1));
        if (arrayOfString.length < 2) {
            this.out.println("Usage: READ varname filename");
            return;
        }
        String str2 = arrayOfString[0];
        String str3 = str1.substring(str2.length()).trim();
        if (!this.sysVariable.exists(str2)) {
            this.out.println("Variable " + str2 + " not defined!");
            return;
        }
        try {
            FileReader localFileReader = new FileReader(this.sysVariable.parseString(str3));
            char[] arrayOfChar = new char[65536];
            int j = localFileReader.read(arrayOfChar);
            try {
                this.sysVariable.setValue(str2, String.valueOf(arrayOfChar, 0, j));
            } catch (Exception localException) {
                this.out.print(localException);
            }
            localFileReader.close();
        } catch (IOException localIOException) {
            this.out.print(localIOException);
        }
    }

    private void procHelp(String paramString) {
        this.out.println("Usage: HELP");
        this.out.println();
        this.out.println(" MSSQL SYBASE VAR UNVAR PRINT DEFINE SQLSET SPOOL READ");
        this.out.println(" @ @@ LOB LOBEXP LOBIMP LOBLEN UNLOAD LOAD BUFFER");
    }

    private void procHost(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[19]).size();
        String str = skipWord(paramString, i);
        str = this.sysVariable.parseString(str.trim());
        if (str.length() > 0)
            try {
                host(str);
            } catch (IOException localIOException) {
                this.out.print(localIOException);
            }
    }

    private boolean procRun2(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[0]).size();
        String str1 = skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            this.out.println("Usage: @[@] file");
            return false;
        }
        String str2 = this.sysVariable.parseString(str1.trim());
        try {
            FileInputStream localFileInputStream = new FileInputStream(str2);
            Command localCommand = run(new InputCommandReader(localFileInputStream));
            return localCommand.COMMAND != null;
        } catch (IOException localIOException) {
            this.out.print(localIOException);
        }
        return false;
    }

    private void procAutocommit(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[22]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        if (str.length() == 0) {
            try {
                this.autoCommit = this.database.getAutoCommit();
                this.out.println("Autocommit : " + (this.autoCommit ? "ON" : "OFF"));
            } catch (SQLException localSQLException1) {
                this.out.print(localSQLException1);
            }
            return;
        }
        this.autoCommit = "ON".equalsIgnoreCase(str);
        try {
            this.database.setAutoCommit(this.autoCommit);
            this.autoCommit = this.database.getAutoCommit();
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procSpoolOff(String paramString) {
        if (this.out.getLogFile() != null)
            this.out.getLogFile().close();
        this.out.setLogFile(null);
    }

    private void procDefine(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[12]).size();
        String str1 = skipWord(paramString, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            this.out.println("Usage:");
            this.out.println("  DEFINE variable=value");
            this.out.println("  SET    PAGESIZE   pagesize");
            this.out.println("  SET    DEBUGLEVEL debuglevel");
            this.out.println("  SET    HEADING    {OFF|ON}");
            this.out.println("  SET    DELIMITER  delimiter");
            this.out.println("  SET    TIMING     {ON|OFF}");
            return;
        }
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.trim().length() == 0) {
            this.out.println("Usage:");
            this.out.println("  DEFINE variable=value");
            this.out.println("  SET    PAGESIZE   pagesize");
            this.out.println("  SET    DEBUGLEVEL debuglevel");
            this.out.println("  SET    HEADING    {OFF|ON}");
            this.out.println("  SET    DELIMITER  delimiter");
            this.out.println("  SET    TIMING     {ON|OFF}");
            return;
        }
        if (!this.sysVariable.exists(str2.trim())) {
            this.out.println("Variable " + str2 + " not declared.");
            return;
        }
        try {
            if (str3.length() == 0)
                this.sysVariable.setValue(str2, null);
            else
                this.sysVariable.setValue(str2, str3);
        } catch (Exception localException) {
            this.out.print("Invalid format : ");
            this.out.println(str3);
        }
    }

    private void procSQLSet(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[12]).size();
        String str1 = skipWord(paramString, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            this.out.println("Usage: SQLSET variable=query");
            return;
        }
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.trim().length() == 0) {
            this.out.println("Usage: SQLSET variable=query");
            return;
        }
        if (!this.sysVariable.exists(str2.trim())) {
            this.out.println("Variable " + str2 + " not declared.");
            return;
        }
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        try {
            DBRowCache localDBRowCache = executeQuery(this.database, str3, this.sysVariable, 1000);
            if ((localDBRowCache.getRowCount() == 1) && (localDBRowCache.getColumnCount() == 1))
                try {
                    this.sysVariable.setValue(str2, localDBRowCache.getItem(1, 1));
                } catch (Exception localException) {
                    this.out.print("Invalid format : ");
                    this.out.println(str3);
                }
            else
                this.out.println("Query does not return one row and one column.");
        } catch (SQLException localSQLException) {
            this.out.print(localSQLException);
        }
    }

    private void procHeading(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[3]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        boolean bool = "ON".equalsIgnoreCase(str);
        this.out.setHeading(bool);
        this.out.println("Heading set to : " + (bool ? "ON" : "OFF"));
    }

    private void procDelimiter(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[4]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        if ("TAB".equalsIgnoreCase(str))
            str = "\t";
        this.out.setSeperator(str.length() == 0 ? " " : str);
        this.out.println("Delimiter set to : " + (str.length() == 0 ? "SPACE" : str));
    }

    private void procVariable(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[9]).size();
        String str2 = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length < 2) {
            this.out.println("Usage:");
            this.out.println("  VAR varname vartype");
            this.out.println("");
            this.out.println("  CHAR VARCHAR LONGVARCHAR BINARY VARBINARY LONGVARBINARY");
            this.out.println("  NUMERIC DECIMAL BIT TINYINT SMALLINT INTEGER BIGINT REAL");
            this.out.println("  FLOAT DOUBLE DATE TIME TIMESTAMP BLOB CLOB");
            return;
        }
        if (this.sysVariable.exists(arrayOfString[0])) {
            this.out.println("Variable " + arrayOfString[0] + " already defined.");
            return;
        }
        this.sysVariable.add(arrayOfString[0], SQLTypes.getTypeID(arrayOfString[1]));
        this.out.println("Variable " + arrayOfString[0] + " created.");
    }

    private void procUnvariable(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[10]).size();
        String str2 = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length == 0) {
            this.out.println("Usage: UNVAR varname");
            return;
        }
        if (!this.sysVariable.exists(arrayOfString[0])) {
            this.out.println("Variable " + arrayOfString[0] + " not exist.");
            return;
        }
        this.sysVariable.remove(arrayOfString[0]);
        this.out.println("Variable " + arrayOfString[0] + " removed.");
    }

    private void procAutotrace(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[5]).size();
        String str = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        if (arrayOfString.length == 0) {
            this.out.println("Usage: SET AUTOTRACE {ON | OFF}");
            return;
        }
        if ((!"ON".equalsIgnoreCase(arrayOfString[0])) && (!"OFF".equalsIgnoreCase(arrayOfString[0]))) {
            this.out.println("Usage: SET AUTOTRACE {ON | OFF}");
            return;
        }
        this.out.setAutoTrace(arrayOfString[0].equalsIgnoreCase("ON"));
        this.out.println("Autotrace set to : " + str);
    }

    private void procUse(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[21]).size();
        String str = skipWord(paramString, i);
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        if (str.trim().length() == 0) {
            this.out.println("Usage: USE dbname");
            return;
        }
        try {
            this.database.setCatalog(str.trim());
        } catch (SQLException localSQLException) {
            this.out.print(localSQLException);
        }
    }

    private void procPrint(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(this.cmdType.getASQLSingle()[11]).size();
        String str2 = skipWord(paramString, i);
        this.out.println(this.sysVariable.parseString(str2));
    }

    public void procUnknownCommand(Command paramCommand) {
        paramCommand.TYPE1 = 1;
        execute(paramCommand);
    }

    private void procUnload(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[4]).size();
        String str1 = skipWord(paramString, i);
        OptionCommand localOptionCommand = new OptionCommand(str1);
        str1 = localOptionCommand.getCommand();
        String str2 = localOptionCommand.getOption("F", "|");
        String str3 = localOptionCommand.getOption("R", "\\r\\n");
        boolean bool = localOptionCommand.getBoolean("H", true);
        String str4 = null;
        String str5 = null;
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        int j = str1.indexOf(">>");
        if (j >= 0) {
            str5 = str1.substring(0, j).trim();
            str4 = str1.substring(j + 2).trim();
        } else {
            this.out.println("Usage:");
            this.out.println("  UNLOAD -option val query >> file");
            this.out.println("Note :");
            this.out.println("  -F change field seperator(Default:|)");
            this.out.println("  -R change record seperator(Default:\\r\\n)");
            this.out.println("  -H display field name {ON|OFF}");
            return;
        }
        str4 = str4.trim();
        str5 = str5.trim();
        if ((str5.length() == 0) || (str4.length() == 0)) {
            this.out.println("Usage:");
            this.out.println("  UNLOAD -option val query >> file");
            this.out.println("Note :");
            this.out.println("  -F change field seperator(Default:|)");
            this.out.println("  -R change record seperator(Default:\\r\\n)");
            this.out.println("  -H display field name {ON|OFF}");
            return;
        }
        str4 = this.sysVariable.parseString(str4);
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        PrintStream localPrintStream = null;
        File localFile = new File(str4);
        try {
            long l = System.currentTimeMillis();
            localSQLStatement = prepareStatement(this.database, str5, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            this.currentStmt = localSQLStatement.stmt;
            localResultSet = localSQLStatement.stmt.executeQuery();
            this.resultSet = localResultSet;
            this.out.println("Query executed in " + DBOperation.getElapsed(System.currentTimeMillis() - l));
            if (str4.trim().endsWith(".gz"))
                localPrintStream = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(localFile), 65536)));
            else
                localPrintStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(localFile), 262144));
            writeData(localPrintStream, localResultSet, parseRecord(str2), parseRecord(str3), bool);
            localPrintStream.close();
            localResultSet.close();
            localSQLStatement.close();
            this.resultSet = null;
            this.currentStmt = null;
            return;
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        } catch (IOException localIOException) {
            this.out.print(localIOException);
        }
        if (localPrintStream != null)
            localPrintStream.close();
        if (localResultSet != null)
            try {
                localResultSet.close();
            } catch (SQLException localSQLException2) {
                this.out.print(localSQLException2);
            }
        if (localSQLStatement != null)
            try {
                localSQLStatement.close();
            } catch (SQLException localSQLException3) {
                this.out.print(localSQLException3);
            }
    }

    public long writeData(PrintStream paramPrintStream, ResultSet paramResultSet, String paramString1, String paramString2, boolean paramBoolean)
            throws SQLException, IOException {
        long l1 = 0L;
        String str1 = null;
        int i = 0;
        byte[] arrayOfByte1 = new byte[8192];
        char[] arrayOfChar1 = new char[4096];
        byte[] arrayOfByte2 = new byte[65536];
        char[] arrayOfChar2 = new char[65536];
        long l2 = System.currentTimeMillis();
        ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
        int j = localResultSetMetaData.getColumnCount();
        int[] arrayOfInt = new int[j];
        SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat localSimpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat localSimpleDateFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        for (int k = 0; k < j; k++) {
            arrayOfInt[k] = localResultSetMetaData.getColumnType(k + 1);
            if (!paramBoolean)
                continue;
            str1 = localResultSetMetaData.getColumnName(k + 1);
            if (str1 != null)
                paramPrintStream.print(str1);
            if (k >= j - 1)
                continue;
            paramPrintStream.print(paramString1);
        }
        if (paramBoolean)
            paramPrintStream.print(paramString2);
        while (paramResultSet.next()) {
            l1 += 1L;
            for (int k = 1; k <= j; k++) {
                Object localObject1;
                Object localObject2;
                switch (arrayOfInt[(k - 1)]) {
                    case -1:
                        Reader localReader = paramResultSet.getCharacterStream(k);
                        if (localReader == null)
                            break;
                        try {
                            for (i = localReader.read(arrayOfChar2); i > 0; i = localReader.read(arrayOfChar2)) {
                                paramPrintStream.print(String.valueOf(arrayOfChar2, 0, i));
                                if (i < 65536)
                                    break;
                            }
                            localReader.close();
                        } catch (IOException localIOException1) {
                        }
                    case -4:
                        InputStream localInputStream1 = paramResultSet.getBinaryStream(k);
                        if (localInputStream1 == null)
                            break;
                        try {
                            for (i = localInputStream1.read(arrayOfByte2); i > 0; i = localInputStream1.read(arrayOfByte2)) {
                                paramPrintStream.write(arrayOfByte2, 0, i);
                                if (i < 65536)
                                    break;
                            }
                            localInputStream1.close();
                        } catch (IOException localIOException2) {
                        }
                    case 2005:
                        Clob localClob = paramResultSet.getClob(k);
                        if (localClob == null)
                            break;
                        localObject1 = localClob.getCharacterStream();
                        if (localObject1 == null)
                            break;
                        try {
                            for (i = ((Reader) localObject1).read(arrayOfChar2); i > 0; i = ((Reader) localObject1).read(arrayOfChar2)) {
                                paramPrintStream.print(String.valueOf(arrayOfChar2, 0, i));
                                if (i < 65536)
                                    break;
                            }
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException3) {
                        }
                    case 2004:
                        localObject1 = paramResultSet.getBlob(k);
                        if (localObject1 == null)
                            break;
                        localObject2 = ((Blob) localObject1).getBinaryStream();
                        if (localObject2 == null)
                            break;
                        try {
                            for (i = ((InputStream) localObject2).read(arrayOfByte2); i > 0; i = ((InputStream) localObject2).read(arrayOfByte2)) {
                                paramPrintStream.write(arrayOfByte2, 0, i);
                                if (i < 65536)
                                    break;
                            }
                            ((InputStream) localObject2).close();
                        } catch (IOException localIOException4) {
                        }
                    case 1:
                    case 12:
                        localObject2 = paramResultSet.getCharacterStream(k);
                        if (localObject2 == null)
                            break;
                        try {
                            i = ((Reader) localObject2).read(arrayOfChar1);
                            if (arrayOfInt[(k - 1)] == 1)
                                while ((i > 0) && (arrayOfChar1[(i - 1)] == ' '))
                                    i--;
                            if (i > 0)
                                paramPrintStream.print(String.valueOf(arrayOfChar1, 0, i));
                            ((Reader) localObject2).close();
                        } catch (IOException localIOException5) {
                        }
                    case -3:
                    case -2:
                        InputStream localInputStream2 = paramResultSet.getAsciiStream(k);
                        if (localInputStream2 == null)
                            break;
                        try {
                            i = localInputStream2.read(arrayOfByte1);
                            if (arrayOfInt[(k - 1)] == -2)
                                while ((i > 0) && (arrayOfByte1[(i - 1)] == 32))
                                    i--;
                            if (i > 0)
                                paramPrintStream.write(arrayOfByte1, 0, i);
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                    case 91:
                        Timestamp localTimestamp1 = paramResultSet.getTimestamp(k);
                        if (localTimestamp1 == null)
                            break;
                        paramPrintStream.print(localSimpleDateFormat1.format(localTimestamp1));
                        break;
                    case 92:
                        Time localTime = paramResultSet.getTime(k);
                        if (localTime == null)
                            break;
                        paramPrintStream.print(localSimpleDateFormat2.format(localTime));
                        break;
                    case 93:
                        Timestamp localTimestamp2 = paramResultSet.getTimestamp(k);
                        if (localTimestamp2 == null)
                            break;
                        paramPrintStream.print(localSimpleDateFormat3.format(localTimestamp2));
                        break;
                    case -102:
                    case -101:
                        Timestamp localTimestamp3 = paramResultSet.getTimestamp(k);
                        if (localTimestamp3 == null)
                            break;
                        paramPrintStream.print(localSimpleDateFormat4.format(localTimestamp3));
                        break;
                    case -14:
                    case -13:
                    case -10:
                    case 0:
                    case 70:
                    case 1111:
                    case 2000:
                    case 2001:
                    case 2002:
                    case 2003:
                    case 2006:
                        break;
                    default:
                        String str2 = paramResultSet.getString(k);
                        if (str2 == null)
                            break;
                        paramPrintStream.print(str2);
                }
                if (k < j)
                    paramPrintStream.print(paramString1);
                else
                    paramPrintStream.print(paramString2);
            }
            if (l1 % 100000L != 0L)
                continue;
            getCommandLog().println(lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(System.currentTimeMillis() - l2));
        }
        l2 = System.currentTimeMillis() - l2;
        if (l1 % 100000L != 0L)
            getCommandLog().println(lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(l2));
        if (l2 > 0L)
            getCommandLog().println("Done, total:" + l1 + " , avg:" + l1 * 1000L / l2 + " rows/s.");
        return l1;
    }

    private void procLOB(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[0]).size();
        String str1 = skipWord(paramString, i);
        String str2 = null;
        String str3 = null;
        int j = str1.indexOf("<<");
        if (j >= 0) {
            str3 = str1.substring(0, j).trim();
            str2 = str1.substring(j + 2).trim();
            procLOBWRITE(str3, str2);
        } else {
            j = str1.indexOf(">>");
            if (j >= 0) {
                str3 = str1.substring(0, j).trim();
                str2 = str1.substring(j + 2).trim();
                procLOBREAD(str3, str2);
            } else {
                this.out.println("Usage:");
                this.out.println("  LOB query >> file");
                this.out.println("  LOB query << file");
                this.out.println("Note :");
                this.out.println("  >> mean export long/long raw/blob/clob to a file ");
                this.out.println("  << mean import a file to blob/clob field, the query");
                this.out.println("     should include the for update clause");
            }
        }
    }

    private void procLOBREAD(String paramString1, String paramString2) {
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            this.out.println("Java VM 1.3 or above required to support this feature.");
            return;
        }
        if ((paramString2 == null) || (paramString1 == null) || (paramString2.length() == 0) || (paramString1.length() == 0)) {
            this.out.println("Usage:");
            this.out.println("  LOB query >> file");
            this.out.println("  LOB query << file");
            this.out.println("Note :");
            this.out.println("  >> mean export long/long raw/blob/clob to a file ");
            this.out.println("  << mean import a file to blob/clob field, the query");
            this.out.println("     should include the for update clause");
            return;
        }
        paramString2 = this.sysVariable.parseString(paramString2);
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        File localFile = new File(paramString2);
        try {
            localSQLStatement = prepareStatement(this.database, paramString1, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            localResultSet = localSQLStatement.stmt.executeQuery();
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if (localResultSet.next()) {
                Object localObject1;
                Object localObject2;
                Object localObject4;
                if (localResultSetMetaData.getColumnType(1) == -1) {
                    localObject1 = localResultSet.getCharacterStream(1);
                    if (localObject1 != null) {
                        localObject2 = new char[8192];
                        try {
                            int i = 0;
                            localObject4 = new FileWriter(localFile);
                            while ((i = ((Reader) localObject1).read((char[]) localObject2)) > 0)
                                ((FileWriter) localObject4).write((char[]) localObject2, 0, i);
                            ((FileWriter) localObject4).close();
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException1) {
                            this.out.print(localIOException1);
                        }
                    }
                } else if (localResultSetMetaData.getColumnType(1) == -4) {
                    localObject1 = localResultSet.getBinaryStream(1);
                    if (localObject1 != null) {
                        localObject2 = new byte[8192];
                        try {
                            int j = 0;
                            localObject4 = new FileOutputStream(localFile);
                            while ((j = ((InputStream) localObject1).read((byte[]) localObject2)) > 0)
                                ((FileOutputStream) localObject4).write((byte[]) localObject2, 0, j);
                            ((FileOutputStream) localObject4).close();
                            ((InputStream) localObject1).close();
                        } catch (IOException localIOException2) {
                            this.out.print(localIOException2);
                        }
                    }
                } else {
                    Object localObject3;
                    Object localObject5;
                    if (localResultSetMetaData.getColumnType(1) == 2005) {
                        localObject1 = localResultSet.getClob(1);
                        if (localObject1 != null) {
                            localObject2 = ((Clob) localObject1).getCharacterStream();
                            if (localObject2 != null) {
                                localObject3 = new char[8192];
                                try {
                                    int k = 0;
                                    localObject5 = new FileWriter(localFile);
                                    while ((k = ((Reader) localObject2).read((char[]) localObject3)) > 0)
                                        ((FileWriter) localObject5).write((char[]) localObject3, 0, k);
                                    ((FileWriter) localObject5).close();
                                    ((Reader) localObject2).close();
                                } catch (IOException localIOException3) {
                                    this.out.print(localIOException3);
                                }
                            }
                        }
                    } else if (localResultSetMetaData.getColumnType(1) == 2004) {
                        localObject1 = localResultSet.getBlob(1);
                        if (localObject1 != null) {
                            localObject2 = ((Blob) localObject1).getBinaryStream();
                            if (localObject2 != null) {
                                localObject3 = new byte[8192];
                                try {
                                    int m = 0;
                                    localObject5 = new FileOutputStream(localFile);
                                    while ((m = ((InputStream) localObject2).read((byte[]) localObject3)) > 0)
                                        ((FileOutputStream) localObject5).write((byte[]) localObject3, 0, m);
                                    ((FileOutputStream) localObject5).close();
                                    ((InputStream) localObject2).close();
                                } catch (IOException localIOException4) {
                                    this.out.print(localIOException4);
                                }
                            }
                        }
                    }
                }
                this.out.println("Command succeed.");
            } else {
                this.out.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            this.out.print(localException);
        }
        clearWarnings(this.database, this.out);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procLOBWRITE(String paramString1, String paramString2) {
        long l1 = 0L;
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 4)) {
            this.out.println("Java VM 1.4 or above required to support this feature.");
            return;
        }
        if (((paramString2 == null) && (paramString1 == null)) || (paramString2.length() == 0) || (paramString1.length() == 0)) {
            this.out.println("Usage:");
            this.out.println("  LOB query >> file");
            this.out.println("  LOB query << file");
            this.out.println("Note :");
            this.out.println("  >> mean export long/long raw/blob/clob to a file ");
            this.out.println("  << mean import a file to blob/clob field, the query");
            this.out.println("     should include the for update clause");
            return;
        }
        paramString2 = this.sysVariable.parseString(paramString2);
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        File localFile = new File(paramString2);
        if (!localFile.exists()) {
            this.out.println("File " + paramString2 + " does not exists!");
            return;
        }
        if (!localFile.isFile()) {
            this.out.println(paramString2 + " is not a valid file!");
            return;
        }
        if (!localFile.canRead()) {
            this.out.println("Cannot read file " + paramString2 + "!");
            return;
        }
        try {
            localSQLStatement = prepareStatement(this.database, paramString1, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            localResultSet = localSQLStatement.stmt.executeQuery();
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if (localResultSet.next()) {

                if (localResultSetMetaData.getColumnType(1) == 2005) {
                    Clob clob = localResultSet.getClob(1);
                    long l2 = 0L;
                    if (clob != null) {
                        char[] arrayOfChar = new char[8192];
                        try {
                            int i = 0;
                            clob.truncate(l1);
                            Writer writer = clob.setCharacterStream(l1);
                            FileReader reader = new FileReader(localFile);
                            while ((i = reader.read(arrayOfChar)) > 0) {
                                writer.write(arrayOfChar, 0, i);
                                l2 += i;
                            }
                            reader.close();
                            writer.close();
                        } catch (IOException localIOException1) {
                            this.out.print(localIOException1);
                        }
                    }
                } else if (localResultSetMetaData.getColumnType(1) == 2004) {
                    Blob blob = localResultSet.getBlob(1);
                    if (blob != null) {
                        byte[] arrayOfByte = new byte[8192];
                        long l3 = 0L;
                        try {
                            int j = 0;
                            blob.truncate(l1);
                            OutputStream outputStream = blob.setBinaryStream(l1);
                            FileInputStream inputStream = new FileInputStream(localFile);
                            while ((j = inputStream.read(arrayOfByte)) > 0) {
                                outputStream.write(arrayOfByte, 0, j);
                                l3 += j;
                            }
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException localIOException2) {
                            this.out.print(localIOException2);
                        }
                    }
                }
                this.out.println("Command succeed.");
            } else {
                this.out.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            this.out.print(localException);
        }
        clearWarnings(this.database, this.out);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procLOBEXP(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[1]).size();
        String str1 = skipWord(paramString, i);
        str1 = str1.trim();
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            this.out.println("Java VM 1.3 required to support this feature.");
            return;
        }
        if (str1.length() == 0) {
            this.out.println("Usage:");
            this.out.println("  LOBEXP query");
            this.out.println("Note :");
            this.out.println("  Query should return tow column as following:");
            this.out.println("  col1 : CHAR or VARCHAR specify the filename.");
            this.out.println("  col2 : long/long raw/blob/clob field.");
            return;
        }
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        try {
            localSQLStatement = prepareStatement(this.database, str1, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            localResultSet = localSQLStatement.stmt.executeQuery();
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if ((localResultSetMetaData.getColumnCount() != 2) || ((localResultSetMetaData.getColumnType(1) != 12) && (localResultSetMetaData.getColumnType(1) != 1)) || ((localResultSetMetaData.getColumnType(2) != -1) && (localResultSetMetaData.getColumnType(2) != -4) && (localResultSetMetaData.getColumnType(2) != 2004) && (localResultSetMetaData.getColumnType(2) != 2005))) {
                this.out.println("Usage:");
                this.out.println("  LOBEXP query");
                this.out.println("Note :");
                this.out.println("  Query should return tow column as following:");
                this.out.println("  col1 : CHAR or VARCHAR specify the filename.");
                this.out.println("  col2 : long/long raw/blob/clob field.");
                try {
                    if (localResultSet != null)
                        localResultSet.close();
                } catch (SQLException localSQLException3) {
                    this.out.print(localSQLException3);
                }
                try {
                    if (localSQLStatement != null)
                        localSQLStatement.close();
                } catch (SQLException localSQLException4) {
                    this.out.print(localSQLException4);
                }
                return;
            }
            while (localResultSet.next()) {
                String str2 = localResultSet.getString(1);
                if (str2 == null)
                    this.out.println("The file name is null!");
                else
                    this.out.println("Export lob data to file: " + str2);
                File localFile = new File(str2);
                if (localResultSetMetaData.getColumnType(2) == -1) {
                    Reader reader = localResultSet.getCharacterStream(2);
                    if (reader == null)
                        continue;
                    char[] buffer = new char[8192];
                    try {
                        int j = 0;
                        FileWriter output = new FileWriter(localFile);
                        while ((j = reader.read(buffer)) > 0)
                            output.write(buffer, 0, j);
                        output.close();
                        reader.close();
                    } catch (IOException localIOException1) {
                        this.out.print(localIOException1);
                    }
                    continue;
                }
                if (localResultSetMetaData.getColumnType(2) == -4) {
                    InputStream stream = localResultSet.getBinaryStream(2);
                    if (stream == null)
                        continue;
                    byte[] buffer = new byte[8192];
                    try {
                        int k = 0;
                        FileOutputStream output = new FileOutputStream(localFile);
                        while ((k = stream.read(buffer)) > 0) {
                            output.write(buffer, 0, k);
                        }
                        output.close();
                        stream.close();
                    } catch (IOException localIOException2) {
                        this.out.print(localIOException2);
                    }
                    continue;
                }
                if (localResultSetMetaData.getColumnType(2) == 2005) {
                    Clob localObject1 = localResultSet.getClob(2);
                    if (localObject1 == null)
                        continue;
                    Reader localObject2 = localObject1.getCharacterStream();
                    if (localObject2 == null)
                        continue;
                    char[] localObject3 = new char[8192];
                    try {
                        int m = 0;
                        FileWriter output = new FileWriter(localFile);
                        while ((m = localObject2.read(localObject3)) > 0) {
                            output.write(localObject3, 0, m);
                        }
                        output.close();
                        localObject2.close();
                    } catch (IOException localIOException3) {
                        this.out.print(localIOException3);
                    }
                    continue;
                }
                if (localResultSetMetaData.getColumnType(2) != 2004)
                    continue;
                Blob blob = localResultSet.getBlob(2);
                if (blob == null)
                    continue;
                InputStream stream = blob.getBinaryStream();
                if (stream == null)
                    continue;
                byte[] buffer = new byte[8192];
                try {
                    int n = 0;
                    FileOutputStream output = new FileOutputStream(localFile);
                    while ((n = stream.read(buffer)) > 0) {
                        output.write(buffer, 0, n);
                    }
                    output.close();
                    stream.close();
                } catch (IOException localIOException4) {
                    this.out.print(localIOException4);
                }
            }
            this.out.println("Command succeed.");
        } catch (Exception localException) {
            this.out.print(localException);
        }
        clearWarnings(this.database, this.out);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procLOBIMP(String paramString) {
        long l1 = 0L;
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[2]).size();
        String str1 = skipWord(paramString, i);
        str1 = str1.trim();
        char[] arrayOfChar = new char[8192];
        byte[] arrayOfByte = new byte[8192];
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 4)) {
            this.out.println("Java VM 1.4 required to support this feature.");
            return;
        }
        if (str1.length() == 0) {
            this.out.println("Usage:");
            this.out.println("  LOBIMP query");
            this.out.println("Note :");
            this.out.println("  Query should return tow column as following:");
            this.out.println("  col1 : CHAR or VARCHAR specify the filename.");
            this.out.println("  col2 : blob/clob field.");
            return;
        }
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        try {
            localSQLStatement = prepareStatement(this.database, str1, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            this.currentStmt = localSQLStatement.stmt;
            localResultSet = localSQLStatement.stmt.executeQuery();
            this.resultSet = localResultSet;
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if ((localResultSetMetaData.getColumnCount() != 2) || ((localResultSetMetaData.getColumnType(1) != 12) && (localResultSetMetaData.getColumnType(1) != 1)) || ((localResultSetMetaData.getColumnType(2) != 2004) && (localResultSetMetaData.getColumnType(2) != 2005))) {
                this.out.println("Usage:");
                this.out.println("  LOBIMP query");
                this.out.println("Note :");
                this.out.println("  Query should return tow column as following:");
                this.out.println("  col1 : CHAR or VARCHAR specify the filename.");
                this.out.println("  col2 : blob/clob field.");
                try {
                    if (localResultSet != null)
                        localResultSet.close();
                } catch (SQLException localSQLException3) {
                    this.out.print(localSQLException3);
                }
                try {
                    if (localSQLStatement != null)
                        localSQLStatement.close();
                } catch (SQLException localSQLException4) {
                    this.out.print(localSQLException4);
                }
                return;
            }
            while (localResultSet.next()) {
                String str2 = localResultSet.getString(1);
                File localFile = new File(str2);
                if (!localFile.exists()) {
                    this.out.println("File " + str2 + " does not exists!");
                    continue;
                }
                if (!localFile.isFile()) {
                    this.out.println(str2 + " is not a valid file!");
                    continue;
                }
                if (!localFile.canRead()) {
                    this.out.println("Cannot read file " + str2 + "!");
                    continue;
                }
                Object localObject1;
                long l2;
                Object localObject2;
                Object localObject3;
                if (localResultSetMetaData.getColumnType(2) == 2005) {
                    localObject1 = localResultSet.getClob(2);
                    l2 = 0L;
                    if (localObject1 != null)
                        try {
                            int j = 0;
                            ((Clob) localObject1).truncate(l1);
                            localObject2 = ((Clob) localObject1).setCharacterStream(l1);
                            localObject3 = new FileReader(localFile);
                            while ((j = ((FileReader) localObject3).read(arrayOfChar)) > 0) {
                                ((Writer) localObject2).write(arrayOfChar, 0, j);
                                l2 += j;
                            }
                            ((FileReader) localObject3).close();
                            ((Writer) localObject2).close();
                        } catch (IOException localIOException1) {
                            this.out.print(localIOException1);
                        }
                } else if (localResultSetMetaData.getColumnType(2) == 2004) {
                    localObject1 = localResultSet.getBlob(2);
                    if (localObject1 != null) {
                        l2 = 0L;
                        try {
                            int k = 0;
                            ((Blob) localObject1).truncate(l1);
                            localObject2 = ((Blob) localObject1).setBinaryStream(l1);
                            localObject3 = new FileInputStream(localFile);
                            while ((k = ((FileInputStream) localObject3).read(arrayOfByte)) > 0) {
                                ((OutputStream) localObject2).write(arrayOfByte, 0, k);
                                l2 += k;
                            }
                            ((FileInputStream) localObject3).close();
                            ((OutputStream) localObject2).close();
                        } catch (IOException localIOException2) {
                            this.out.print(localIOException2);
                        }
                    }
                }
                this.out.println("File " + str2 + " loaded.");
            }
            this.out.println("Command succeed.");
        } catch (Exception localException) {
            this.out.print(localException);
        }
        this.currentStmt = null;
        this.resultSet = null;
        clearWarnings(this.database, this.out);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    private void procLOBLEN(String paramString) {
        int i = TextUtils.getWords(this.cmdType.getASQLMultiple()[2]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        int j = 0;
        long l = 0L;
        char[] arrayOfChar = new char[8192];
        byte[] arrayOfByte = new byte[8192];
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            this.out.println("Java VM 1.3 required to support this feature.");
            return;
        }
        if (str.length() == 0) {
            this.out.println("Usage:");
            this.out.println("  LOBLEN query");
            this.out.println("Note :");
            this.out.println("  Query should return one column as following:");
            this.out.println("  col1 : long/long raw/blob/clob field.");
            return;
        }
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        try {
            localSQLStatement = prepareStatement(this.database, str, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            this.currentStmt = localSQLStatement.stmt;
            localResultSet = localSQLStatement.stmt.executeQuery();
            this.resultSet = localResultSet;
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if ((localResultSetMetaData.getColumnCount() != 1) || ((localResultSetMetaData.getColumnType(1) != 12) && (localResultSetMetaData.getColumnType(1) != 1) && (localResultSetMetaData.getColumnType(1) != -1) && (localResultSetMetaData.getColumnType(1) != -4) && (localResultSetMetaData.getColumnType(1) != 2004) && (localResultSetMetaData.getColumnType(1) != 2005))) {
                this.out.println("Usage:");
                this.out.println("  LOBLEN query");
                this.out.println("Note :");
                this.out.println("  Query should return one column as following:");
                this.out.println("  col1 : long/long raw/blob/clob field.");
                try {
                    if (localResultSet != null)
                        localResultSet.close();
                } catch (SQLException localSQLException3) {
                    this.out.print(localSQLException3);
                }
                try {
                    if (localSQLStatement != null)
                        localSQLStatement.close();
                } catch (SQLException localSQLException4) {
                    this.out.print(localSQLException4);
                }
                return;
            }
            j = localResultSetMetaData.getColumnType(1);
            while (localResultSet.next()) {
                l = 0L;
                Object localObject1;
                if (j == -1) {
                    localObject1 = localResultSet.getCharacterStream(1);
                    if (localObject1 != null)
                        try {
                            int k = 0;
                            while ((k = ((Reader) localObject1).read(arrayOfChar)) > 0)
                                l += k;
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException1) {
                            this.out.println();
                            this.out.print(localIOException1);
                        }
                } else if (j == -4) {
                    localObject1 = localResultSet.getBinaryStream(1);
                    if (localObject1 != null)
                        try {
                            int m = 0;
                            while ((m = ((InputStream) localObject1).read(arrayOfByte)) > 0)
                                l += m;
                            ((InputStream) localObject1).close();
                        } catch (IOException localIOException2) {
                            this.out.println();
                            this.out.print(localIOException2);
                        }
                } else {
                    Object localObject2;
                    if (j == 2005) {
                        localObject1 = localResultSet.getClob(1);
                        if (localObject1 != null) {
                            localObject2 = ((Clob) localObject1).getCharacterStream();
                            if (localObject2 != null)
                                try {
                                    int n = 0;
                                    while ((n = ((Reader) localObject2).read(arrayOfChar)) > 0)
                                        l += n;
                                    ((Reader) localObject2).close();
                                } catch (IOException localIOException3) {
                                    this.out.println();
                                    this.out.print(localIOException3);
                                }
                        }
                    } else if (j == 2004) {
                        localObject1 = localResultSet.getBlob(1);
                        if (localObject1 != null) {
                            localObject2 = ((Blob) localObject1).getBinaryStream();
                            if (localObject2 != null)
                                try {
                                    int i1 = 0;
                                    while ((i1 = ((InputStream) localObject2).read(arrayOfByte)) > 0)
                                        l += i1;
                                    ((InputStream) localObject2).close();
                                } catch (IOException localIOException4) {
                                    this.out.println();
                                    this.out.print(localIOException4);
                                }
                        }
                    }
                }
                this.out.println(l + "," + l / 1024L);
            }
        } catch (Exception localException) {
            this.out.print(localException);
        }
        this.currentStmt = null;
        this.resultSet = null;
        clearWarnings(this.database, this.out);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    @Override
    public void showVersion() {
        this.out.println();
        this.out.println(" AnySQL for SQL Server/Sybase, version 2.0.0 -- " + DateOperator.getDay("yyyy-MM-dd HH:mm:ss"));
        this.out.println();
        this.out.println(" (@) Copyright Lou Fangxin, all rights reserved.");
        this.out.println();
    }

    public long writeData(BufferedWriter paramBufferedWriter, ResultSet paramResultSet, String paramString1, String paramString2, boolean paramBoolean)
            throws SQLException, IOException {
        long l1 = 0L;
        String str1 = null;
        int i = 0;
        int j = 32768;
        byte[] arrayOfByte1 = new byte[8192];
        char[] arrayOfChar1 = new char[4096];
        byte[] arrayOfByte2 = new byte[65536];
        char[] arrayOfChar2 = new char[65536];
        long l2 = System.currentTimeMillis();
        ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
        int k = localResultSetMetaData.getColumnCount();
        int[] arrayOfInt = new int[k];
        SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat localSimpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int m = 0; m < k; m++) {
            arrayOfInt[m] = localResultSetMetaData.getColumnType(m + 1);
            if (!paramBoolean)
                continue;
            str1 = localResultSetMetaData.getColumnName(m + 1);
            if (str1 != null)
                paramBufferedWriter.write(str1);
            if (m >= k - 1)
                continue;
            paramBufferedWriter.write(paramString1);
        }
        if (paramBoolean)
            paramBufferedWriter.write(paramString2);
        while (paramResultSet.next()) {
            l1 += 1L;
            for (int m = 1; m <= k; m++) {
                Object localObject1;
                Object localObject2;
                switch (arrayOfInt[(m - 1)]) {
                    case -1:
                        Reader localReader = paramResultSet.getCharacterStream(m);
                        if (localReader == null)
                            break;
                        try {
                            i = localReader.read(arrayOfChar2);
                            if (i > 0)
                                paramBufferedWriter.write(arrayOfChar2, 0, i);
                            localReader.close();
                        } catch (IOException localIOException1) {
                        }
                    case -4:
                        InputStream localInputStream1 = paramResultSet.getBinaryStream(m);
                        if (localInputStream1 == null)
                            break;
                        try {
                            i = localInputStream1.read(arrayOfByte2);
                            if (i > 0)
                                paramBufferedWriter.write(new String(arrayOfByte2, 0, i));
                            localInputStream1.close();
                        } catch (IOException localIOException2) {
                        }
                    case 2005:
                        Clob localClob = paramResultSet.getClob(m);
                        if (localClob == null)
                            break;
                        localObject1 = localClob.getCharacterStream();
                        if (localObject1 == null)
                            break;
                        try {
                            i = ((Reader) localObject1).read(arrayOfChar2);
                            if (i > 0)
                                paramBufferedWriter.write(arrayOfChar2, 0, i);
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException3) {
                        }
                    case 2004:
                        localObject1 = paramResultSet.getBlob(m);
                        if (localObject1 == null)
                            break;
                        localObject2 = ((Blob) localObject1).getBinaryStream();
                        if (localObject2 == null)
                            break;
                        try {
                            i = ((InputStream) localObject2).read(arrayOfByte2);
                            if (i > 0)
                                paramBufferedWriter.write(new String(arrayOfByte2, 0, i));
                            ((InputStream) localObject2).close();
                        } catch (IOException localIOException4) {
                        }
                    case 1:
                    case 12:
                        localObject2 = paramResultSet.getCharacterStream(m);
                        if (localObject2 == null)
                            break;
                        try {
                            i = ((Reader) localObject2).read(arrayOfChar1);
                            if (arrayOfInt[(m - 1)] == 1)
                                while ((i > 0) && (arrayOfChar1[(i - 1)] == ' '))
                                    i--;
                            if (i > 0)
                                paramBufferedWriter.write(arrayOfChar1, 0, i);
                            ((Reader) localObject2).close();
                        } catch (IOException localIOException5) {
                        }
                    case -3:
                    case -2:
                        InputStream localInputStream2 = paramResultSet.getAsciiStream(m);
                        if (localInputStream2 == null)
                            break;
                        try {
                            i = localInputStream2.read(arrayOfByte1);
                            if (arrayOfInt[(m - 1)] == -2)
                                while ((i > 0) && (arrayOfByte1[(i - 1)] == 32))
                                    i--;
                            if (i > 0)
                                paramBufferedWriter.write(new String(arrayOfByte1, 0, i));
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                    case 91:
                        Date localDate = paramResultSet.getDate(m);
                        if (localDate == null)
                            break;
                        paramBufferedWriter.write(localSimpleDateFormat1.format(localDate));
                        break;
                    case 92:
                        Time localTime = paramResultSet.getTime(m);
                        if (localTime == null)
                            break;
                        paramBufferedWriter.write(localSimpleDateFormat2.format(localTime));
                        break;
                    case 93:
                        Timestamp localTimestamp = paramResultSet.getTimestamp(m);
                        if (localTimestamp == null)
                            break;
                        paramBufferedWriter.write(localSimpleDateFormat3.format(localTimestamp));
                        break;
                    case 0:
                    case 70:
                    case 1111:
                    case 2000:
                    case 2001:
                    case 2002:
                    case 2003:
                    case 2006:
                        break;
                    default:
                        String str2 = paramResultSet.getString(m);
                        if (str2 == null)
                            break;
                        paramBufferedWriter.write(str2);
                }
                if (m < k)
                    paramBufferedWriter.write(paramString1);
                else
                    paramBufferedWriter.write(paramString2);
            }
            if (l1 % 100000L != 0L)
                continue;
            getCommandLog().println(lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(System.currentTimeMillis() - l2));
        }
        if (l1 % 100000L != 0L)
            getCommandLog().println(lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(System.currentTimeMillis() - l2));
        return l1;
    }

    public int fetch(ResultSet rs, DBRowCache rowCache)
            throws SQLException {
        return fetch(rs, rowCache, 100);
    }

    public int fetch(ResultSet rs, DBRowCache rowCache, int size)
            throws SQLException {
        int i = 0;
        int j = 0;
        int k = 0;
        byte[] arrayOfByte1 = new byte[8192];
        char[] arrayOfChar1 = new char[4096];
        byte[] arrayOfByte2 = new byte[65536];
        char[] arrayOfChar2 = new char[65536];
        Object localObject2;
        if (rowCache.getColumnCount() == 0) {
            localObject2 = rs.getMetaData();
            for (i = 1; i <= ((ResultSetMetaData) localObject2).getColumnCount(); i++)
                if (((ResultSetMetaData) localObject2).getColumnName(i) != null) {
                    if (rowCache.findColumn(((ResultSetMetaData) localObject2).getColumnName(i)) == 0) {
                        rowCache.addColumn(((ResultSetMetaData) localObject2).getColumnName(i), ((ResultSetMetaData) localObject2).getColumnType(i));
                    } else {
                        for (j = 1; rowCache.findColumn(((ResultSetMetaData) localObject2).getColumnName(i) + "_" + j) != 0; j++)
                            ;
                        rowCache.addColumn(((ResultSetMetaData) localObject2).getColumnName(i) + "_" + j, ((ResultSetMetaData) localObject2).getColumnType(i));
                    }
                } else {
                    for (j = 1; rowCache.findColumn("NULL" + j) != 0; j++) ;
                    rowCache.addColumn("NULL" + j, ((ResultSetMetaData) localObject2).getColumnType(i));
                }
        }
        if (rowCache.getColumnCount() == 0)
            return 0;
        Object[] arrayOfObject;
        for (i = rowCache.getRowCount(); (i < size) && (rs.next()); i = rowCache.appendRow(arrayOfObject)) {
            arrayOfObject = new Object[rowCache.getColumnCount()];
            for (j = 1; j <= rowCache.getColumnCount(); j++) {
                Object localObject1 = null;
                int m;
                Object localObject3;
                Object localObject4;
                switch (rowCache.getColumnType(j)) {
                    case -1:
                        localObject2 = rs.getCharacterStream(j);
                        if (localObject2 == null)
                            break;
                        try {
                            m = ((Reader) localObject2).read(arrayOfChar2);
                            if (m > 0)
                                localObject1 = String.valueOf(arrayOfChar2, 0, m);
                            ((Reader) localObject2).close();
                        } catch (IOException localIOException1) {
                        }
                    case -4:
                        InputStream localInputStream1 = rs.getBinaryStream(j);
                        if (localInputStream1 == null)
                            break;
                        try {
                            m = localInputStream1.read(arrayOfByte2);
                            if (m > 0)
                                localObject1 = new String(arrayOfByte2, 0, m);
                            localInputStream1.close();
                        } catch (IOException localIOException2) {
                        }
                    case 2005:
                        Clob localClob = rs.getClob(j);
                        if (localClob == null)
                            break;
                        localObject3 = localClob.getCharacterStream();
                        if (localObject3 == null)
                            break;
                        try {
                            m = ((Reader) localObject3).read(arrayOfChar2);
                            if (m > 0)
                                localObject1 = String.valueOf(arrayOfChar2, 0, m);
                            ((Reader) localObject3).close();
                        } catch (IOException localIOException3) {
                        }
                    case 2004:
                        localObject3 = rs.getBlob(j);
                        if (localObject3 == null)
                            break;
                        localObject4 = ((Blob) localObject3).getBinaryStream();
                        if (localObject4 == null)
                            break;
                        try {
                            m = ((InputStream) localObject4).read(arrayOfByte2);
                            if (m > 0)
                                localObject1 = new String(arrayOfByte2, 0, m);
                            ((InputStream) localObject4).close();
                        } catch (IOException localIOException4) {
                        }
                    case 1:
                    case 12:
                        localObject4 = rs.getCharacterStream(j);
                        if (localObject4 == null)
                            break;
                        try {
                            m = ((Reader) localObject4).read(arrayOfChar1);
                            if (rowCache.getColumnType(j) == 1)
                                while ((m > 0) && (arrayOfChar1[(m - 1)] == ' '))
                                    m--;
                            if (m > 0)
                                localObject1 = String.valueOf(arrayOfChar1, 0, m);
                            ((Reader) localObject4).close();
                        } catch (IOException localIOException5) {
                        }
                    case -3:
                    case -2:
                        InputStream localInputStream2 = rs.getAsciiStream(j);
                        if (localInputStream2 == null)
                            break;
                        try {
                            m = localInputStream2.read(arrayOfByte1);
                            if (rowCache.getColumnType(j) == -2)
                                while ((m > 0) && (arrayOfByte1[(m - 1)] == 32))
                                    m--;
                            if (m > 0)
                                localObject1 = new String(arrayOfByte1, 0, m);
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                    case 91:
                        localObject1 = rs.getDate(j);
                        break;
                    case 92:
                        localObject1 = rs.getTime(j);
                        break;
                    case -102:
                    case -101:
                    case 93:
                        localObject1 = rs.getTimestamp(j);
                        break;
                    case -7:
                    case -6:
                    case -5:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 16:
                        localObject1 = rs.getObject(j);
                        break;
                    case -14:
                    case -13:
                    case -10:
                    case 0:
                    case 70:
                    case 1111:
                    case 2000:
                    case 2001:
                    case 2002:
                    case 2003:
                    case 2006:
                        localObject1 = "N/A";
                        break;
                    default:
                        localObject1 = rs.getString(j);
                }
                arrayOfObject[(j - 1)] = localObject1;
            }
        }
        return i;
    }
}
