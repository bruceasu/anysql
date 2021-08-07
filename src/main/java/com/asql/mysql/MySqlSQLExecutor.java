package com.asql.mysql;

import static com.asql.core.CMDType.*;

import com.asql.core.*;
import com.asql.core.io.CommandReader;
import com.asql.core.io.DefaultCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.log.DefaultCommandLog;
import com.asql.core.util.DateOperator;
import com.asql.mysql.invoker.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Map;

public class MySqlSQLExecutor extends DefaultSQLExecutor {

    private CMDType       _cmdtype    = null;
    private CommandLog    _stdout     = null;
    private CommandReader _stdin      = null;
    private VariableTable tnsnames    = new VariableTable();
    private Command       lastcommand = null;

    private Map<Integer, ModuleInvoker> invokers = new Hashtable<>();

    public MySqlSQLExecutor() {
        super(new MySqlCMDType());
        this._cmdtype = new MySqlCMDType();
        this._stdout  = new DefaultCommandLog(this);
        this._stdin   = new DefaultCommandReader();
        setShowComplete(false);
        setFetchSize(12);

        init();
    }

    public MySqlSQLExecutor(CommandReader reader, CommandLog log) {
        super(new MySqlCMDType(), reader, log);
        this._cmdtype = new MySqlCMDType();
        this._stdout  = log;
        this._stdin   = reader;
        setShowComplete(false);
        setFetchSize(12);

        init();
    }


    private void init() {
        invokers.put(ASQL_MULTIPLE, new MultipleInvoker(this));
        invokers.put(ASQL_SINGLE, new SingleInvoker(this));
        invokers.put(SQL_QUERY, new SQLInvoker(this));
        invokers.put(SQL_DML, new SQLInvoker(this));
        invokers.put(SQL_DDL, new SQLInvoker(this));
        invokers.put(SQL_BLOCK, new SQLInvoker(this));
        invokers.put(SQL_CALL, new CallInvoker(this));
        invokers.put(SQL_SCRIPT, new ScriptInvoker(this));
        invokers.put(ASQL_SQL_FILE, new SQLFileInvoker(this));


    }

