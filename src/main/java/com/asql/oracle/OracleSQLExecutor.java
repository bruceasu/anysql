package com.asql.oracle;

import static com.asql.core.CMDType.ASQL_CANCEL;
import static com.asql.core.CMDType.ASQL_COMMENT;
import static com.asql.core.CMDType.ASQL_DBCOMMAND;
import static com.asql.core.CMDType.ASQL_END;
import static com.asql.core.CMDType.ASQL_EXIT;
import static com.asql.core.CMDType.ASQL_MULTIPLE;
import static com.asql.core.CMDType.ASQL_SINGLE;
import static com.asql.core.CMDType.ASQL_SQLFILE;
import static com.asql.core.CMDType.MULTI_COMMENT_END;
import static com.asql.core.CMDType.MULTI_COMMENT_START;
import static com.asql.core.CMDType.NULL_COMMAND;
import static com.asql.core.CMDType.SQL_BLOCK;
import static com.asql.core.CMDType.SQL_CALL;
import static com.asql.core.CMDType.SQL_DDL;
import static com.asql.core.CMDType.SQL_DML;
import static com.asql.core.CMDType.SQL_QUERY;
import static com.asql.core.CMDType.SQL_SCRIPT;
import static com.asql.core.CMDType.UNKNOWN_COMMAND;
import static com.asql.oracle.OracleCMDType.*;

