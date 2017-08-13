package com.asql.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class DBOperation {
    public static final int SQL_QUERY_TIMEOUT = 3600;
    public static final int SQL_LONG_SIZE = 65536;

    public static final String getElapsed(long paramLong) {
        long l1 = paramLong / 1000L;
        long l2 = paramLong % 1000L;
        long l3 = l1 % 60L;
        l1 /= 60L;
        long l4 = l1 % 60L;
        long l5 = l1 / 60L;
        return (l5 < 10L ? "0" : "") + String.valueOf(l5) + ":" + (l4 < 10L ? "0" : "") + String.valueOf(l4) + ":" + (l3 < 10L ? "0" : "") + String.valueOf(l3) + "." + (l2 < 100L ? "0" : l2 < 10L ? "00" : "") + String.valueOf(l2);
    }

    public static final SQLStatement prepareScript(Connection paramConnection, String paramString)
            throws SQLException {
        return prepareScript(paramConnection, paramString, null, 1003, 1007);
    }

    public static final SQLStatement prepareScript(Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        return prepareScript(paramConnection, paramString, paramVariableTable, 1003, 1007);
    }

    public static final SQLStatement prepareScript(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2)
            throws SQLException {
        PreparedStatement localPreparedStatement = null;
        SQLQuery localSQLQuery = SQLConvert.parseSQL(paramString == null ? "" : paramString, paramVariableTable);
        localPreparedStatement = paramConnection.prepareStatement(localSQLQuery.getDestSQL(), paramInt1, paramInt2);
        localPreparedStatement.setQueryTimeout(SQL_QUERY_TIMEOUT);
        localPreparedStatement.setFetchSize(1000);
        return new SQLStatement(localPreparedStatement, localSQLQuery);
    }

    public static final SQLStatement prepareStatement(Connection paramConnection, String paramString)
            throws SQLException {
        return prepareStatement(paramConnection, paramString, null, 1003, 1007);
    }

    public static final SQLStatement prepareStatement(Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        return prepareStatement(paramConnection, paramString, paramVariableTable, 1003, 1007);
    }

    public static final SQLStatement prepareStatement(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2)
            throws SQLException {
        PreparedStatement localPreparedStatement = null;
        SQLQuery localSQLQuery = SQLConvert.parseSQL(paramString == null ? "" : paramString, paramVariableTable);
        localPreparedStatement = paramConnection.prepareStatement(localSQLQuery.getDestSQL(), paramInt1, paramInt2);
        localPreparedStatement.setQueryTimeout(SQL_QUERY_TIMEOUT);
        localPreparedStatement.setFetchSize(1000);
        return new SQLStatement(localPreparedStatement, localSQLQuery);
    }

    public static final SQLCallable prepareCall(Connection paramConnection, String paramString)
            throws SQLException {
        return prepareCall(paramConnection, paramString, null, 1003, 1007);
    }

    public static final SQLCallable prepareCall(Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        return prepareCall(paramConnection, paramString, paramVariableTable, 1003, 1007);
    }

    public static final SQLCallable prepareCall(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2)
            throws SQLException {
        CallableStatement localCallableStatement = null;
        SQLQuery localSQLQuery = SQLConvert.parseCall(paramString == null ? "" : paramString, paramVariableTable);
        localCallableStatement = paramConnection.prepareCall("{ " + localSQLQuery.getDestSQL() + " }", paramInt1, paramInt2);
        localCallableStatement.setQueryTimeout(SQL_QUERY_TIMEOUT);
        localCallableStatement.setFetchSize(1000);
        return new SQLCallable(localCallableStatement, localSQLQuery);
    }

    public static final int fetch(ResultSet paramResultSet, DBRowCache paramDBRowCache)
            throws SQLException {
        return fetch(paramResultSet, paramDBRowCache, 100);
    }

    public static final int fetch(ResultSet paramResultSet, DBRowCache paramDBRowCache, int paramInt)
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

    public static int executeUpdate(Connection paramConnection, String paramString)
            throws SQLException {
        return executeUpdate(paramConnection, paramString, null);
    }

    public static int executeUpdate(Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        int i = 0;
        SQLStatement localSQLStatement = null;
        SQLException localObject = null;
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString);
            localSQLStatement.bind(paramVariableTable);
            i = localSQLStatement.stmt.executeUpdate();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException2) {
        }
        if (localObject != null)
            throw localObject;
        return i;
    }

    public static final int[] executeUpdate(SQLStatement paramSQLStatement, DBRowCache paramDBRowCache)
            throws BatchUpdateException {
        return executeUpdate(paramSQLStatement, null, paramDBRowCache, 1, paramDBRowCache.getRowCount());
    }

    public static final int[] executeUpdate(SQLStatement paramSQLStatement, VariableTable paramVariableTable, DBRowCache paramDBRowCache)
            throws BatchUpdateException {
        return executeUpdate(paramSQLStatement, paramVariableTable, paramDBRowCache, 1, paramDBRowCache.getRowCount());
    }

    public static final int[] executeUpdate(SQLStatement paramSQLStatement, DBRowCache paramDBRowCache, int paramInt1, int paramInt2)
            throws BatchUpdateException {
        return executeUpdate(paramSQLStatement, null, paramDBRowCache, paramInt1, paramInt2);
    }

    public static final int[] executeUpdate(SQLStatement paramSQLStatement, VariableTable paramVariableTable, DBRowCache paramDBRowCache, int paramInt1, int paramInt2)
            throws BatchUpdateException {
        int[] arrayOfInt1 = new int[0];
        int[] arrayOfInt2 = new int[0];
        int[] arrayOfInt3 = new int[0];
        int[] arrayOfInt4 = new int[0];
        int i = 0;
        arrayOfInt1 = new int[paramInt2 - paramInt1 + 1];
        int j;
        if (paramSQLStatement.getParamNames().length > 0) {
            arrayOfInt3 = new int[paramSQLStatement.getParamNames().length];
            arrayOfInt4 = new int[paramSQLStatement.getParamNames().length];
            for (j = 0; j < paramSQLStatement.getParamNames().length; j++) {
                arrayOfInt3[j] = paramDBRowCache.findColumn(paramSQLStatement.getParamNames()[j]);
                if (arrayOfInt3[j] > 0)
                    arrayOfInt4[j] = paramDBRowCache.getColumnType(arrayOfInt3[j]);
                else
                    arrayOfInt4[j] = paramVariableTable.getType(paramSQLStatement.getParamNames()[j]);
            }
        }
        for (int k = paramInt1; k <= paramInt2; k++)
            try {
                if (paramSQLStatement.getParamNames().length > 0)
                    for (j = 0; j < paramSQLStatement.getParamNames().length; j++) {
                        Object localObject1;
                        if (arrayOfInt3[j] > 0)
                            localObject1 = paramDBRowCache.getItem(k, arrayOfInt3[j]);
                        else
                            localObject1 = paramVariableTable.getValue(paramSQLStatement.getParamNames()[j]);
                        if (localObject1 == null) {
                            paramSQLStatement.stmt.setNull(j + 1, 1);
                        } else if ((arrayOfInt4[j] != -1) && (arrayOfInt4[j] != -4)) {
                            paramSQLStatement.stmt.setObject(j + 1, localObject1);
                        } else {
                            Object localObject2;
                            if (arrayOfInt4[j] == -1) {
                                localObject2 = new StringReader(localObject1.toString());
                                paramSQLStatement.stmt.setCharacterStream(j + 1, (Reader) localObject2, 65536);
                            } else {
                                if (arrayOfInt4[j] != -4)
                                    continue;
                                localObject2 = new File(localObject1.toString());
                                if ((((File) localObject2).exists()) && (((File) localObject2).isFile()) && (((File) localObject2).canRead()))
                                    try {
                                        FileInputStream localFileInputStream = new FileInputStream((File) localObject2);
                                        paramSQLStatement.stmt.setBinaryStream(j + 1, localFileInputStream, (int) ((File) localObject2).length());
                                    } catch (IOException localIOException) {
                                    }
                                else
                                    paramSQLStatement.stmt.setNull(j + 1, 1);
                            }
                        }
                    }
                arrayOfInt1[(k - paramInt1)] = paramSQLStatement.stmt.executeUpdate();
                i++;
            } catch (SQLException localSQLException) {
                if (i > 0)
                    arrayOfInt2 = new int[i];
                for (int m = 0; m < i; m++)
                    arrayOfInt2[m] = arrayOfInt1[m];
                BatchUpdateException localBatchUpdateException = new BatchUpdateException(localSQLException.getMessage(), localSQLException.getSQLState(), localSQLException.getErrorCode(), arrayOfInt2);
                throw localBatchUpdateException;
            }
        return arrayOfInt1;
    }

    public static final int[] addBatch(SQLStatement paramSQLStatement, DBRowCache paramDBRowCache)
            throws BatchUpdateException {
        return addBatch(paramSQLStatement, null, paramDBRowCache, 1, paramDBRowCache.getRowCount());
    }

    public static final int[] addBatch(SQLStatement paramSQLStatement, VariableTable paramVariableTable, DBRowCache paramDBRowCache)
            throws BatchUpdateException {
        return addBatch(paramSQLStatement, paramVariableTable, paramDBRowCache, 1, paramDBRowCache.getRowCount());
    }

    public static final int[] addBatch(SQLStatement paramSQLStatement, DBRowCache paramDBRowCache, int paramInt1, int paramInt2)
            throws BatchUpdateException {
        return addBatch(paramSQLStatement, null, paramDBRowCache, paramInt1, paramInt2);
    }

    public static final int[] addBatch(SQLStatement paramSQLStatement, VariableTable paramVariableTable, DBRowCache paramDBRowCache, int paramInt1, int paramInt2)
            throws BatchUpdateException {
        int[] arrayOfInt1 = new int[0];
        int[] arrayOfInt2 = new int[0];
        int[] arrayOfInt3 = new int[0];
        int i = 0;
        int j = 0;
        int k = 1;
        Object localObject1 = null;
        arrayOfInt1 = new int[paramInt2 - paramInt1 + 1];
        if (paramSQLStatement.getParamNames().length > 0) {
            arrayOfInt2 = new int[paramSQLStatement.getParamNames().length];
            arrayOfInt3 = new int[paramSQLStatement.getParamNames().length];
            for (j = 0; j < paramSQLStatement.getParamNames().length; j++) {
                arrayOfInt2[j] = paramDBRowCache.findColumn(paramSQLStatement.getParamNames()[j]);
                if (arrayOfInt2[j] > 0)
                    arrayOfInt3[j] = paramDBRowCache.getColumnType(arrayOfInt2[j]);
                else
                    arrayOfInt3[j] = paramVariableTable.getType(paramSQLStatement.getParamNames()[j]);
            }
        }
        for (k = paramInt1; k <= paramInt2; k++)
            try {
                if (paramSQLStatement.getParamNames().length > 0)
                    for (j = 0; j < paramSQLStatement.getParamNames().length; j++) {
                        if (arrayOfInt2[j] > 0)
                            localObject1 = paramDBRowCache.getItem(k, arrayOfInt2[j]);
                        else
                            localObject1 = paramVariableTable.getValue(paramSQLStatement.getParamNames()[j]);
                        if (localObject1 == null) {
                            paramSQLStatement.stmt.setNull(j + 1, 1);
                        } else if ((arrayOfInt3[j] != -1) && (arrayOfInt3[j] != -4)) {
                            paramSQLStatement.stmt.setObject(j + 1, localObject1);
                        } else {
                            Object localObject2;
                            if (arrayOfInt3[j] == -1) {
                                localObject2 = new StringReader(localObject1.toString());
                                paramSQLStatement.stmt.setCharacterStream(j + 1, (Reader) localObject2, 65536);
                            } else {
                                if (arrayOfInt3[j] != -4)
                                    continue;
                                localObject2 = new File(localObject1.toString());
                                if ((((File) localObject2).exists()) && (((File) localObject2).isFile()) && (((File) localObject2).canRead()))
                                    try {
                                        FileInputStream localFileInputStream = new FileInputStream((File) localObject2);
                                        paramSQLStatement.stmt.setBinaryStream(j + 1, localFileInputStream, (int) ((File) localObject2).length());
                                    } catch (IOException localIOException) {
                                    }
                                else
                                    paramSQLStatement.stmt.setNull(j + 1, 1);
                            }
                        }
                    }
                paramSQLStatement.stmt.addBatch();
                i++;
            } catch (SQLException localSQLException1) {
                if (i > 0)
                    arrayOfInt1 = new int[i];
                for (int n = 0; n < i; n++)
                    arrayOfInt1[n] = 1;
                try {
                    paramSQLStatement.stmt.clearParameters();
                } catch (SQLException localSQLException2) {
                }
                BatchUpdateException localBatchUpdateException = new BatchUpdateException(localSQLException1.getMessage(), localSQLException1.getSQLState(), localSQLException1.getErrorCode(), arrayOfInt1);
                throw localBatchUpdateException;
            }
        if (i > 0)
            arrayOfInt1 = new int[i];
        for (int m = 0; m < i; m++)
            arrayOfInt1[m] = 1;
        return arrayOfInt1;
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString)
            throws SQLException {
        return executeQuery(paramConnection, paramString, null, 10000);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, int paramInt)
            throws SQLException {
        return executeQuery(paramConnection, paramString, null, paramInt);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        return executeQuery(paramConnection, paramString, paramVariableTable, 10000);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(paramInt);
            localSQLStatement.bind(paramVariableTable);
            localResultSet = localSQLStatement.stmt.executeQuery();
            fetch(localResultSet, localSimpleDBRowCache, paramInt);
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache;
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramVariableTable, paramInt1, paramInt2, paramInt3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramVariableTable, paramInt, paramArrayOfInt1, paramArrayOfInt2);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, paramVariableTable, paramString2, paramString3, paramString4);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, paramVariableTable, paramString2, paramArrayOfString1, paramArrayOfString2);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramVariableTable, paramArrayOfInt, paramInt1, paramInt2);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramVariableTable, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString1, VariableTable paramVariableTable, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, paramVariableTable, paramArrayOfString, paramString2, paramString3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramVariableTable, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramInt1, paramInt2, paramInt3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramInt, paramArrayOfInt1, paramArrayOfInt2);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString1, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, paramString2, paramString3, paramString4);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString1, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, paramString2, paramArrayOfString1, paramArrayOfString2);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramArrayOfInt, paramInt1, paramInt2);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString1, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, paramArrayOfString, paramString2, paramString3);
    }

    public static DBRowCache executeQuery(Connection paramConnection, String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramVariableTable, paramInt1, paramInt2, paramInt3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramVariableTable, paramInt, paramArrayOfInt1, paramArrayOfInt2);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, paramVariableTable, paramString2, paramString3, paramString4);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, paramVariableTable, paramString2, paramArrayOfString1, paramArrayOfString2);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramVariableTable, paramArrayOfInt, paramInt1, paramInt2);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramVariableTable, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, VariableTable paramVariableTable, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, paramVariableTable, paramArrayOfString, paramString2, paramString3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramVariableTable, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramInt1, paramInt2, paramInt3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramInt, paramArrayOfInt1, paramArrayOfInt2);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, paramString2, paramString3, paramString4);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, paramString2, paramArrayOfString1, paramArrayOfString2);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramArrayOfInt, paramInt1, paramInt2);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, paramArrayOfString, paramString2, paramString3);
    }

    public static void executeQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramInt, paramArrayOfInt1, paramArrayOfInt2);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramInt1, paramInt2, paramInt3);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString1, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramString2, paramString3, paramString4);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString1, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramString2, paramArrayOfString1, paramArrayOfString2);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramArrayOfInt, paramInt1, paramInt2);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString1, VariableTable paramVariableTable, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString1, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramArrayOfString, paramString2, paramString3);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
        SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache1, 1000);
                localSimpleDBRowCache2.addCrosstab(localSimpleDBRowCache1, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
                localSimpleDBRowCache1.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
        return localSimpleDBRowCache2;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramInt, paramArrayOfInt1, paramArrayOfInt2);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramInt1, paramInt2, paramInt3);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString1, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramString2, paramString3, paramString4);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, VariableTable paramVariableTable, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString1, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramString2, paramArrayOfString1, paramArrayOfString2);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramArrayOfInt, paramInt1, paramInt2);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, VariableTable paramVariableTable, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString1, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramArrayOfString, paramString2, paramString3);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException localObject = null;
        int i = 1000;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(10000);
            localSQLStatement.bind(paramVariableTable);
            if (localSQLStatement.stmt == null)
                throw new SQLException("Null Statument Returned!");
            localResultSet = localSQLStatement.stmt.executeQuery();
            while (i == 1000) {
                i = fetch(localResultSet, localSimpleDBRowCache, 1000);
                paramDBRowCache.addCrosstab(localSimpleDBRowCache, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
                localSimpleDBRowCache.deleteAllRow();
            }
            localResultSet.close();
        } catch (SQLException localSQLException1) {
            localObject = localSQLException1;
        }
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null))
                localSQLStatement.stmt.close();
        } catch (SQLException localSQLException3) {
        }
        if (localObject != null)
            throw localObject;
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, null, paramInt1, paramInt2, paramInt3);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString1, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, null, paramString2, paramString3, paramString4);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        return crossQuery(paramConnection, paramString, null, paramInt, paramArrayOfInt1, paramArrayOfInt2);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString1, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, null, paramString2, paramArrayOfString1, paramArrayOfString2);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        return crossQuery(paramConnection, paramString, null, paramArrayOfInt, paramInt1, paramInt2);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString1, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        return crossQuery(paramConnection, paramString1, null, paramArrayOfString, paramString2, paramString3);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, null, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        return crossQuery(paramConnection, paramString, null, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        return crossQuery(paramConnection, paramString, paramVariableTable, 1, 2, 3);
    }

    public static DBRowCache crossQuery(Connection paramConnection, String paramString)
            throws SQLException {
        return crossQuery(paramConnection, paramString, null, 1, 2, 3);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int paramInt1, int paramInt2, int paramInt3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, null, paramInt1, paramInt2, paramInt3);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, String paramString2, String paramString3, String paramString4)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, null, paramString2, paramString3, paramString4);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, null, paramInt, paramArrayOfInt1, paramArrayOfInt2);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, String paramString2, String[] paramArrayOfString1, String[] paramArrayOfString2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, null, paramString2, paramArrayOfString1, paramArrayOfString2);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int[] paramArrayOfInt, int paramInt1, int paramInt2)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, null, paramArrayOfInt, paramInt1, paramInt2);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString1, String[] paramArrayOfString, String paramString2, String paramString3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString1, null, paramArrayOfString, paramString2, paramString3);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, null, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, null, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString, VariableTable paramVariableTable)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, paramVariableTable, 1, 2, 3);
    }

    public static void crossQuery(DBRowCache paramDBRowCache, Connection paramConnection, String paramString)
            throws SQLException {
        crossQuery(paramDBRowCache, paramConnection, paramString, null, 1, 2, 3);
    }
}
