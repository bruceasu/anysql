package com.asql.mysql;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.log.CommandLog;
import com.asql.core.io.CommandReader;
import com.asql.core.DBConnection;
import com.asql.core.DBOperation;
import com.asql.core.DBRowCache;
import com.asql.core.util.DateOperator;
import com.asql.core.DefaultSQLExecutor;
import com.asql.core.io.InputCommandReader;
import com.asql.core.util.JavaVM;
import com.asql.core.log.OutputCommandLog;
import com.asql.core.SQLStatement;
import com.asql.core.SQLTypes;
import com.asql.core.util.TextUtils;
import com.asql.core.VariableTable;
import com.asql.core.log.DefaultCommandLog;
import com.asql.core.io.DefaultCommandReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class MySQLSQLExecutor extends DefaultSQLExecutor {
    private CMDType _cmdtype = null;
    private CommandLog _stdout = null;
    private CommandReader _stdin = null;
    private VariableTable tnsnames = new VariableTable();
    private Command lastcommand = null;
    public final int ASQL_SINGLE_CONNECT = 0;
    public final int ASQL_SINGLE_DEBUGLEVEL = 1;
    public final int ASQL_SINGLE_PAGESIZE = 2;
    public final int ASQL_SINGLE_HEADING = 3;
    public final int ASQL_SINGLE_DELIMITER = 4;
    public final int ASQL_SINGLE_AUTOTRACE = 5;
    public final int ASQL_SINGLE_TIMING = 6;
    public final int ASQL_SINGLE_DISCONNECT = 7;
    public final int ASQL_SINGLE_CONN = 8;
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
    private final int ASQL_SINGLE_SQLFILE_1 = 0;
    private final int ASQL_SINGLE_SQLFILE_2 = 1;
    private final int ASQL_MULTIPLE_LOB = 0;
    private final int ASQL_MULTIPLE_LOBEXP = 1;

    public MySQLSQLExecutor() {
        super(new MySQLCMDType());
        this._cmdtype = new MySQLCMDType();
        this._stdout = new DefaultCommandLog(this);
        this._stdin = new DefaultCommandReader();
        setShowComplete(false);
        setFetchSize(12);
    }

    public MySQLSQLExecutor(CommandReader paramCommandReader, CommandLog paramCommandLog) {
        super(new MySQLCMDType(), paramCommandReader, paramCommandLog);
        this._cmdtype = new MySQLCMDType();
        this._stdout = paramCommandLog;
        this._stdin = paramCommandReader;
        setShowComplete(false);
        setFetchSize(12);
    }

    public final boolean execute(Command paramCommand) {
        long l2 = 0L;
        if (paramCommand == null) {
            this._stdout.println("No command to execute.");
            return true;
        }
        long l1;
        switch (paramCommand.TYPE1) {
            case 0:
            case 1:
            case 2:
            case 13:
                if (!isConnected()) {
                    this._stdout.println("Database not connected.");
                    return true;
                }
                l1 = System.currentTimeMillis();
                executeSQL(this.database, paramCommand, this.sysVariable, this._stdout);
                l2 = System.currentTimeMillis();
                if (this.timing)
                    this._stdout.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                this.lastcommand = paramCommand;
                break;
            case 3:
                if (!isConnected()) {
                    this._stdout.println("Database not connected.");
                    return true;
                }
                l1 = System.currentTimeMillis();
                executeScript(this.database, paramCommand, this.sysVariable, this._stdout);
                l2 = System.currentTimeMillis();
                if (this.timing)
                    this._stdout.println(" Execute time: " + DBOperation.getElapsed(l2 - l1));
                this.lastcommand = paramCommand;
                break;
            case 7:
                execute(this.lastcommand);
                break;
            case 4:
                if (!isConnected()) {
                    this._stdout.println("Database not connected.");
                    return true;
                }
                l1 = System.currentTimeMillis();
                executeCall(this.database, new Command(paramCommand.TYPE1, paramCommand.TYPE2, skipWord(paramCommand.COMMAND, 1)), this.sysVariable, this._stdout);
                l2 = System.currentTimeMillis();
                if (!this.timing)
                    break;
                this._stdout.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                break;
            case 16:
                int i = this._cmdtype.startsWith(this._cmdtype.getSQLFile(), paramCommand.COMMAND);
                switch (i) {
                    case 0:
                        if (!procRun2("@@ " + paramCommand.COMMAND.trim().substring(2)))
                            break;
                        return false;
                    case 1:
                        if (!procRun2("@ " + paramCommand.COMMAND.trim().substring(1)))
                            break;
                        return false;
                }
                break;
            case 6:
                int j = getMultipleID(paramCommand.COMMAND);
                switch (j) {
                    case 0:
                        l1 = System.currentTimeMillis();
                        procLOB(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this._stdout.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                        break;
                    case 1:
                        l1 = System.currentTimeMillis();
                        procLOBEXP(paramCommand.COMMAND);
                        l2 = System.currentTimeMillis();
                        if (!this.timing)
                            break;
                        this._stdout.println("Execute time: " + DBOperation.getElapsed(l2 - l1));
                }
                break;
            case 5:
                int k = getSingleID(paramCommand.COMMAND);
                switch (k) {
                    case 0:
                    case 8:
                        procConnect(paramCommand.COMMAND);
                        break;
                    case 7:
                        procDisconnect(paramCommand.COMMAND);
                        break;
                    case 1:
                        procDebugLevel(paramCommand.COMMAND);
                        break;
                    case 2:
                        procPageSize(paramCommand.COMMAND);
                        break;
                    case 3:
                        procHeading(paramCommand.COMMAND);
                        break;
                    case 4:
                        procDelimiter(paramCommand.COMMAND);
                        break;
                    case 9:
                        procVariable(paramCommand.COMMAND);
                        break;
                    case 10:
                        procUnvariable(paramCommand.COMMAND);
                        break;
                    case 11:
                        procPrint(paramCommand.COMMAND);
                        break;
                    case 6:
                        procTiming(paramCommand.COMMAND);
                        break;
                    case 12:
                        procDefine(paramCommand.COMMAND);
                        break;
                    case 22:
                        procAutocommit(paramCommand.COMMAND);
                        break;
                    case 13:
                        procSQLSet(paramCommand.COMMAND);
                        break;
                    case 14:
                        procSpoolAppend(paramCommand.COMMAND);
                        break;
                    case 16:
                        procSpool(paramCommand.COMMAND);
                        break;
                    case 17:
                        procRead(paramCommand.COMMAND);
                        break;
                    case 18:
                        procHelp(paramCommand.COMMAND);
                        break;
                    case 19:
                        procHost(paramCommand.COMMAND);
                        break;
                    case 21:
                        procUse(paramCommand.COMMAND);
                        break;
                    case 15:
                        procSpoolOff(paramCommand.COMMAND);
                    case 5:
                    case 20:
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

    private void procConnect(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[0]).size();
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
            this._stdout.println("Usage:");
            this._stdout.println("  CONNECT host:port/db username passwd");
            return;
        }
        try {
            try {
                if (this.database != null)
                    this.database.close();
            } catch (SQLException localSQLException1) {
            }
            this.database = DBConnection.getConnection("MYSQL", str2, str3, str4);
            this.database.setAutoCommit(false);
            this.autoCommit = this.database.getAutoCommit();
            this._stdout.println("Database connected.");
        } catch (SQLException localSQLException2) {
            this._stdout.print(localSQLException2);
        }
    }

    private void procDebugLevel(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[1]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        int j = getInt(str, 0);
        setDebugLevel(j);
        this._stdout.println("Debug level set to : " + getDebugLevel());
    }

    private void procPageSize(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[2]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        int j = getInt(str, 14);
        this._stdout.setPagesize(j);
        this._stdout.println("Page size set to : " + this._stdout.getPagesize());
    }

    private void procTiming(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[6]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        this.timing = "ON".equalsIgnoreCase(str);
        this._stdout.println("Timing set to : " + (this.timing ? "ON" : "OFF"));
    }

    private void procSpoolAppend(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[14]).size();
        String str1 = skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            this._stdout.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = this.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(str2, true);
            if (this._stdout.getLogFile() != null)
                this._stdout.getLogFile().close();
            this._stdout.setLogFile(new OutputCommandLog(this, localFileOutputStream));
        } catch (IOException localIOException) {
            this._stdout.print(localIOException);
        }
    }

    private void procSpool(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[16]).size();
        String str1 = skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            this._stdout.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = this.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(str2);
            if (this._stdout.getLogFile() != null)
                this._stdout.getLogFile().close();
            this._stdout.setLogFile(new OutputCommandLog(this, localFileOutputStream));
        } catch (IOException localIOException) {
            this._stdout.print(localIOException);
        }
    }

    private void procRead(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[17]).size();
        String str1 = skipWord(paramString, i);
        str1 = str1.trim();
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str1));
        if (arrayOfString.length < 2) {
            this._stdout.println("Usage: READ varname filename");
            return;
        }
        String str2 = arrayOfString[0];
        String str3 = str1.substring(str2.length()).trim();
        if (!this.sysVariable.exists(str2)) {
            this._stdout.println("Variable " + str2 + " not defined!");
            return;
        }
        try {
            FileReader localFileReader = new FileReader(this.sysVariable.parseString(str3));
            char[] arrayOfChar = new char[65536];
            int j = localFileReader.read(arrayOfChar);
            try {
                this.sysVariable.setValue(str2, String.valueOf(arrayOfChar, 0, j));
            } catch (Exception localException) {
                this._stdout.print(localException);
            }
            localFileReader.close();
        } catch (IOException localIOException) {
            this._stdout.print(localIOException);
        }
    }

    private void procHelp(String paramString) {
        this._stdout.println("Usage: HELP");
        this._stdout.println();
        this._stdout.println(" CONNECT VAR UNVAR PRINT DEFINE SQLSET SPOOL READ");
        this._stdout.println(" @ @@ LOB LOBEXP");
    }

    private void procHost(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[19]).size();
        String str = skipWord(paramString, i);
        str = this.sysVariable.parseString(str.trim());
        if (str.length() > 0)
            try {
                host(str);
            } catch (IOException localIOException) {
                this._stdout.print(localIOException);
            }
    }

    private void procAutocommit(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[22]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        if (!isConnected()) {
            this._stdout.println("Database not connected.");
            return;
        }
        if (str.length() == 0) {
            try {
                this.autoCommit = this.database.getAutoCommit();
                this._stdout.println("Autocommit : " + (this.autoCommit ? "ON" : "OFF"));
            } catch (SQLException localSQLException1) {
                this._stdout.print(localSQLException1);
            }
            return;
        }
        this.autoCommit = "ON".equalsIgnoreCase(str);
        try {
            this.database.setAutoCommit(this.autoCommit);
            this.autoCommit = this.database.getAutoCommit();
        } catch (SQLException localSQLException2) {
            this._stdout.print(localSQLException2);
        }
    }

    private void procUse(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[21]).size();
        String str = skipWord(paramString, i);
        if (!isConnected()) {
            this._stdout.println("Database not connected.");
            return;
        }
        if (str.trim().length() == 0) {
            this._stdout.println("Usage: USE dbname");
            return;
        }
        try {
            this.database.setCatalog(str.trim());
        } catch (SQLException localSQLException) {
            this._stdout.print(localSQLException);
        }
    }

    private boolean procRun2(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLMultiple()[0]).size();
        String str1 = skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            this._stdout.println("Usage: @[@] file");
            return false;
        }
        String str2 = this.sysVariable.parseString(str1.trim());
        try {
            FileInputStream localFileInputStream = new FileInputStream(str2);
            Command localCommand = run(new InputCommandReader(localFileInputStream));
            return localCommand.COMMAND != null;
        } catch (IOException localIOException) {
            this._stdout.print(localIOException);
        }
        return false;
    }

    private void procSpoolOff(String paramString) {
        if (this._stdout.getLogFile() != null)
            this._stdout.getLogFile().close();
        this._stdout.setLogFile(null);
    }

    private void procDefine(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[12]).size();
        String str1 = skipWord(paramString, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            this._stdout.println("Usage:");
            this._stdout.println("  DEFINE variable=value");
            this._stdout.println("  SET    PAGESIZE   pagesize");
            this._stdout.println("  SET    DEBUGLEVEL debuglevel");
            this._stdout.println("  SET    HEADING    {OFF|ON}");
            this._stdout.println("  SET    DELIMITER  delimiter");
            this._stdout.println("  SET    TIMING     {ON|OFF}");
            return;
        }
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.trim().length() == 0) {
            this._stdout.println("Usage:");
            this._stdout.println("  DEFINE variable=value");
            this._stdout.println("  SET    PAGESIZE   pagesize");
            this._stdout.println("  SET    DEBUGLEVEL debuglevel");
            this._stdout.println("  SET    HEADING    {OFF|ON}");
            this._stdout.println("  SET    DELIMITER  delimiter");
            this._stdout.println("  SET    TIMING     {ON|OFF}");
            return;
        }
        if (!this.sysVariable.exists(str2.trim())) {
            this._stdout.println("Variable " + str2 + " not declared.");
            return;
        }
        try {
            if (str3.length() == 0)
                this.sysVariable.setValue(str2, null);
            else
                this.sysVariable.setValue(str2, str3);
        } catch (Exception localException) {
            this._stdout.print("Invalid format : ");
            this._stdout.println(str3);
        }
    }

    private void procSQLSet(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[12]).size();
        String str1 = skipWord(paramString, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            this._stdout.println("Usage: SQLSET variable=query");
            return;
        }
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.trim().length() == 0) {
            this._stdout.println("Usage: SQLSET variable=query");
            return;
        }
        if (!this.sysVariable.exists(str2.trim())) {
            this._stdout.println("Variable " + str2 + " not declared.");
            return;
        }
        if (!isConnected()) {
            this._stdout.println("Database not connected.");
            return;
        }
        try {
            DBRowCache localDBRowCache = executeQuery(this.database, str3, this.sysVariable, 1000);
            if ((localDBRowCache.getRowCount() == 1) && (localDBRowCache.getColumnCount() == 1))
                try {
                    this.sysVariable.setValue(str2, localDBRowCache.getItem(1, 1));
                } catch (Exception localException) {
                    this._stdout.print("Invalid format : ");
                    this._stdout.println(str3);
                }
            else
                this._stdout.println("Query does not return one row and one column.");
        } catch (SQLException localSQLException) {
            this._stdout.print(localSQLException);
        }
    }

    private void procHeading(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[3]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        boolean bool = "ON".equalsIgnoreCase(str);
        this._stdout.setHeading(bool);
        this._stdout.println("Heading set to : " + (bool ? "ON" : "OFF"));
    }

    private void procDelimiter(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[4]).size();
        String str = skipWord(paramString, i);
        str = str.trim();
        if ("TAB".equalsIgnoreCase(str))
            str = "\t";
        this._stdout.setSeperator(str.length() == 0 ? " " : str);
        this._stdout.println("Delimiter set to : " + (str.length() == 0 ? "SPACE" : str));
    }

    private void procVariable(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[9]).size();
        String str2 = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length < 2) {
            this._stdout.println("Usage:");
            this._stdout.println("  VAR varname vartype");
            this._stdout.println("");
            this._stdout.println("  CHAR VARCHAR LONGVARCHAR BINARY VARBINARY LONGVARBINARY");
            this._stdout.println("  NUMERIC DECIMAL BIT TINYINT SMALLINT INTEGER BIGINT REAL");
            this._stdout.println("  FLOAT DOUBLE DATE TIME TIMESTAMP BLOB CLOB");
            return;
        }
        if (this.sysVariable.exists(arrayOfString[0])) {
            this._stdout.println("Variable " + arrayOfString[0] + " already defined.");
            return;
        }
        this.sysVariable.add(arrayOfString[0], SQLTypes.getTypeID(arrayOfString[1]));
        this._stdout.println("Variable " + arrayOfString[0] + " created.");
    }

    private void procAutotrace(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[5]).size();
        String str = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        if (arrayOfString.length == 0) {
            this._stdout.println("Usage: SET AUTOTRACE {ON | OFF}");
            return;
        }
        if ((!"ON".equalsIgnoreCase(arrayOfString[0])) && (!"OFF".equalsIgnoreCase(arrayOfString[0]))) {
            this._stdout.println("Usage: SET AUTOTRACE {ON | OFF}");
            return;
        }
        this._stdout.setAutoTrace(arrayOfString[0].equalsIgnoreCase("ON"));
        this._stdout.println("Autotrace set to : " + str);
    }

    private void procUnvariable(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[10]).size();
        String str2 = skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length == 0) {
            this._stdout.println("Usage: UNVAR varname");
            return;
        }
        if (!this.sysVariable.exists(arrayOfString[0])) {
            this._stdout.println("Variable " + arrayOfString[0] + " not exist.");
            return;
        }
        this.sysVariable.remove(arrayOfString[0]);
        this._stdout.println("Variable " + arrayOfString[0] + " removed.");
    }

    private void procPrint(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(this._cmdtype.getASQLSingle()[11]).size();
        String str2 = skipWord(paramString, i);
        this._stdout.println(this.sysVariable.parseString(str2));
    }

    public void procUnknownCommand(Command paramCommand) {
        this._stdout.println("Unknown command!");
    }

    private void procLOB(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLMultiple()[0]).size();
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
                this._stdout.println("Usage:");
                this._stdout.println("  LOB query >> file");
                this._stdout.println("  LOB query << file");
                this._stdout.println("Note :");
                this._stdout.println("  >> mean export long/long raw/blob/clob to a file ");
                this._stdout.println("  << mean import a file to blob/clob field, the query");
                this._stdout.println("     should include the for update clause");
            }
        }
    }

    private void procLOBREAD(String paramString1, String paramString2) {
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            this._stdout.println("Java VM 1.3 or above required to support this feature.");
            return;
        }
        if ((paramString2 == null) || (paramString1 == null) || (paramString2.length() == 0) || (paramString1.length() == 0)) {
            this._stdout.println("Usage:");
            this._stdout.println("  LOB query >> file");
            this._stdout.println("  LOB query << file");
            this._stdout.println("Note :");
            this._stdout.println("  >> mean export long/long raw/blob/clob to a file ");
            this._stdout.println("  << mean import a file to blob/clob field, the query");
            this._stdout.println("     should include the for update clause");
            return;
        }
        paramString2 = this.sysVariable.parseString(paramString2);
        if (!isConnected()) {
            this._stdout.println("Database not connected.");
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
                Object localObject3;
                Object localObject5;

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
                            this._stdout.print(localIOException1);
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
                            this._stdout.print(localIOException2);
                        }
                    }
                } else {

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
                                    this._stdout.print(localIOException3);
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
                                    this._stdout.print(localIOException4);
                                }
                            }
                        }
                    }
                }
                this._stdout.println("Command succeed.");
            } else {
                this._stdout.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            this._stdout.print(localException);
        }
        clearWarnings(this.database, this._stdout);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this._stdout.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this._stdout.print(localSQLException2);
        }
    }

    private void procLOBWRITE(String paramString1, String paramString2) {
        long l1 = 0L;
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 4)) {
            this._stdout.println("Java VM 1.4 or above required to support this feature.");
            return;
        }
        if (((paramString2 == null) && (paramString1 == null)) || (paramString2.length() == 0) || (paramString1.length() == 0)) {
            this._stdout.println("Usage:");
            this._stdout.println("  LOB query >> file");
            this._stdout.println("  LOB query << file");
            this._stdout.println("Note :");
            this._stdout.println("  >> mean export long/long raw/blob/clob to a file ");
            this._stdout.println("  << mean import a file to blob/clob field, the query");
            this._stdout.println("     should include the for update clause");
            return;
        }
        paramString2 = this.sysVariable.parseString(paramString2);
        if (!isConnected()) {
            this._stdout.println("Database not connected.");
            return;
        }
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        File localFile = new File(paramString2);
        if (!localFile.exists()) {
            this._stdout.println("File " + paramString2 + " does not exists!");
            return;
        }
        if (!localFile.isFile()) {
            this._stdout.println(paramString2 + " is not a valid file!");
            return;
        }
        if (!localFile.canRead()) {
            this._stdout.println("Cannot read file " + paramString2 + "!");
            return;
        }
        try {
            localSQLStatement = prepareStatement(this.database, paramString1, this.sysVariable);
            localSQLStatement.bind(this.sysVariable);
            localResultSet = localSQLStatement.stmt.executeQuery();
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if (localResultSet.next()) {
                Object localObject1;
                Object localObject2;
                Object localObject3;
                if (localResultSetMetaData.getColumnType(1) == 2005) {
                    localObject1 = localResultSet.getClob(1);
                    long l2 = 0L;
                    if (localObject1 != null) {
                        char[] arrayOfChar = new char[8192];
                        try {
                            int i = 0;
                            ((Clob) localObject1).truncate(l1);
                            localObject2 = ((Clob) localObject1).setCharacterStream(l1);
                            localObject3 = new FileReader(localFile);
                            while ((i = ((FileReader) localObject3).read(arrayOfChar)) > 0) {
                                ((Writer) localObject2).write(arrayOfChar, 0, i);
                                l2 += i;
                            }
                            ((FileReader) localObject3).close();
                            ((Writer) localObject2).close();
                        } catch (IOException localIOException1) {
                            this._stdout.print(localIOException1);
                        }
                    }
                } else if (localResultSetMetaData.getColumnType(1) == 2004) {
                    localObject1 = localResultSet.getBlob(1);
                    if (localObject1 != null) {
                        byte[] arrayOfByte = new byte[8192];
                        long l3 = 0L;
                        try {
                            int j = 0;
                            ((Blob) localObject1).truncate(l1);
                            localObject2 = ((Blob) localObject1).setBinaryStream(l1);
                            localObject3 = new FileInputStream(localFile);
                            while ((j = ((FileInputStream) localObject3).read(arrayOfByte)) > 0) {
                                ((OutputStream) localObject2).write(arrayOfByte, 0, j);
                                l3 += j;
                            }
                            ((FileInputStream) localObject3).close();
                            ((OutputStream) localObject2).close();
                        } catch (IOException localIOException2) {
                            this._stdout.print(localIOException2);
                        }
                    }
                }
                this._stdout.println("Command succeed.");
            } else {
                this._stdout.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            this._stdout.print(localException);
        }
        clearWarnings(this.database, this._stdout);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this._stdout.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this._stdout.print(localSQLException2);
        }
    }

    private void procLOBEXP(String paramString) {
        int i = TextUtils.getWords(this._cmdtype.getASQLMultiple()[1]).size();
        String str1 = skipWord(paramString, i);
        str1 = str1.trim();
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            this._stdout.println("Java VM 1.3 required to support this feature.");
            return;
        }
        if (str1.length() == 0) {
            this._stdout.println("Usage:");
            this._stdout.println("  LOBEXP query");
            this._stdout.println("Note :");
            this._stdout.println("  Query should return tow column as following:");
            this._stdout.println("  col1 : CHAR or VARCHAR specify the filename.");
            this._stdout.println("  col2 : long/long raw/blob/clob field.");
            return;
        }
        if (!isConnected()) {
            this._stdout.println("Database not connected.");
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
                this._stdout.println("Usage:");
                this._stdout.println("  LOBEXP query");
                this._stdout.println("Note :");
                this._stdout.println("  Query should return tow column as following:");
                this._stdout.println("  col1 : CHAR or VARCHAR specify the filename.");
                this._stdout.println("  col2 : long/long raw/blob/clob field.");
                try {
                    if (localResultSet != null)
                        localResultSet.close();
                } catch (SQLException localSQLException3) {
                    this._stdout.print(localSQLException3);
                }
                try {
                    if (localSQLStatement != null)
                        localSQLStatement.close();
                } catch (SQLException localSQLException4) {
                    this._stdout.print(localSQLException4);
                }
                return;
            }
            while (localResultSet.next()) {
                String str2 = localResultSet.getString(1);
                if (str2 == null)
                    this._stdout.println("The file name is null!");
                else
                    this._stdout.println("Export lob data to file: " + str2);
                File localFile = new File(str2);
                Object localObject4;
                Object localObject2;
                Object localObject1;
                Object localObject3;
                Object localObject5;
                if (localResultSetMetaData.getColumnType(2) == -1) {
                    localObject1 = localResultSet.getCharacterStream(2);
                    if (localObject1 == null)
                        continue;
                    localObject2 = new char[8192];
                    try {
                        int j = 0;
                        localObject4 = new FileWriter(localFile);
                        while ((j = ((Reader) localObject1).read((char[]) localObject2)) > 0)
                            ((FileWriter) localObject4).write((char[]) localObject2, 0, j);
                        ((FileWriter) localObject4).close();
                        ((Reader) localObject1).close();
                    } catch (IOException localIOException1) {
                        this._stdout.print(localIOException1);
                    }
                    continue;
                }
                if (localResultSetMetaData.getColumnType(2) == -4) {
                    localObject1 = localResultSet.getBinaryStream(2);
                    if (localObject1 == null)
                        continue;
                    localObject2 = new byte[8192];
                    try {
                        int k = 0;
                        localObject4 = new FileOutputStream(localFile);
                        while ((k = ((InputStream) localObject1).read((byte[]) localObject2)) > 0)
                            ((FileOutputStream) localObject4).write((byte[]) localObject2, 0, k);
                        ((FileOutputStream) localObject4).close();
                        ((InputStream) localObject1).close();
                    } catch (IOException localIOException2) {
                        this._stdout.print(localIOException2);
                    }
                    continue;
                }

                if (localResultSetMetaData.getColumnType(2) == 2005) {
                    localObject1 = localResultSet.getClob(2);
                    if (localObject1 == null)
                        continue;
                    localObject2 = ((Clob) localObject1).getCharacterStream();
                    if (localObject2 == null)
                        continue;
                    localObject3 = new char[8192];
                    try {
                        int m = 0;
                        localObject5 = new FileWriter(localFile);
                        while ((m = ((Reader) localObject2).read((char[]) localObject3)) > 0)
                            ((FileWriter) localObject5).write((char[]) localObject3, 0, m);
                        ((FileWriter) localObject5).close();
                        ((Reader) localObject2).close();
                    } catch (IOException localIOException3) {
                        this._stdout.print(localIOException3);
                    }
                    continue;
                }
                if (localResultSetMetaData.getColumnType(2) != 2004)
                    continue;
                localObject1 = localResultSet.getBlob(2);
                if (localObject1 == null)
                    continue;
                localObject2 = ((Blob) localObject1).getBinaryStream();
                if (localObject2 == null)
                    continue;
                localObject3 = new byte[8192];
                try {
                    int n = 0;
                    localObject5 = new FileOutputStream(localFile);
                    while ((n = ((InputStream) localObject2).read((byte[]) localObject3)) > 0)
                        ((FileOutputStream) localObject5).write((byte[]) localObject3, 0, n);
                    ((FileOutputStream) localObject5).close();
                    ((InputStream) localObject2).close();
                } catch (IOException localIOException4) {
                    this._stdout.print(localIOException4);
                }
            }
            this._stdout.println("Command succeed.");
        } catch (Exception localException) {
            this._stdout.print(localException);
        }
        clearWarnings(this.database, this._stdout);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            this._stdout.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            this._stdout.print(localSQLException2);
        }
    }

    public void showVersion() {
        this._stdout.println();
        this._stdout.println(" AnySQL for MySQL, version 1.0.0 -- " + DateOperator.getDay("yyyy-MM-dd HH:mm:ss"));
        this._stdout.println();
        this._stdout.println(" (@) Copyright Lou Fangxin, all rights reserved.");
        this._stdout.println();
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
        int m = 0;
        for (m = 0; m < k; m++) {
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
            for (m = 1; m <= k; m++) {
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

    public int fetch(ResultSet paramResultSet, DBRowCache paramDBRowCache)
            throws SQLException {
        return fetch(paramResultSet, paramDBRowCache, 100);
    }

    public int fetch(ResultSet paramResultSet, DBRowCache paramDBRowCache, int paramInt)
            throws SQLException {
        int i = 0;
        int j = 0;
        int k = 0;
        byte[] arrayOfByte1 = new byte[8192];
        char[] arrayOfChar1 = new char[4096];
        byte[] arrayOfByte2 = new byte[65536];
        char[] arrayOfChar2 = new char[65536];
        Object localObject2;
        if (paramDBRowCache.getColumnCount() == 0) {
            localObject2 = paramResultSet.getMetaData();
            for (i = 1; i <= ((ResultSetMetaData) localObject2).getColumnCount(); i++)
                if (((ResultSetMetaData) localObject2).getColumnName(i) != null) {
                    if (paramDBRowCache.findColumn(((ResultSetMetaData) localObject2).getColumnName(i)) == 0) {
                        paramDBRowCache.addColumn(((ResultSetMetaData) localObject2).getColumnName(i), ((ResultSetMetaData) localObject2).getColumnType(i));
                    } else {
                        for (j = 1; paramDBRowCache.findColumn(((ResultSetMetaData) localObject2).getColumnName(i) + "_" + j) != 0; j++)
                            ;
                        paramDBRowCache.addColumn(((ResultSetMetaData) localObject2).getColumnName(i) + "_" + j, ((ResultSetMetaData) localObject2).getColumnType(i));
                    }
                } else {
                    for (j = 1; paramDBRowCache.findColumn("NULL" + j) != 0; j++) ;
                    paramDBRowCache.addColumn("NULL" + j, ((ResultSetMetaData) localObject2).getColumnType(i));
                }
        }
        if (paramDBRowCache.getColumnCount() == 0)
            return 0;
        Object[] arrayOfObject;
        for (i = paramDBRowCache.getRowCount(); (i < paramInt) && (paramResultSet.next()); i = paramDBRowCache.appendRow(arrayOfObject)) {
            arrayOfObject = new Object[paramDBRowCache.getColumnCount()];
            for (j = 1; j <= paramDBRowCache.getColumnCount(); j++) {
                Object localObject1 = null;
                int m;
                Object localObject3;
                Object localObject4;
                switch (paramDBRowCache.getColumnType(j)) {
                    case -1:
                        localObject2 = paramResultSet.getCharacterStream(j);
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
                        InputStream localInputStream1 = paramResultSet.getBinaryStream(j);
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
                        Clob localClob = paramResultSet.getClob(j);
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
                        localObject3 = paramResultSet.getBlob(j);
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
                        localObject4 = paramResultSet.getCharacterStream(j);
                        if (localObject4 == null)
                            break;
                        try {
                            m = ((Reader) localObject4).read(arrayOfChar1);
                            if (paramDBRowCache.getColumnType(j) == 1)
                                while ((m > 0) && (arrayOfChar1[(m - 1)] == ' '))
                                    m--;
                            if (m > 0)
                                localObject1 = String.valueOf(arrayOfChar1, 0, m);
                            ((Reader) localObject4).close();
                        } catch (IOException localIOException5) {
                        }
                    case -3:
                    case -2:
                        InputStream localInputStream2 = paramResultSet.getAsciiStream(j);
                        if (localInputStream2 == null)
                            break;
                        try {
                            m = localInputStream2.read(arrayOfByte1);
                            if (paramDBRowCache.getColumnType(j) == -2)
                                while ((m > 0) && (arrayOfByte1[(m - 1)] == 32))
                                    m--;
                            if (m > 0)
                                localObject1 = new String(arrayOfByte1, 0, m);
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                    case 91:
                        localObject1 = paramResultSet.getDate(j);
                        break;
                    case 92:
                        localObject1 = paramResultSet.getTime(j);
                        break;
                    case -102:
                    case -101:
                    case 93:
                        localObject1 = paramResultSet.getTimestamp(j);
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
                        localObject1 = paramResultSet.getObject(j);
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
                        localObject1 = paramResultSet.getString(j);
                }
                arrayOfObject[(j - 1)] = localObject1;
            }
        }
        return i;
    }
}

