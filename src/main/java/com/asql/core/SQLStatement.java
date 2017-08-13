package com.asql.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLStatement implements AutoCloseable{
    public PreparedStatement stmt = null;
    protected String sourceSQL = null;
    protected String destSQL = null;
    protected String[] paramNames = new String[0];
    protected String[] paramTypes = new String[0];

    public SQLStatement(){};
    
    public SQLStatement(PreparedStatement paramPreparedStatement, SQLQuery paramSQLQuery) {
        this.stmt = paramPreparedStatement;
        this.sourceSQL = paramSQLQuery.getSourceSQL();
        this.destSQL = paramSQLQuery.getDestSQL();
        this.paramNames = paramSQLQuery.getParamNames();
        this.paramTypes = paramSQLQuery.getParamTypes();
    }

    public String getSourceSQL() {
        return this.sourceSQL;
    }

    public String getDestSQL() {
        return this.destSQL;
    }

    public String[] getParamNames() {
        return this.paramNames;
    }

    public String[] getParamTypes() {
        return this.paramTypes;
    }

    public void close()
            throws SQLException {
        if (this.stmt != null) {
            this.stmt.close();
            this.stmt = null;
        }
    }

    public boolean getMoreResults()
            throws SQLException {
        return this.stmt.getMoreResults();
    }

    public int getUpdateCount()
            throws SQLException {
        return this.stmt.getUpdateCount();
    }

    public ResultSet getResultSet()
            throws SQLException {
        return this.stmt.getResultSet();
    }

    public boolean execute(VariableTable paramVariableTable)
            throws SQLException {
        bind(paramVariableTable);
        return this.stmt.execute();
    }

    public void bind(VariableTable paramVariableTable)
            throws SQLException {
        try {
            this.stmt.clearParameters();
        } catch (SQLException localSQLException) {
        }
        if (this.paramNames.length > 0)
            for (int i = 0; i < this.paramNames.length; i++) {
                int j = paramVariableTable.getType(this.paramNames[i]);
                Object localObject1 = paramVariableTable.getValue(this.paramNames[i]);
                setObject(i, j, localObject1);
            }
    }


    public void bind(VariableTable paramVariableTable, DBRowCache paramDBRowCache, int paramInt)
            throws SQLException {
        CreateTypes createTypes = new CreateTypes(paramVariableTable, paramDBRowCache).invoke();
        int[] colIndexes = createTypes.getColIndexes();
        int[] colTypes = createTypes.getColTypes();
        setObject(paramVariableTable, paramDBRowCache,  colIndexes, colTypes, 1);
    }

    public int[] executeBatch(DBRowCache paramDBRowCache)
            throws SQLException {
        return executeBatch(new VariableTable(), paramDBRowCache, 1, paramDBRowCache.getRowCount());
    }

    public int[] executeBatch(DBRowCache paramDBRowCache, int paramInt1, int paramInt2)
            throws SQLException {
        return executeBatch(new VariableTable(), paramDBRowCache, paramInt1, paramInt2);
    }

    public int[] executeBatch(VariableTable paramVariableTable, DBRowCache paramDBRowCache)
            throws SQLException {
        return executeBatch(paramVariableTable, paramDBRowCache, 1, paramDBRowCache.getRowCount());
    }

    public int[] executeBatch(VariableTable paramVariableTable,
                              DBRowCache paramDBRowCache,
                              int paramInt1,
                              int paramInt2)
            throws SQLException {
        CreateTypes createTypes = new CreateTypes(paramVariableTable, paramDBRowCache).invoke();
        int[] colIndexes = createTypes.getColIndexes();
        int[] colTypes = createTypes.getColTypes();
        this.stmt.clearBatch();
        for (int j = paramInt1; j <= paramInt2; j++)
            try {
                setObject(paramVariableTable, paramDBRowCache, colIndexes, colTypes, j);
                this.stmt.addBatch();
            } catch (SQLException localSQLException) {
                this.stmt.clearBatch();
                throw localSQLException;
            }
        return this.stmt.executeBatch();
    }

    protected void setObject(int i, int j, Object localObject1) throws SQLException {
        if (localObject1 == null) {
            this.stmt.setNull(i + 1, 1);
        } else {
            Object localObject2;
            switch (j) {
                case -3:
                case -2:
                case 1:
                case 12:
                    this.stmt.setString(i + 1, localObject1.toString());
                    break;
                case -1:
                case 2005:
                    if (j == 2005) {
                        setType2005(i, localObject1.toString());
                    } else {
                        localObject2 = new StringReader(localObject1.toString());
                        this.stmt.setCharacterStream(i + 1, (Reader) localObject2, 65536);
                    }
                    break;
                case -4:
                case 2004:
                    setType2004(i, localObject1.toString());
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
                    this.stmt.setNull(i + 1, 1);
                    break;
                default:
                    this.stmt.setObject(i + 1, localObject1);
            }
        }
    }

    protected void setObject(VariableTable paramVariableTable,
                           DBRowCache paramDBRowCache,
                           int[] colIndexes,
                           int[] colTypes,
                           int j) throws SQLException {

        if (this.paramNames.length > 0) {
            Object[] arrayOfObject = paramDBRowCache.getRow(j);
            for (int i = 0; i < this.paramNames.length; i++) {
                Object localObject1;
                if (colIndexes[i] > 0)
                    localObject1 = arrayOfObject[(colIndexes[i] - 1)];
                else
                    localObject1 = paramVariableTable.getValue(this.paramNames[i]);
                setParamObject(localObject1, i, colTypes);
            }
        }
    }

    protected void setParamObject(Object localObject1, int i, int[] colTypes) throws SQLException {
        if (localObject1 == null) {
            this.stmt.setNull(i + 1, 1);
        } else {
            Object localObject2;
            switch (colTypes[i]) {
                case -1:
                case 2005:
                    if (colTypes[i] == 2005) {
                        setType2005(i, localObject1.toString());
                    } else {
                        localObject2 = new StringReader(localObject1.toString());
                        this.stmt.setCharacterStream(i + 1, (Reader) localObject2, 65536);
                    }
                    break;
                case -4:
                case 2004:
                    setType2004(i, localObject1.toString());
                    break;
                case -3:
                case -2:
                case 1:
                case 12:
                    this.stmt.setString(i + 1, localObject1.toString());
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
                    this.stmt.setNull(i + 1, 1);
                    break;
                default:
                    this.stmt.setObject(i + 1, localObject1);
            }
        }
    }

    protected void setType2004(int i, String path) throws SQLException {
        File file= new File(path);
        if (( file.exists()) && ( file.isFile()) && (file.canRead()))
            try {
                FileInputStream stream = new FileInputStream( file);
                this.stmt.setBinaryStream(i + 1, stream, file.length());
            } catch (IOException localIOException2) {
            }
        else
            this.stmt.setNull(i + 1, 1);
    }

    protected void setType2005(int i, String path) throws SQLException {
        File file = new File(path);
        if ((file.exists()) && (file.isFile()) && (file.canRead())) {
            try {
                BufferedReader localBufferedReader = new BufferedReader(new FileReader(file));
                this.stmt.setCharacterStream(i + 1, localBufferedReader, file.length());
            } catch (IOException localIOException1) {
            }
        } else {
            this.stmt.setNull(i + 1, 1);
        }
    }

    public class CreateTypes {
        private VariableTable paramVariableTable;
        private DBRowCache paramDBRowCache;
        private int[] colIndexes;
        private int[] colTypes;

        public CreateTypes(VariableTable paramVariableTable, DBRowCache paramDBRowCache) {
            this.paramVariableTable = paramVariableTable;
            this.paramDBRowCache = paramDBRowCache;
        }

        public int[] getColIndexes() {
            return colIndexes;
        }

        public int[] getColTypes() {
            return colTypes;
        }

        public CreateTypes invoke() {
            colIndexes = new int[0];
            colTypes = new int[0];
            if (SQLStatement.this.paramNames.length > 0) {
                colIndexes = new int[SQLStatement.this.paramNames.length];
                colTypes = new int[SQLStatement.this.paramNames.length];
                for (int i = 0; i < SQLStatement.this.paramNames.length; i++) {
                    colIndexes[i] = paramDBRowCache.findColumn(SQLStatement.this.paramNames[i]);
                    if (colIndexes[i] > 0)
                        colTypes[i] = paramDBRowCache.getColumnType(colIndexes[i]);
                    else
                        colTypes[i] = paramVariableTable.getType(SQLStatement.this.paramNames[i]);
                }
            }
            return this;
        }
    }
}
