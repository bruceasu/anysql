package com.asql.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;

public final class SQLCallable extends SQLStatement {
    public CallableStatement stmt = null;

    public SQLCallable(CallableStatement paramCallableStatement, SQLQuery paramSQLQuery) {
        super(paramCallableStatement, paramSQLQuery);
        this.stmt = paramCallableStatement;
    }


    public boolean getMoreResults()
            throws SQLException {
        return this.stmt.getMoreResults();
    }

    public ResultSet getResultSet()
            throws SQLException {
        return this.stmt.getResultSet();
    }

    public int getUpdateCount()
            throws SQLException {
        return this.stmt.getUpdateCount();
    }

    public boolean execute(VariableTable paramVariableTable)
            throws SQLException {
        bind(paramVariableTable);
        boolean bool = this.stmt.execute();
        fetch(paramVariableTable);
        return bool;
    }

    public void close()
            throws SQLException {
        if (this.stmt != null) {
            this.stmt.close();
            this.stmt = null;
        }
    }

    public void bind(VariableTable paramVariableTable)
            throws SQLException {
        try {
            this.stmt.clearParameters();
        } catch (SQLException ignored) {
        }
        if (this.paramNames.length > 0)
            for (int i = 0; i < this.paramNames.length; i++) {
                int j = paramVariableTable.getType(this.paramNames[i]);
                Object localObject1 = paramVariableTable.getValue(this.paramNames[i]);
                if ((this.paramTypes[i].equals("IN")) || (this.paramTypes[i].equals("INOUT"))) {
                    setObject(i, j, localObject1);
                    if (!this.paramTypes[i].equals("INOUT"))
                        continue;
                    this.stmt.registerOutParameter(i + 1, paramVariableTable.getType(this.paramNames[i]));
                } else {
                    if (!this.paramTypes[i].equals("OUT"))
                        continue;
                    this.stmt.registerOutParameter(i + 1, paramVariableTable.getType(this.paramNames[i]));
                }
            }
    }

    public void bind(VariableTable paramVariableTable, DBRowCache paramDBRowCache, int paramInt)
            throws SQLException {
        int j = 1;

        CreateTypes createTypes = new CreateTypes(paramVariableTable, paramDBRowCache).invoke();
        int[] colIndexes = createTypes.getColIndexes();
        int[] colTypes = createTypes.getColTypes();
        if (this.paramNames.length > 0) {
            Object[] arrayOfObject = paramDBRowCache.getRow(j);
            for (int i = 0; i < this.paramNames.length; i++) {
                Object localObject1;
                if (colIndexes[i] > 0)
                    localObject1 = arrayOfObject[(colIndexes[i] - 1)];
                else
                    localObject1 = paramVariableTable.getValue(this.paramNames[i]);
                if ((this.paramTypes[i].equals("IN")) || (this.paramTypes[i].equals("INOUT"))) {
                    setParamObject(localObject1, i, colTypes);
                    if (!this.paramTypes[i].equals("INOUT"))
                        continue;
                    if (colIndexes[i] > 0)
                        this.stmt.registerOutParameter(i + 1, paramDBRowCache.getColumnType(colIndexes[i]));
                    else
                        this.stmt.registerOutParameter(i + 1, paramVariableTable.getType(this.paramNames[i]));
                } else {
                    if (!this.paramTypes[i].equals("OUT"))
                        continue;
                    if (colIndexes[i] > 0)
                        this.stmt.registerOutParameter(i + 1, paramDBRowCache.getColumnType(colIndexes[i]));
                    else
                        this.stmt.registerOutParameter(i + 1, paramVariableTable.getType(this.paramNames[i]));
                }
            }
        }
    }

    public void fetch(VariableTable paramVariableTable)
            throws SQLException {
        if (this.paramNames.length > 0)
            for (int i = 0; i < this.paramNames.length; i++) {
                Object localObject1 = null;
                if ((!this.paramTypes[i].equalsIgnoreCase("OUT"))
                        && (!this.paramTypes[i].equalsIgnoreCase("INOUT")))
                    continue;
                int k = paramVariableTable.getType(this.paramNames[i]);
                switch (k) {
                    case 2005:
                        Clob clob = this.stmt.getClob(i + 1);
                        if (clob == null) {
                            break;
                        }
                        Reader reader = clob.getCharacterStream();
                        if (reader == null) {
                            break;
                        }
                        localObject1 = readChars(reader);
                    case 2004:
                        Blob blob = this.stmt.getBlob(i + 1);
                        if (blob == null) {
                            break;
                        }
                        InputStream stream = blob.getBinaryStream();
                        if (stream == null) {
                            break;
                        }
                        localObject1 = readStream(stream);
                    case -3:
                    case -2:
                    case 1:
                    case 12:
                        localObject1 = this.stmt.getString(i + 1);
                        break;
                    case 91:
                        localObject1 = this.stmt.getDate(i + 1);
                        break;
                    case 92:
                        localObject1 = this.stmt.getTime(i + 1);
                        break;
                    case 93:
                        localObject1 = this.stmt.getTimestamp(i + 1);
                        break;
                    default:
                        localObject1 = this.stmt.getObject(i + 1);
                        if (!(localObject1 instanceof ResultSet))
                            break;
                        try {
                            ResultSet resultSet = (ResultSet) localObject1;
                            resultSet.close();
                        } catch (SQLException localSQLException) {
                        }
                        localObject1 = null;
                }
                try {
                    paramVariableTable.setValue(this.paramNames[i], localObject1);
                } catch (NumberFormatException localNumberFormatException) {
                }
            }
    }

    private Object readChars( Reader localObject2) {
        String value = null;
        char[] chars = new char[65536];
        try {
            int j = localObject2.read(chars);
            if (j > 0)
                value = String.valueOf(chars, 0, j);
            localObject2.close();
        } catch (IOException localIOException1) {
        }
        return value;
    }

    private String readStream(InputStream stream) {
        String string = null;
        byte[] buffer = new byte[65536];

        try {
            int j = stream.read(buffer);
            if (j > 0)
                string = new String(buffer, 0, j);
            stream.close();
        } catch (IOException localIOException2) {
        }
        return string;
    }
}