import com.asql.core.*;
import com.asql.core.io.CommandReader;
import com.asql.core.io.InputCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVM;
import com.asql.core.util.TextUtils;
import com.asql.oracle.invoker.*;
import java.io.*;
import java.math.BigInteger;
import java.sql.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class OracleSQLExecutor extends DefaultSQLExecutor {
    public VariableTable tnsnames = new VariableTable();
    public Command lastcommand = null;
    public String autotraceType = "OFF";
    public String autotraceName = "ALL";
    public String oracleDriver = "THIN";
    public SQLStatement[] descSqls = new SQLStatement[10];
    public SQLStatement[] traceSqls = new SQLStatement[9];
    public CheckNet[] checkNets = new CheckNet[50];
    public Hashtable hashtable = new Hashtable();
    public int scanLimit = 4096;
    public String lastObjectType = null;
    public String lastObjectOwner = null;
    public String lastObjectName = null;
    public DBRowCache loadBuffer = new SimpleDBRowCache();

    Map<Integer, ModuleInvoker> invokers = new Hashtable<>();

    public OracleSQLExecutor() {
        super(new OracleCMDType());
        Properties localProperties = System.getProperties();
        String os = localProperties.getProperty("os.name");
        String oracleHome = localProperties.getProperty("ORACLE_HOME");
        loadTNSNames("tnsnames.ora");
        loadTNSNames(JavaVM.JAVA_HOME + "/tnsnames.ora");
        if ((oracleHome != null) && (oracleHome.length() > 0)) {
            String tnsNamesPath = null;
            if (os.toUpperCase().startsWith("WINDOWS"))
                tnsNamesPath = oracleHome + "\\network\\admin\\tnsnames.ora";
            else
                tnsNamesPath = oracleHome + "/network/admin/tnsnames.ora";
            loadTNSNames(tnsNamesPath);
        }
        init();
    }

    public OracleSQLExecutor(CommandReader paramCommandReader, CommandLog paramCommandLog) {
        super(new OracleCMDType(), paramCommandReader, paramCommandLog);
        Properties localProperties = System.getProperties();
        String str1 = localProperties.getProperty("os.name");
        String str2 = localProperties.getProperty("ORACLE_HOME");
        loadTNSNames("tnsnames.ora");
        if ((str2 != null) && (str2.length() > 0)) {
            String str3 = null;
            if (str1.toUpperCase().startsWith("WINDOWS"))
                str3 = str2 + "\\network\\admin\\tnsnames.ora";
            else
                str3 = str2 + "/network/admin/tnsnames.ora";
            loadTNSNames(str3);
        }
        init();
    }

    private void init() {
        invokers.put(ASQL_SINGLE,  new SingleInvoker(this));
        invokers.put(ASQL_MULTIPLE, new MutipleInvoker(this));
        invokers.put(SQL_QUERY, new SQLInvoker(this));
        invokers.put(SQL_DML, new SQLInvoker(this));
        invokers.put(SQL_DDL, new SQLInvoker(this));
        invokers.put(SQL_BLOCK, new SQLInvoker(this));
        invokers.put(SQL_SCRIPT, new ScriptInvoker(this));
        invokers.put(SQL_CALL, new CallInvoker(this));
        invokers.put(ASQL_SQLFILE, new SQLFileInvoker(this));
        invokers.put(ASQL_DBCOMMAND, new DBCommandInvoker(this));
    }
    @Override
    public final boolean execute(Command paramCommand) {
        if (paramCommand == null) {
            out.println("No command to execute.");
            return true;
        }
        switch (paramCommand.TYPE1) {
            case SQL_QUERY:
            case SQL_DML:
            case SQL_DDL:
            case SQL_BLOCK:
                if (checkNotConnected()) return true;
                invokers.get(SQL_QUERY).invoke(paramCommand);
                lastcommand = paramCommand;
                break;
            case SQL_SCRIPT:
                if (checkNotConnected()) return true;
                invokers.get(SQL_SCRIPT).invoke(paramCommand);
                lastcommand = paramCommand;
                break;
            case ASQL_END:
                execute(lastcommand);
                break;
            case SQL_CALL:
                if (checkNotConnected()) return true;
                invokers.get(SQL_CALL).invoke(paramCommand);
                break;
            case ASQL_SQLFILE:
                if (checkNotConnected()) return true;
                boolean x =invokers.get(ASQL_SQLFILE).invoke(paramCommand);
                if (!x) {
                    return x;
                }
                break;
            case ASQL_MULTIPLE:
                invokers.get(ASQL_MULTIPLE).invoke(paramCommand);
                break;
            case ASQL_DBCOMMAND:
                Boolean x1 =   invokers.get(ASQL_DBCOMMAND).invoke(paramCommand);
                if (x1 != null) return x1;
                break;
            case ASQL_SINGLE:
                invokers.get(ASQL_SINGLE).invoke(paramCommand);
            case ASQL_EXIT:
            case ASQL_CANCEL:
            case UNKNOWN_COMMAND:
            case ASQL_COMMENT:
            case NULL_COMMAND:
            case MULTI_COMMENT_START:
            case MULTI_COMMENT_END:
        }
        return true;
    }

    @Override
    public void procDisabledCommand(Command paramCommand) {
        out.println("Query Only mode, DML/DDL/Script disabled.!");
    }

    @Override
    public void procUnknownCommand(Command paramCommand) {
        out.println("Unknown command!");
    }

    @Override
    public void showVersion() {
        out.println();
        out.println(" AnySQL for Oracle(8i/9i/10g), Release 3.0.0 (Build:20060816-1013)");
        out.println();
        out.println(" (@) Copyright Lou Fangxin 2004/2005, all rights reserved.");
        out.println();
    }

    @Override
    public void doServerMessage() throws SQLException {
        SQLCallable sqlCallable = null;
        VariableTable table = new VariableTable();
        table.add("P_LINE", 12);
        table.add("P_STATUS", 4);
        table.setValue("P_STATUS", "0");
        sqlCallable = prepareCall(database,
                "DBMS_OUTPUT.GET_LINE(LINE=>:P_LINE OUT,STATUS=>:P_STATUS OUT)", table);
        while (true) {
            sqlCallable.bind(table);
            sqlCallable.stmt.execute();
            sqlCallable.fetch(table);
            if (table.getInt("P_STATUS", 1) == 1)
                break;
            out.println(table.getString("P_LINE"));
        }
        sqlCallable.close();
    }

    @Override
    public String getLastCommand() {
        if (lastcommand == null)
            return null;
        return lastcommand.COMMAND;
    }

    @Override
    public int fetch(ResultSet paramResultSet, DBRowCache paramDBRowCache)
            throws SQLException {
        return fetch(paramResultSet, paramDBRowCache, 100);
    }

    @Override
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
                        localObject1 = paramResultSet.getTimestamp(j);
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

    public void loadTNSNames(String path) {
        try(BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path)))) {
            loadTNSNames(localBufferedReader);
        } catch (IOException ignore) {
        }
    }

    public void loadTNSNames(BufferedReader reader) {
        int i = 0;
        int j = 0;
        String str1 = "";
        String str2 = "";
        try {
            if (reader == null)
                return;
            while ((str1 = reader.readLine()) != null) {
                if ((str1.trim().length() > 0) && (str1.trim().substring(0, 1).equals("#")))
                    continue;
                j = 0;
                if ((str1.length() > 0)
                        && ((str1.trim().startsWith("(")) || (str1.trim().startsWith(")")))) {
                    if (str2.length() > 0) {
                        str2 = str2 + str1.trim();
                        continue;
                    }
                    str2 = str1.trim();
                    continue;
                }
                j = str2.indexOf("=");
                if (j > 0) {
                    tnsnames.addFromString(str2, j);
                }
                if (str1.length() > 0) {
                    str2 = str1.trim();
                    continue;
                }
                str2 = "";
            }
            if (str2.length() > 0) {
                j = str2.indexOf("=");
                if (j > 0) {
                    tnsnames.addFromString(str2, j);
                }
            }
        } catch (IOException localIOException1) {
        }
    }

    public void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Throwable ignore) {
        }
    }
    
    public void getObjectFromCommand(String paramString) {
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(paramString));
        int i = 0;
        lastObjectType = null;
        lastObjectOwner = null;
        lastObjectName = null;
        while ((arrayOfString[i].equalsIgnoreCase("ALTER"))
                || (arrayOfString[i].equalsIgnoreCase("CREATE"))
                || (arrayOfString[i].equalsIgnoreCase("OR"))
                || (arrayOfString[i].equalsIgnoreCase("REPLACE"))
                || (arrayOfString[i].equalsIgnoreCase("PUBLIC"))
                || (arrayOfString[i].equalsIgnoreCase("DROP")))
            i++;
        if ((i < arrayOfString.length) && (i > 0)) {
            if ((arrayOfString[i].equalsIgnoreCase("PACKAGE"))
                    || (arrayOfString[i].equalsIgnoreCase("TYPE"))) {
                i++;
                if (i < arrayOfString.length)
                    if (arrayOfString[i].equalsIgnoreCase("BODY")) {
                        lastObjectType = (arrayOfString[(i - 1)].toUpperCase() + " BODY");
                    } else {
                        lastObjectType = arrayOfString[(i - 1)].toUpperCase();
                        i--;
                    }
            } else {
                lastObjectType = arrayOfString[i].toUpperCase();
            }
            i++;
        }
        if ((i < arrayOfString.length) && (i > 0)) {
            String str = arrayOfString[i].toUpperCase();
            i = str.indexOf("(");
            if (i > 0)
                str = str.substring(0, i);
            i = str.indexOf(".");
            if (i > 0) {
                lastObjectOwner = str.substring(0, i);
                lastObjectName = str.substring(i + 1);
            } else {
                lastObjectName = str;
            }
        }
    }

    public DBRowCache getSessionWait() {
        DBRowCache localDBRowCache = null;
        try {
            if (traceSqls[3] != null)
                localDBRowCache = executeQuery(traceSqls[3], sysVariable, 2000);
        } catch (SQLException localSQLException) {
            out.print(localSQLException);
        }
        return localDBRowCache;
    }

    public DBRowCache getDbRowCacheOfSessionWait() {
        DBRowCache rowCache = null;
        if ((!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("EVENT".equalsIgnoreCase(autotraceName))))
            rowCache = getSessionWait();
        return rowCache;
    }

    public void printSessionWait(DBRowCache rowCacheStart) {
        DBRowCache rowCacheEnd;
        if ((!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("EVENT".equalsIgnoreCase(autotraceName)))) {
            rowCacheEnd = getSessionWait();
            printSessionWaits(rowCacheStart, rowCacheEnd);
        }
    }

    public void printSessionWaits(DBRowCache rowCache1, DBRowCache rowCache2) {
        if ((rowCache1 == null) || (rowCache2 == null))
            return;
        if (rowCache1.getRowCount() == 0)
            return;
        if (rowCache1.getRowCount() == rowCache2.getRowCount()) {
            out.println();
            out.println("Waiting Event");
            out.println("  Waits     Time  Event");
            out.println("-----------------------------------------------------");
            for (int i = 1; i <= rowCache1.getRowCount(); i++) {
                String str = rowCache1.getString(i, 1);
                BigInteger bi1 = new BigInteger(rowCache1.getItem(i, 2).toString());
                BigInteger bi3 = new BigInteger(rowCache1.getItem(i, 3).toString());
                BigInteger bi2 = new BigInteger(rowCache2.getItem(i, 2).toString());
                BigInteger bi4 = new BigInteger(rowCache2.getItem(i, 3).toString());
                BigInteger bi5 = bi2.add(bi1.negate());
                BigInteger bi6 = bi4.add(bi3.negate());
                if ((bi6.longValue() <= 0L) || (!str.equals(rowCache2.getItem(i, 1))))
                    continue;
                out.print(rpad(bi5.longValue() + " ", 8));
                out.print(rpad(bi6.longValue() + "  ", 10));
                out.println((String) rowCache1.getItem(i, 1));
            }
        }
    }

    public DBRowCache getSessionStats() {
        DBRowCache localDBRowCache = null;
        try {
            if (traceSqls[0] != null)
                localDBRowCache = executeQuery(traceSqls[0], sysVariable, 1000);
            return localDBRowCache;
        } catch (SQLException localSQLException) {
        }
        return null;
    }

    public DBRowCache getDbRowCacheOfSessionStats() {
        DBRowCache rowCache = null;
        if ((!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("STATISTICS".equalsIgnoreCase(autotraceName))
                || ("STATS".equalsIgnoreCase(autotraceName))))
            rowCache = getSessionStats();
        return rowCache;
    }

    public void printSessionStats(DBRowCache rowCacheStart) {
        DBRowCache rowCacheEnd;
        if ((!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("STATISTICS".equalsIgnoreCase(autotraceName))
                || ("STATS".equalsIgnoreCase(autotraceName)))) {
            rowCacheEnd = getSessionStats();
            printSessionStats(rowCacheStart, rowCacheEnd);
        }
    }

    public void printSessionStats(DBRowCache rowCache1, DBRowCache rowCache2) {
        if ((rowCache1 == null) || (rowCache2 == null))
            return;
        if (rowCache1.getRowCount() == 0)
            return;
        if (rowCache1.getRowCount() == rowCache2.getRowCount()) {
            out.println();
            out.println("Statistics");
            out.println("---------------------------------------------------");
            for (int i = 1; i <= rowCache1.getRowCount(); i++) {
                Object localObject1 = rowCache1.getItem(i, 1);
                Object localObject2 = rowCache2.getItem(i, 1);
                BigInteger bi1 = new BigInteger(rowCache1.getItem(i, 2).toString());
                BigInteger bi2 = new BigInteger(rowCache2.getItem(i, 2).toString());
                BigInteger bi3 = bi2.add(bi1.negate());
                if ((bi3.longValue() <= 0L) || (!localObject1.equals(localObject2)))
                    continue;
                if (bi3.longValue() > 10000000L)
                    out.println(rpad(new StringBuilder().append(" ").append(bi3.longValue() / 1000000L).append("M").toString(), 12) + "  " + hashtable.get(localObject1).toString());
                else if (bi3.longValue() > 10000L)
                    out.println(rpad(new StringBuilder().append(" ").append(bi3.longValue() / 1000L).append("K").toString(), 12) + "  " + hashtable.get(localObject1).toString());
                else
                    out.println(rpad(new StringBuilder().append(" ").append(bi3.longValue()).toString(), 12) + "  " + hashtable.get(localObject1).toString());
            }
        }
    }

    public DBRowCache getSystemEvent() {
        DBRowCache localDBRowCache = null;
        try {
            if (traceSqls[5] != null)
                localDBRowCache = executeQuery(traceSqls[5], sysVariable, 2000);
        } catch (SQLException localSQLException) {
        }
        return localDBRowCache;
    }

    public final DBRowCache getExplainPlan(String paramString) {
        DBRowCache rowCache = null;
        String str = "P" + Math.random();
        SQLStatement statement = null;
        try {
            sysVariable.add("PLAN_STMT_ID", 12);
            sysVariable.setValue("PLAN_STMT_ID", str);
            if (traceSqls[6] != null) {
                traceSqls[6].bind(sysVariable);
                traceSqls[6].stmt.execute();
            }
            statement = prepareScript(database, "EXPLAIN PLAN SET STATEMENT_ID='" + str + "' FOR " + paramString, sysVariable);
            statement.stmt.execute();
            if (traceSqls[7] != null)
                rowCache = executeQuery(traceSqls[7], sysVariable, 1000);
            if (traceSqls[6] != null) {
                traceSqls[6].bind(sysVariable);
                traceSqls[6].stmt.execute();
            }
            sysVariable.remove("PLAN_STMT_ID");
        } catch (SQLException localSQLException1) {
            out.print(localSQLException1);
        } finally {
            closeQuietly(statement);
        }
        return rowCache;
    }

    public void printExplain(Command paramCommand) {
        if ((paramCommand.TYPE1 != SQL_BLOCK) && (paramCommand.TYPE1 != SQL_DDL)
                && (!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("EXPLAIN".equalsIgnoreCase(autotraceName))
                || ("EXP".equalsIgnoreCase(autotraceName)))) {
            DBRowCache localDBRowCache5 = getExplainPlan(paramCommand.COMMAND);
            if (localDBRowCache5 != null) {
                out.println();
                out.println("Execute Plan");
                showDBRowCache(localDBRowCache5, true);
            }
        }
    }

    public void printCost(long end, long start) {
        if (timing)
            out.println("Execute time: " + DBOperation.getElapsed(end - start));
    }

    public boolean checkNotConnected() {
        if (!isConnected()) {
            out.println("Database not connected.");
            return true;
        }
        return false;
    }

    public void showDBRowCache(DBRowCache paramDBRowCache, boolean paramBoolean) {
        paramDBRowCache.getWidth(false);
        if (paramBoolean) {
            out.println(paramDBRowCache.getFixedHeader());
            out.println(paramDBRowCache.getSeperator());
        }
        for (int i = 1; i <= paramDBRowCache.getRowCount(); i++)
            out.println(paramDBRowCache.getFixedRow(i));
    }

    public boolean procRun1(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[ORACLE_LOADTNS]).size();
        String str1 = skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: @[@] file");
            return false;
        }
        String path = sysVariable.parseString(str1.trim());
        File file = new File(path);
        try (FileInputStream stream = new FileInputStream(file);
             InputCommandReader reader = new InputCommandReader(stream)){
            reader.setWorkingDir(file.getParent());
            Command localCommand = run(reader);
            reader.close();
            return localCommand.COMMAND != null;
        } catch (IOException localIOException) {
            out.print(localIOException);
        }
        return false;
    }

    class TestProcessor {
        String str;
        String[] args;
        /*
            int i = TextUtils.getWords(cmdType.getASQLSingle()[0]).size(); // connect
            String str = skipWord(paramString, i);
            str = str.trim();

            String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str1));
         */
        public TestProcessor(String str, String[] args) {
            this.str = str;
            this.args = args;
        }
        public void stopConnectionTest() {
            for (int i = 0; i < checkNets.length; i++) {
                if (checkNets[i] == null)
                    continue;
                checkNets[i].stopCheck();
            }
        }

        void startConnectionTest() {
            String str1 = "";
            String str2 = "";
            String str3 = "";
            String str4 = "";
            Properties localProperties = DBConnection.getProperties("ORACLE");
            out.println("Start Ora*Net Checker ......!");
            if (args.length == 3) {
                if (("AS".equalsIgnoreCase(args[1]))
                        && (("SYSDBA".equalsIgnoreCase(args[2]))
                        || ("SYSOPER".equalsIgnoreCase(args[2]))))
                    localProperties.setProperty("internal_logon", args[2].toLowerCase());
                str1 = args[0];
            }

            int j = -1;
            j = str1.indexOf("@");
            if (j == -1) {
                str2 = str1;
                str4 = "";
            } else {
                str2 = str1.substring(0, j);
                str4 = str1.substring(j + 1);
            }
            j = str2.indexOf("/");
            if (j == -1)
                try {
                    str3 = in.readPassword();
                    str2 = str2 + "/" + str3.trim();
                } catch (Exception localException) {
                }
            if ((str4.length() > 0) && (tnsnames.exists(str4)) && (tnsnames.getValue(str4) != null))
                str4 = (String) tnsnames.getValue(str4);
            for (int k = 0; k < checkNets.length; k++) {
                if (checkNets[k] != null)
                    checkNets[k].stopCheck();
                checkNets[k] = new CheckNet(str2 + "@" + str4);
                checkNets[k].start();
            }
        }

    }

    class CheckNet extends Thread {
        boolean exit_loop = false;
        Connection db = null;
        String conninfo = null;

        public CheckNet(String arg2) {
            conninfo = arg2;
        }

        public void stopCheck() {
            exit_loop = true;
        }

        public void run() {
            Properties localProperties = DBConnection.getProperties("ORACLE");
            while (!exit_loop)
                try {
                    if (oracleDriver.equalsIgnoreCase("THIN"))
                        db = DBConnection.getConnection("ORACLE", conninfo, localProperties);
                    else
                        db = DBConnection.getConnection("ORAOCI", conninfo, localProperties);
                    try {
                        Thread.currentThread();
                        Thread.sleep(2000L);
                    } catch (InterruptedException localInterruptedException) {
                    }
                    db.close();
                } catch (SQLException localSQLException) {
                }
        }
    }
}
