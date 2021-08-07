package com.asql.pgsql;

import static com.asql.core.CMDType.*;

import com.asql.core.*;
import com.asql.core.io.CommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.TextUtils;
import com.asql.pgsql.invoker.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.sql.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class PgSqlSQLExecutor extends DefaultSQLExecutor {

    public Command        lastcommand     = null;
    public String         autotraceType   = "OFF";
    public String         autotraceName   = "ALL";
    public SQLStatement[] descSqls        = new SQLStatement[10];
    public SQLStatement[] traceSqls       = new SQLStatement[9];
    public Hashtable      hashtable       = new Hashtable();
    public int            scanLimit       = 4096;
    public String         lastObjectType  = null;
    public String         lastObjectOwner = null;
    public String         lastObjectName  = null;
    public DBRowCache     loadBuffer      = new SimpleDBRowCache();

    Map<Integer, ModuleInvoker> invokers = new Hashtable<>();

    public PgSqlSQLExecutor() {
        super(new PgSqlCMDType());
        init();
    }

    public PgSqlSQLExecutor(CommandReader paramCommandReader, CommandLog paramCommandLog) {
        super(new PgSqlCMDType(), paramCommandReader, paramCommandLog);
        Properties localProperties = System.getProperties();
        init();
    }

    private void init() {
        invokers.put(ASQL_SINGLE, new SingleInvoker(this));
        invokers.put(ASQL_MULTIPLE, new MutipleInvoker(this));
        invokers.put(SQL_QUERY, new SQLInvoker(this));
        invokers.put(SQL_DML, new SQLInvoker(this));
        invokers.put(SQL_DDL, new SQLInvoker(this));
        invokers.put(SQL_BLOCK, new SQLInvoker(this));
        invokers.put(SQL_SCRIPT, new ScriptInvoker(this));
        invokers.put(SQL_CALL, new CallInvoker(this));
        invokers.put(ASQL_SQL_FILE, new SQLFileInvoker(this));
        invokers.put(ASQL_DB_COMMAND, new DBCommandInvoker(this));
    }

    @Override
    public final boolean execute(Command cmd) {
        if (cmd == null) {
            out.println("No command to execute.");
            return true;
        }
        switch (cmd.type1) {
            case SQL_QUERY:
            case SQL_DML:
            case SQL_DDL:
            case SQL_BLOCK:
                if (checkNotConnected()) { return true; }
                invokers.get(SQL_QUERY).invoke(cmd);
                lastcommand = cmd;
                break;
            case SQL_SCRIPT:
                if (checkNotConnected()) { return true; }
                invokers.get(SQL_SCRIPT).invoke(cmd);
                lastcommand = cmd;
                break;
            case ASQL_END:
                execute(lastcommand);
                break;
            case SQL_CALL:
                if (checkNotConnected()) { return true; }
                invokers.get(SQL_CALL).invoke(cmd);
                break;
            case ASQL_SQL_FILE:
                if (checkNotConnected()) { return true; }
                boolean x = invokers.get(ASQL_SQL_FILE).invoke(cmd);
                if (!x) {
                    return x;
                }
                break;
            case ASQL_MULTIPLE:
                invokers.get(ASQL_MULTIPLE).invoke(cmd);
                break;
            case ASQL_DB_COMMAND:
                Boolean x1 = invokers.get(ASQL_DB_COMMAND).invoke(cmd);
                if (x1 != null) { return x1; }
                break;
            case ASQL_SINGLE:
                invokers.get(ASQL_SINGLE).invoke(cmd);
            case ASQL_EXIT:
            case ASQL_CANCEL:
            case UNKNOWN_COMMAND:
            case ASQL_COMMENT:
            case NULL_COMMAND:
            case MULTI_COMMENT_START:
            case MULTI_COMMENT_END:
            default:
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
        out.println(" AnySQL for PostgreSQL");
        out.println();
        out.println(" (@) Copyright Bruce 2021, all rights reserved.");
        out.println();
    }


    @Override
    public String getLastCommand() {
        if (lastcommand == null) { return null; }
        return lastcommand.command;
    }

    @Override
    public int fetch(ResultSet rs, DBRowCache rowCache)
    throws SQLException {
        return fetch(rs, rowCache, 100);
    }

    @Override
    public int fetch(ResultSet rs, DBRowCache rowCache, int size)
    throws SQLException {
        int    i            = 0;
        int    j            = 0;
        int    k            = 0;
        byte[] arrayOfByte1 = new byte[8192];
        char[] arrayOfChar1 = new char[4096];
        byte[] arrayOfByte2 = new byte[65536];
        char[] arrayOfChar2 = new char[65536];
        if (rowCache.getColumnCount() == 0) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                if (columnName != null) {
                    if (rowCache.findColumn(columnName) == 0) {
                        rowCache.addColumn(columnName, metaData.getColumnType(i));
                    } else {
                        for (j = 1; rowCache.findColumn(columnName + "_" + j) != 0; j++) { ; }
                        rowCache.addColumn(columnName + "_" + j, metaData.getColumnType(i));
                    }
                } else {
                    for (j = 1; rowCache.findColumn("NULL" + j) != 0; j++) { ; }
                    rowCache.addColumn("NULL" + j, metaData.getColumnType(i));
                }
            }
        }
        if (rowCache.getColumnCount() == 0) { return 0; }
        Object[] arrayOfObject;
        Object   localObject2;
        for (i = rowCache.getRowCount(); (i < size) && (rs.next());
                i = rowCache.appendRow(arrayOfObject)) {
            arrayOfObject = new Object[rowCache.getColumnCount()];
//            for (int kk = 1; kk <= rowCache.getColumnCount(); kk++) {
//                System.out.println("paramDBRowCache.getColumnType(" + kk + ") = " + rowCache
//                        .getColumnType(kk));
//            }
            for (j = 1; j <= rowCache.getColumnCount(); j++) {
//                System.out.println("j = " + j);
//                System.out.println("paramResultSet = " + paramResultSet);
//                System.out.println("paramDBRowCache.getColumnType(j) = " + paramDBRowCache.getColumnType(j));
//                System.out.println("paramResultSet.getObject(j) = " + paramResultSet.getObject(j));
                Object localObject1 = null;
                int    m;
                Object localObject3;
                Object localObject4;
                switch (rowCache.getColumnType(j)) {
                    case -1:
                        localObject2 = rs.getCharacterStream(j);
                        if (localObject2 == null) { break; }
                        try {
                            m = ((Reader) localObject2).read(arrayOfChar2);
                            if (m > 0) { localObject1 = String.valueOf(arrayOfChar2, 0, m); }
                            ((Reader) localObject2).close();
                        } catch (IOException localIOException1) {
                        }
                    case -4:
                        InputStream localInputStream1 = rs.getBinaryStream(j);
                        if (localInputStream1 == null) { break; }
                        try {
                            m = localInputStream1.read(arrayOfByte2);
                            if (m > 0) { localObject1 = new String(arrayOfByte2, 0, m); }
                            localInputStream1.close();
                        } catch (IOException localIOException2) {
                        }
                    case 2005:
                        Clob localClob = rs.getClob(j);
                        if (localClob == null) { break; }
                        localObject3 = localClob.getCharacterStream();
                        if (localObject3 == null) { break; }
                        try {
                            m = ((Reader) localObject3).read(arrayOfChar2);
                            if (m > 0) { localObject1 = String.valueOf(arrayOfChar2, 0, m); }
                            ((Reader) localObject3).close();
                        } catch (IOException localIOException3) {
                        }
                    case 2004:
                        localObject3 = rs.getBlob(j);
                        if (localObject3 == null) { break; }
                        localObject4 = ((Blob) localObject3).getBinaryStream();
                        if (localObject4 == null) { break; }
                        try {
                            m = ((InputStream) localObject4).read(arrayOfByte2);
                            if (m > 0) { localObject1 = new String(arrayOfByte2, 0, m); }
                            ((InputStream) localObject4).close();
                        } catch (IOException localIOException4) {
                        }
                    case 1:
                    case 12:
                        localObject4 = rs.getCharacterStream(j);
                        if (localObject4 == null) { break; }
                        try {
                            m = ((Reader) localObject4).read(arrayOfChar1);
                            if (rowCache.getColumnType(j) == 1) {
                                while ((m > 0) && (arrayOfChar1[(m - 1)] == ' ')) { m--; }
                            }
                            if (m > 0) { localObject1 = String.valueOf(arrayOfChar1, 0, m); }
                            ((Reader) localObject4).close();
                        } catch (IOException localIOException5) {
                        }
                    case -3:
                    case -2:
                        InputStream localInputStream2 = rs.getAsciiStream(j);
                        if (localInputStream2 == null) { break; }
                        try {
                            m = localInputStream2.read(arrayOfByte1);
                            if (rowCache.getColumnType(j) == -2) {
                                while ((m > 0) && (arrayOfByte1[(m - 1)] == 32)) { m--; }
                            }
                            if (m > 0) { localObject1 = new String(arrayOfByte1, 0, m); }
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                    case 91:
                        try {
                            localObject1 = rs.getTimestamp(j);
                        } catch (Throwable e) {}
                        break;
                    case 92:
                        try {
                            localObject1 = rs.getTime(j);
                        } catch (Throwable e) {}
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


    public void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) { closeable.close(); }
        } catch (Throwable ignore) {
        }
    }

    public void getObjectFromCommand(String paramString) {
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(paramString));
        int      i             = 0;
        lastObjectType  = null;
        lastObjectOwner = null;
        lastObjectName  = null;
        while ((arrayOfString[i].equalsIgnoreCase("ALTER"))
                || (arrayOfString[i].equalsIgnoreCase("CREATE"))
                || (arrayOfString[i].equalsIgnoreCase("OR"))
                || (arrayOfString[i].equalsIgnoreCase("REPLACE"))
                || (arrayOfString[i].equalsIgnoreCase("PUBLIC"))
                || (arrayOfString[i].equalsIgnoreCase("DROP"))) { i++; }
        if ((i < arrayOfString.length) && (i > 0)) {
            if ((arrayOfString[i].equalsIgnoreCase("PACKAGE"))
                    || (arrayOfString[i].equalsIgnoreCase("TYPE"))) {
                i++;
                if (i < arrayOfString.length) {
                    if (arrayOfString[i].equalsIgnoreCase("BODY")) {
                        lastObjectType = (arrayOfString[(i - 1)].toUpperCase() + " BODY");
                    } else {
                        lastObjectType = arrayOfString[(i - 1)].toUpperCase();
                        i--;
                    }
                }
            } else {
                lastObjectType = arrayOfString[i].toUpperCase();
            }
            i++;
        }
        if ((i < arrayOfString.length) && (i > 0)) {
            String str = arrayOfString[i].toUpperCase();
            i = str.indexOf("(");
            if (i > 0) { str = str.substring(0, i); }
            i = str.indexOf(".");
            if (i > 0) {
                lastObjectOwner = str.substring(0, i);
                lastObjectName  = str.substring(i + 1);
            } else {
                lastObjectName = str;
            }
        }
    }

    public DBRowCache getSessionWait() {
        DBRowCache localDBRowCache = null;
        try {
            if (traceSqls[3] != null) {
                localDBRowCache = executeQuery(traceSqls[3], sysVariable, 2000);
            }
        } catch (SQLException localSQLException) {
            out.print(localSQLException);
        }
        return localDBRowCache;
    }

    public DBRowCache getDbRowCacheOfSessionWait() {
        DBRowCache rowCache = null;
        if ((!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("EVENT".equalsIgnoreCase(autotraceName)))) { rowCache = getSessionWait(); }
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
        if ((rowCache1 == null) || (rowCache2 == null)) { return; }
        if (rowCache1.getRowCount() == 0) { return; }
        if (rowCache1.getRowCount() == rowCache2.getRowCount()) {
            out.println();
            out.println("Waiting Event");
            out.println("  Waits     Time  Event");
            out.println("-----------------------------------------------------");
            for (int i = 1; i <= rowCache1.getRowCount(); i++) {
                String     str = rowCache1.getString(i, 1);
                BigInteger bi1 = new BigInteger(rowCache1.getItem(i, 2).toString());
                BigInteger bi3 = new BigInteger(rowCache1.getItem(i, 3).toString());
                BigInteger bi2 = new BigInteger(rowCache2.getItem(i, 2).toString());
                BigInteger bi4 = new BigInteger(rowCache2.getItem(i, 3).toString());
                BigInteger bi5 = bi2.add(bi1.negate());
                BigInteger bi6 = bi4.add(bi3.negate());
                if ((bi6.longValue() <= 0L) || (!str.equals(rowCache2.getItem(i, 1)))) { continue; }
                out.print(rpad(bi5.longValue() + " ", 8));
                out.print(rpad(bi6.longValue() + "  ", 10));
                out.println((String) rowCache1.getItem(i, 1));
            }
        }
    }

    public DBRowCache getSessionStats() {
        DBRowCache localDBRowCache = null;
        try {
            if (traceSqls[0] != null) {
                localDBRowCache = executeQuery(traceSqls[0], sysVariable, 1000);
            }
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
                || ("STATS".equalsIgnoreCase(autotraceName)))) { rowCache = getSessionStats(); }
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
        if ((rowCache1 == null) || (rowCache2 == null)) { return; }
        if (rowCache1.getRowCount() == 0) { return; }
        if (rowCache1.getRowCount() == rowCache2.getRowCount()) {
            out.println();
            out.println("Statistics");
            out.println("---------------------------------------------------");
            for (int i = 1; i <= rowCache1.getRowCount(); i++) {
                Object     localObject1 = rowCache1.getItem(i, 1);
                Object     localObject2 = rowCache2.getItem(i, 1);
                BigInteger bi1          = new BigInteger(rowCache1.getItem(i, 2).toString());
                BigInteger bi2          = new BigInteger(rowCache2.getItem(i, 2).toString());
                BigInteger bi3          = bi2.add(bi1.negate());
                if ((bi3.longValue() <= 0L) || (!localObject1.equals(localObject2))) { continue; }
                if (bi3.longValue() > 10000000L) {
                    out.println(
                            rpad(new StringBuilder().append(" ").append(bi3.longValue() / 1000000L)
                                                    .append("M").toString(), 12) + "  " + hashtable
                                    .get(localObject1).toString());
                } else if (bi3.longValue() > 10000L) {
                    out.println(rpad(new StringBuilder().append(" ").append(bi3.longValue() / 1000L)
                                                        .append("K").toString(), 12) + "  "
                            + hashtable.get(localObject1).toString());
                } else {
                    out.println(
                            rpad(new StringBuilder().append(" ").append(bi3.longValue()).toString(),
                                    12) + "  " + hashtable.get(localObject1).toString());
                }
            }
        }
    }

    public DBRowCache getSystemEvent() {
        DBRowCache localDBRowCache = null;
        try {
            if (traceSqls[5] != null) {
                localDBRowCache = executeQuery(traceSqls[5], sysVariable, 2000);
            }
        } catch (SQLException localSQLException) {
        }
        return localDBRowCache;
    }

    public final DBRowCache getExplainPlan(String paramString) {
        DBRowCache   rowCache  = null;
        String       str       = "P" + Math.random();
        SQLStatement statement = null;
        try {
            sysVariable.add("PLAN_STMT_ID", 12);
            sysVariable.setValue("PLAN_STMT_ID", str);
            if (traceSqls[6] != null) {
                traceSqls[6].bind(sysVariable);
                traceSqls[6].stmt.execute();
            }
            statement = prepareScript(database,
                    "EXPLAIN PLAN SET STATEMENT_ID='" + str + "' FOR " + paramString, sysVariable);
            statement.stmt.execute();
            if (traceSqls[7] != null) {
                rowCache = executeQuery(traceSqls[7], sysVariable, 1000);
            }
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
        if ((paramCommand.type1 != SQL_BLOCK) && (paramCommand.type1 != SQL_DDL)
                && (!"OFF".equalsIgnoreCase(autotraceType))
                && (("ALL".equalsIgnoreCase(autotraceName))
                || ("EXPLAIN".equalsIgnoreCase(autotraceName))
                || ("EXP".equalsIgnoreCase(autotraceName)))) {
            DBRowCache localDBRowCache5 = getExplainPlan(paramCommand.command);
            if (localDBRowCache5 != null) {
                out.println();
                out.println("Execute Plan");
                showDBRowCache(localDBRowCache5, true);
            }
        }
    }


    public void showDBRowCache(DBRowCache paramDBRowCache, boolean paramBoolean) {
        paramDBRowCache.getWidth(false);
        if (paramBoolean) {
            out.println(paramDBRowCache.getFixedHeader());
            out.println(paramDBRowCache.getSeperator());
        }
        for (int i = 1; i <= paramDBRowCache.getRowCount(); i++) {
            out.println(paramDBRowCache.getFixedRow(i));
        }
    }


}