    @Override
    public final boolean execute(Command cmd) {

        if (cmd == null) {
            this._stdout.println("No command to execute.");
            return true;
        }
        long startTime;
        long endTime = 0L;
        switch (cmd.type1) {
            case SQL_QUERY:
            case SQL_DML:
            case SQL_DDL:
            case SQL_BLOCK:
                if (checkNotConnected()) { return true;}
                invokers.get(SQL_QUERY).invoke(cmd);
                this.lastcommand = cmd;
                break;
            case SQL_SCRIPT:
                if (checkNotConnected()) { return true; }
                invokers.get(SQL_SCRIPT).invoke(cmd);
                this.lastcommand = cmd;
                break;
            case ASQL_END:
                execute(this.lastcommand);
                break;
            case SQL_CALL:
                if (checkNotConnected()) { return true; }
                invokers.get(SQL_CALL).invoke(cmd);
                break;
            case ASQL_SQL_FILE:
                invokers.get(ASQL_MULTIPLE).invoke(cmd);
                break;
            case ASQL_MULTIPLE:
                return invokers.get(ASQL_MULTIPLE).invoke(cmd);
            case ASQL_SINGLE:
                return invokers.get(ASQL_SINGLE).invoke(cmd);
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
    public void procUnknownCommand(Command paramCommand) {
        this._stdout.println("Unknown command!");
    }


    @Override
    public void showVersion() {
        this._stdout.println();
        this._stdout.println(" AnySQL for MySQL, version 1.0.1 -- " + DateOperator.getDay(
                "yyyy-MM-dd HH:mm:ss"));
        this._stdout.println();
        this._stdout.println(" (@) Copyright Lou Fangxin(1.0.0), all rights reserved.");
        this._stdout.println(" (@) Copyright Bruce(1.0.0), all rights left.");
        this._stdout.println();
    }

    @Override
    public long writeData(BufferedWriter paramBufferedWriter,
                          ResultSet paramResultSet,
                          String paramString1,
                          String paramString2,
                          boolean paramBoolean) throws SQLException, IOException {
        long              l1                     = 0L;
        String            str1                   = null;
        int               i                      = 0;
        int               j                      = 32768;
        byte[]            arrayOfByte1           = new byte[8192];
        char[]            arrayOfChar1           = new char[4096];
        byte[]            arrayOfByte2           = new byte[65536];
        char[]            arrayOfChar2           = new char[65536];
        long              l2                     = System.currentTimeMillis();
        ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
        int               k                      = localResultSetMetaData.getColumnCount();
        int[]             arrayOfInt             = new int[k];
        SimpleDateFormat  localSimpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat  localSimpleDateFormat2 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat  localSimpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int               m                      = 0;
        for (m = 0; m < k; m++) {
            arrayOfInt[m] = localResultSetMetaData.getColumnType(m + 1);
            if (!paramBoolean) {
                continue;
            }
            str1 = localResultSetMetaData.getColumnName(m + 1);
            if (str1 != null) {
                paramBufferedWriter.write(str1);
            }
            if (m >= k - 1) {
                continue;
            }
            paramBufferedWriter.write(paramString1);
        }
        if (paramBoolean) {
            paramBufferedWriter.write(paramString2);
        }
        while (paramResultSet.next()) {
            l1 += 1L;
            for (m = 1; m <= k; m++) {
                Object localObject1;
                Object localObject2;
                switch (arrayOfInt[(m - 1)]) {
                    case -1:
                        Reader localReader = paramResultSet.getCharacterStream(m);
                        if (localReader == null) {
                            break;
                        }
                        try {
                            i = localReader.read(arrayOfChar2);
                            if (i > 0) {
                                paramBufferedWriter.write(arrayOfChar2, 0, i);
                            }
                            localReader.close();
                        } catch (IOException localIOException1) {
                        }
                    case -4:
                        InputStream localInputStream1 = paramResultSet.getBinaryStream(m);
                        if (localInputStream1 == null) {
                            break;
                        }
                        try {
                            i = localInputStream1.read(arrayOfByte2);
                            if (i > 0) {
                                paramBufferedWriter.write(new String(arrayOfByte2, 0, i));
                            }
                            localInputStream1.close();
                        } catch (IOException localIOException2) {
                        }
                    case 2005:
                        Clob localClob = paramResultSet.getClob(m);
                        if (localClob == null) {
                            break;
                        }
                        localObject1 = localClob.getCharacterStream();
                        if (localObject1 == null) {
                            break;
                        }
                        try {
                            i = ((Reader) localObject1).read(arrayOfChar2);
                            if (i > 0) {
                                paramBufferedWriter.write(arrayOfChar2, 0, i);
                            }
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException3) {
                        }
                    case 2004:
                        localObject1 = paramResultSet.getBlob(m);
                        if (localObject1 == null) {
                            break;
                        }
                        localObject2 = ((Blob) localObject1).getBinaryStream();
                        if (localObject2 == null) {
                            break;
                        }
                        try {
                            i = ((InputStream) localObject2).read(arrayOfByte2);
                            if (i > 0) {
                                paramBufferedWriter.write(new String(arrayOfByte2, 0, i));
                            }
                            ((InputStream) localObject2).close();
                        } catch (IOException localIOException4) {
                        }
                    case 1:
                    case 12:
                        localObject2 = paramResultSet.getCharacterStream(m);
                        if (localObject2 == null) {
                            break;
                        }
                        try {
                            i = ((Reader) localObject2).read(arrayOfChar1);
                            if (arrayOfInt[(m - 1)] == 1) {
                                while ((i > 0) && (arrayOfChar1[(i - 1)] == ' ')) {
                                    i--;
                                }
                            }
                            if (i > 0) {
                                paramBufferedWriter.write(arrayOfChar1, 0, i);
                            }
                            ((Reader) localObject2).close();
                        } catch (IOException localIOException5) {
                        }
                    case -3:
                    case -2:
                        InputStream localInputStream2 = paramResultSet.getAsciiStream(m);
                        if (localInputStream2 == null) {
                            break;
                        }
                        try {
                            i = localInputStream2.read(arrayOfByte1);
                            if (arrayOfInt[(m - 1)] == -2) {
                                while ((i > 0) && (arrayOfByte1[(i - 1)] == 32)) {
                                    i--;
                                }
                            }
                            if (i > 0) {
                                paramBufferedWriter.write(new String(arrayOfByte1, 0, i));
                            }
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                    case 91:
                        Date localDate = paramResultSet.getDate(m);
                        if (localDate == null) {
                            break;
                        }
                        paramBufferedWriter.write(localSimpleDateFormat1.format(localDate));
                        break;
                    case 92:
                        Time localTime = paramResultSet.getTime(m);
                        if (localTime == null) {
                            break;
                        }
                        paramBufferedWriter.write(localSimpleDateFormat2.format(localTime));
                        break;
                    case 93:
                        Timestamp localTimestamp = paramResultSet.getTimestamp(m);
                        if (localTimestamp == null) {
                            break;
                        }
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
                        if (str2 == null) {
                            break;
                        }
                        paramBufferedWriter.write(str2);
                }
                if (m < k) {
                    paramBufferedWriter.write(paramString1);
                } else {
                    paramBufferedWriter.write(paramString2);
                }
            }
            if (l1 % 100000L != 0L) {
                continue;
            }
            getCommandLog().println(
                    lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(
                            System.currentTimeMillis() - l2));
        }
        if (l1 % 100000L != 0L) {
            getCommandLog().println(
                    lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(
                            System.currentTimeMillis() - l2));
        }
        return l1;
    }

    @Override
    public int fetch(ResultSet rs, DBRowCache rowCache) throws SQLException {
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
        Object localObject2;
        if (rowCache.getColumnCount() == 0) {
            localObject2 = rs.getMetaData();
            for (i = 1; i <= ((ResultSetMetaData) localObject2).getColumnCount(); i++) {
                if (((ResultSetMetaData) localObject2).getColumnName(i) != null) {
                    if (rowCache.findColumn(
                            ((ResultSetMetaData) localObject2).getColumnName(i)) == 0) {
                        rowCache.addColumn(
                                ((ResultSetMetaData) localObject2).getColumnName(i),
                                ((ResultSetMetaData) localObject2).getColumnType(i));
                    } else {
                        for (j = 1; rowCache.findColumn(
                                ((ResultSetMetaData) localObject2).getColumnName(i) + "_" + j) != 0;
                                j++) {
                            ;
                        }
                        rowCache.addColumn(
                                ((ResultSetMetaData) localObject2).getColumnName(i) + "_" + j,
                                ((ResultSetMetaData) localObject2).getColumnType(i));
                    }
                } else {
                    for (j = 1; rowCache.findColumn("NULL" + j) != 0; j++) {
                        ;
                    }
                    rowCache.addColumn("NULL" + j,
                            ((ResultSetMetaData) localObject2).getColumnType(i));
                }
            }
        }
        if (rowCache.getColumnCount() == 0) {
            return 0;
        }
        Object[] arrayOfObject;
        for (i = rowCache.getRowCount(); (i < size) && (rs.next());
                i = rowCache.appendRow(arrayOfObject)) {
            arrayOfObject = new Object[rowCache.getColumnCount()];
            for (j = 1; j <= rowCache.getColumnCount(); j++) {
                Object localObject1 = null;
                int    m;
                Object localObject3;
                Object localObject4;
                switch (rowCache.getColumnType(j)) {
                    case -1:
                        localObject2 = rs.getCharacterStream(j);
                        if (localObject2 == null) {
                            break;
                        }
                        try {
                            m = ((Reader) localObject2).read(arrayOfChar2);
                            if (m > 0) {
                                localObject1 = String.valueOf(arrayOfChar2, 0, m);
                            }
                            ((Reader) localObject2).close();
                        } catch (IOException localIOException1) {
                        }
                        break;
                    case -4:
                        InputStream localInputStream1 = rs.getBinaryStream(j);
                        if (localInputStream1 == null) {
                            break;
                        }
                        try {
                            m = localInputStream1.read(arrayOfByte2);
                            if (m > 0) {
                                localObject1 = new String(arrayOfByte2, 0, m);
                            }
                            localInputStream1.close();
                        } catch (IOException localIOException2) {
                        }
                        break;
                    case 2005:
                        Clob localClob = rs.getClob(j);
                        if (localClob == null) {
                            break;
                        }
                        localObject3 = localClob.getCharacterStream();
                        if (localObject3 == null) {
                            break;
                        }
                        try {
                            m = ((Reader) localObject3).read(arrayOfChar2);
                            if (m > 0) {
                                localObject1 = String.valueOf(arrayOfChar2, 0, m);
                            }
                            ((Reader) localObject3).close();
                        } catch (IOException localIOException3) {
                        }
                        break;
                    case 2004:
                        localObject3 = rs.getBlob(j);
                        if (localObject3 == null) {
                            break;
                        }
                        localObject4 = ((Blob) localObject3).getBinaryStream();
                        if (localObject4 == null) {
                            break;
                        }
                        try {
                            m = ((InputStream) localObject4).read(arrayOfByte2);
                            if (m > 0) {
                                localObject1 = new String(arrayOfByte2, 0, m);
                            }
                            ((InputStream) localObject4).close();
                        } catch (IOException localIOException4) {
                        }
                        break;
                    case 1:
                    case 12:
                        localObject4 = rs.getCharacterStream(j);
                        if (localObject4 == null) {
                            break;
                        }
                        try {
                            m = ((Reader) localObject4).read(arrayOfChar1);
                            if (rowCache.getColumnType(j) == 1) {
                                while ((m > 0) && (arrayOfChar1[(m - 1)] == ' ')) {
                                    m--;
                                }
                            }
                            if (m > 0) {
                                localObject1 = String.valueOf(arrayOfChar1, 0, m);
                            }
                            ((Reader) localObject4).close();
                        } catch (IOException localIOException5) {
                        }
                        break;
                    case -3:
                    case -2:
                        InputStream localInputStream2 = rs.getAsciiStream(j);
                        if (localInputStream2 == null) {
                            break;
                        }
                        try {
                            m = localInputStream2.read(arrayOfByte1);
                            if (rowCache.getColumnType(j) == -2) {
                                while ((m > 0) && (arrayOfByte1[(m - 1)] == 32)) {
                                    m--;
                                }
                            }
                            if (m > 0) {
                                localObject1 = new String(arrayOfByte1, 0, m);
                            }
                            localInputStream2.close();
                        } catch (IOException localIOException6) {
                        }
                        break;
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

