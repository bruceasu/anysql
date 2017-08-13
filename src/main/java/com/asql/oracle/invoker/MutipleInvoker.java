/*
 * Copyright (C) 2017 Bruce Asu<bruceasu@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *  　　
 * 　　The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.asql.oracle.invoker;

import static com.asql.oracle.OracleCMDType.*;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVM;
import com.asql.core.util.TextUtils;
import com.asql.oracle.OracleSQLExecutor;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

/**
 * Created by suk on 2017/8/13.
 */
public class MutipleInvoker implements ModuleInvoker {
    OracleSQLExecutor executor;
    CommandLog out;
    CMDType cmdType;

    public MutipleInvoker(OracleSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCommandType();
    }

    @Override
    public boolean invoke(Command cmd) {
        int j = executor.getMultipleID(cmd.COMMAND);
        switch (j) {
            case ORACLE_LOB:
                time(cmd, this::procLOB);
                break;
            case ORACLE_LOBEXP:
                time(cmd, this::procLOBEXP);
                break;
            case ORACLE_LOBLEN:
                time(cmd, this::procLOBLEN);
                break;
            case ORACLE_LOBIMP:
                time(cmd, this::procLOBIMP);
                break;
            case ORACLE_EXPLAINPLAN:
                time(cmd, this::procExplainPlan);
                break;
            case ORACLE_EXPMVIEW:
                time(cmd, this::procExplainMView);
                break;
            case ORACLE_EXPREWRITE:
                time(cmd, this::procExplainRewrite);
                break;
            case ORACLE_UNLOAD:
                time(cmd, this::procUnload);
                break;
            case ORACLE_LOAD:
                time(cmd, this::procLoad);
                break;
            case ORACLE_CROSS:
                time(cmd, command -> {
                    out.println();
                    DBRowCache rowCacheOfSessionWait = executor.getDbRowCacheOfSessionWait();
                    DBRowCache rowCacheOfSessionStats = executor.getDbRowCacheOfSessionStats();
                    boolean flag = procCross(command);
                    executor.printSessionStats(rowCacheOfSessionStats);
                    executor.printSessionWait(rowCacheOfSessionWait);
                    executor.printExplain(cmd);
                    return flag;
                });
        }

        return true;
    }

    boolean procLOB(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_LOB]).size();
        String str1 = executor.skipWord(cmdLine, i);
        String file = null;
        String query = null;
        int j = str1.indexOf("<<");
        if (j >= 0) {
            query = str1.substring(0, j).trim();
            file = str1.substring(j + 2).trim();
            procLOBWRITE(query, file);
        } else {
            j = str1.indexOf(">>");
            if (j >= 0) {
                query = str1.substring(0, j).trim();
                file = str1.substring(j + 2).trim();
                procLOBREAD(query, file);
            } else {
                lobUsage();
                return false;
            }
        }
        return true;
    }

    boolean procLOBEXP(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_LOBEXP]).size();
        String str1 = executor.skipWord(cmdLine, i);
        str1 = str1.trim();
        int j = 0;
        long l = 0L;
        char[] arrayOfChar = new char[8192];
        byte[] arrayOfByte = new byte[8192];
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            out.println("Java VM 1.3 required to support this feature.");
            return false;
        }
        if (str1.length() == 0) {
            out.println("Usage:");
            out.println("  LOBEXP query");
            out.println("Note :");
            out.println("  Query should return tow column as following:");
            out.println("  col1 : CHAR or VARCHAR specify the filename.");
            out.println("  col2 : long/long raw/blob/clob field.");
            return false;
        }
        if (executor.checkNotConnected()) return false;
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        try {
            localSQLStatement = executor.prepareStatement(executor.database, str1, executor.sysVariable);
            localSQLStatement.bind(executor.sysVariable);
            executor.currentStmt = localSQLStatement.stmt;
            localResultSet = localSQLStatement.stmt.executeQuery();
            executor.resultSet = localResultSet;
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if ((localResultSetMetaData.getColumnCount() != 2) || ((localResultSetMetaData.getColumnType(1) != 12) && (localResultSetMetaData.getColumnType(1) != 1)) || ((localResultSetMetaData.getColumnType(2) != -1) && (localResultSetMetaData.getColumnType(2) != -4) && (localResultSetMetaData.getColumnType(2) != 2004) && (localResultSetMetaData.getColumnType(2) != 2005))) {
                out.println("Usage:");
                out.println("  LOBEXP query");
                out.println("Note :");
                out.println("  Query should return tow column as following:");
                out.println("  col1 : CHAR or VARCHAR specify the filename.");
                out.println("  col2 : long/long raw/blob/clob field.");
                try {
                    if (localResultSet != null)
                        localResultSet.close();
                } catch (SQLException localSQLException3) {
                    out.print(localSQLException3);
                }
                try {
                    if (localSQLStatement != null)
                        localSQLStatement.close();
                } catch (SQLException localSQLException4) {
                    out.print(localSQLException4);
                }
                return false;
            }
            j = localResultSetMetaData.getColumnType(2);
            while (localResultSet.next()) {
                String str2 = localResultSet.getString(1);
                if (str2 == null) {
                    out.println("The file name is null!");
                    continue;
                }
                out.print("Write to file: " + str2);
                l = 0L;
                File localFile = new File(str2);
                Object localObject1;
                Object localObject3;
                if (j == -1) {
                    localObject1 = localResultSet.getCharacterStream(2);
                    if (localObject1 != null)
                        try {
                            int k = 0;
                            localObject3 = new FileWriter(localFile);
                            while ((k = ((Reader) localObject1).read(arrayOfChar)) > 0) {
                                ((FileWriter) localObject3).write(arrayOfChar, 0, k);
                                l += k;
                            }
                            ((FileWriter) localObject3).close();
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException1) {
                            out.println();
                            out.print(localIOException1);
                        }
                } else if (j == -4) {
                    localObject1 = localResultSet.getBinaryStream(2);
                    if (localObject1 != null)
                        try {
                            int m = 0;
                            localObject3 = new FileOutputStream(localFile);
                            while ((m = ((InputStream) localObject1).read(arrayOfByte)) > 0) {
                                ((FileOutputStream) localObject3).write(arrayOfByte, 0, m);
                                l += m;
                            }
                            ((FileOutputStream) localObject3).close();
                            ((InputStream) localObject1).close();
                        } catch (IOException localIOException2) {
                            out.println();
                            out.print(localIOException2);
                        }
                } else {
                    Object localObject2;
                    Object localObject4;
                    if (j == 2005) {
                        localObject1 = localResultSet.getClob(2);
                        if (localObject1 != null) {
                            localObject2 = ((Clob) localObject1).getCharacterStream();
                            if (localObject2 != null)
                                try {
                                    int n = 0;
                                    localObject4 = new FileWriter(localFile);
                                    while ((n = ((Reader) localObject2).read(arrayOfChar)) > 0) {
                                        ((FileWriter) localObject4).write(arrayOfChar, 0, n);
                                        l += n;
                                    }
                                    ((FileWriter) localObject4).close();
                                    ((Reader) localObject2).close();
                                } catch (IOException localIOException3) {
                                    out.println();
                                    out.print(localIOException3);
                                }
                        }
                    } else if (j == 2004) {
                        localObject1 = localResultSet.getBlob(2);
                        if (localObject1 != null) {
                            localObject2 = ((Blob) localObject1).getBinaryStream();
                            if (localObject2 != null)
                                try {
                                    int i1 = 0;
                                    localObject4 = new FileOutputStream(localFile);
                                    while ((i1 = ((InputStream) localObject2).read(arrayOfByte)) > 0) {
                                        ((FileOutputStream) localObject4).write(arrayOfByte, 0, i1);
                                        l += i1;
                                    }
                                    ((FileOutputStream) localObject4).close();
                                    ((InputStream) localObject2).close();
                                } catch (IOException localIOException4) {
                                    out.println();
                                    out.print(localIOException4);
                                }
                        }
                    }
                }
                out.println(" , bytes=" + l);
            }
            out.println("Command succeed.");
        } catch (Exception localException) {
            out.print(localException);
        }
        executor.currentStmt = null;
        executor.resultSet = null;
        executor.clearWarnings(executor.database, out);
        try {
            if (localResultSet != null)
                localResultSet.close();
        } catch (SQLException localSQLException1) {
            out.print(localSQLException1);
        }
        try {
            if (localSQLStatement != null)
                localSQLStatement.close();
        } catch (SQLException localSQLException2) {
            out.print(localSQLException2);
        }
        return true;
    }

    boolean procLOBLEN(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_LOBLEN]).size();
        String str = executor.skipWord(cmdLine, i);
        str = str.trim();
        int j = 0;
        long l = 0L;
        char[] arrayOfChar = new char[8192];
        byte[] arrayOfByte = new byte[8192];
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            out.println("Java VM 1.3 required to support this feature.");
            return false;
        }
        if (str.length() == 0) {
            out.println("Usage:");
            out.println("  LOBLEN query");
            out.println("Note :");
            out.println("  Query should return one column as following:");
            out.println("  col1 : long/long raw/blob/clob field.");
            return false;
        }
        if (executor.checkNotConnected()) return false;
        SQLStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = executor.prepareStatement(executor.database, str, executor.sysVariable);
            statement.bind(executor.sysVariable);
            executor.currentStmt = statement.stmt;
            resultSet = statement.stmt.executeQuery();
            executor.resultSet = resultSet;
            ResultSetMetaData localResultSetMetaData = resultSet.getMetaData();
            if ((localResultSetMetaData.getColumnCount() != 1) || ((localResultSetMetaData.getColumnType(1) != 12) && (localResultSetMetaData.getColumnType(1) != 1) && (localResultSetMetaData.getColumnType(1) != -1) && (localResultSetMetaData.getColumnType(1) != -4) && (localResultSetMetaData.getColumnType(1) != 2004) && (localResultSetMetaData.getColumnType(1) != 2005))) {
                out.println("Usage:");
                out.println("  LOBLEN query");
                out.println("Note :");
                out.println("  Query should return one column as following:");
                out.println("  col1 : long/long raw/blob/clob field.");
                try {
                    if (resultSet != null)
                        resultSet.close();
                } catch (SQLException localSQLException3) {
                    out.print(localSQLException3);
                }
                try {
                    if (statement != null)
                        statement.close();
                } catch (SQLException localSQLException4) {
                    out.print(localSQLException4);
                }
                return true;
            }
            j = localResultSetMetaData.getColumnType(1);
            while (resultSet.next()) {
                l = 0L;
                Object localObject1;
                if (j == -1) {
                    localObject1 = resultSet.getCharacterStream(1);
                    if (localObject1 != null)
                        try {
                            int k = 0;
                            while ((k = ((Reader) localObject1).read(arrayOfChar)) > 0)
                                l += k;
                            ((Reader) localObject1).close();
                        } catch (IOException localIOException1) {
                            out.println();
                            out.print(localIOException1);
                        }
                } else if (j == -4) {
                    localObject1 = resultSet.getBinaryStream(1);
                    if (localObject1 != null)
                        try {
                            int m = 0;
                            while ((m = ((InputStream) localObject1).read(arrayOfByte)) > 0)
                                l += m;
                            ((InputStream) localObject1).close();
                        } catch (IOException localIOException2) {
                            out.println();
                            out.print(localIOException2);
                        }
                } else {
                    Object localObject2;
                    if (j == 2005) {
                        localObject1 = resultSet.getClob(1);
                        if (localObject1 != null) {
                            localObject2 = ((Clob) localObject1).getCharacterStream();
                            if (localObject2 != null)
                                try {
                                    int n = 0;
                                    while ((n = ((Reader) localObject2).read(arrayOfChar)) > 0)
                                        l += n;
                                    ((Reader) localObject2).close();
                                } catch (IOException localIOException3) {
                                    out.println();
                                    out.print(localIOException3);
                                }
                        }
                    } else if (j == 2004) {
                        localObject1 = resultSet.getBlob(1);
                        if (localObject1 != null) {
                            localObject2 = ((Blob) localObject1).getBinaryStream();
                            if (localObject2 != null)
                                try {
                                    int i1 = 0;
                                    while ((i1 = ((InputStream) localObject2).read(arrayOfByte)) > 0)
                                        l += i1;
                                    ((InputStream) localObject2).close();
                                } catch (IOException localIOException4) {
                                    out.println();
                                    out.print(localIOException4);
                                }
                        }
                    }
                }
                out.println(l + "," + l / 1024L);
            }
        } catch (Exception localException) {
            out.print(localException);
            return false;
        } finally {
            executor.currentStmt = null;
            executor.resultSet = null;
            executor.clearWarnings(executor.database, out);
            closeQuietly(resultSet);
            closeQuietly(statement);
        }
        return true;
    }

    boolean procLOBIMP(String cmdLine) {
        long l1 = 0L;
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_LOBIMP]).size();
        String str1 = executor.skipWord(cmdLine, i);
        str1 = str1.trim();
        char[] arrayOfChar = new char[8192];
        byte[] arrayOfByte = new byte[8192];
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 4)) {
            out.println("Java VM 1.4 required to support this feature.");
            return false;
        }
        if (str1.length() == 0) {
            lobImpUsage();
            return false;
        }
        if (executor.checkNotConnected()) return false;
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        try {
            localSQLStatement = executor.prepareStatement(executor.database, str1, executor.sysVariable);
            localSQLStatement.bind(executor.sysVariable);
            executor.currentStmt = localSQLStatement.stmt;
            localResultSet = localSQLStatement.stmt.executeQuery();
            executor.resultSet = localResultSet;
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if ((localResultSetMetaData.getColumnCount() != 2)
                    || ((localResultSetMetaData.getColumnType(1) != 12)
                    && (localResultSetMetaData.getColumnType(1) != 1))
                    || ((localResultSetMetaData.getColumnType(2) != 2004)
                    && (localResultSetMetaData.getColumnType(2) != 2005))) {
                lobImpUsage();
                closeQuietly(localResultSet);
                closeQuietly(localSQLStatement);
                return false;
            }
            while (localResultSet.next()) {
                String str2 = localResultSet.getString(1);
                File localFile = new File(str2);
                if (!localFile.exists()) {
                    out.println("File " + str2 + " does not exists!");
                    continue;
                }
                if (!localFile.isFile()) {
                    out.println(str2 + " is not a valid file!");
                    continue;
                }
                if (!localFile.canRead()) {
                    out.println("Cannot read file " + str2 + "!");
                    continue;
                }
                long l2;
                if (localResultSetMetaData.getColumnType(2) == 2005) {
                    Clob clob = localResultSet.getClob(2);
                    l2 = 0L;
                    if (clob != null)
                        clob.truncate(l1);
                    try (Writer writer = clob.setCharacterStream(l1)) {
                        int j = 0;
                        FileReader reader = new FileReader(localFile);
                        while ((j = reader.read(arrayOfChar)) > 0) {
                            writer.write(arrayOfChar, 0, j);
                            l2 += j;
                        }
                        reader.close();
                    } catch (IOException e) {
                        out.print(e);
                    }
                } else if (localResultSetMetaData.getColumnType(2) == 2004) {
                    Blob blob = localResultSet.getBlob(2);
                    if (blob != null) {
                        l2 = 0L;
                        blob.truncate(l1);
                        try (OutputStream stream = blob.setBinaryStream(l1);
                             FileInputStream inputStream = new FileInputStream(localFile)) {
                            int k = 0;
                            while ((k = inputStream.read(arrayOfByte)) > 0) {
                                stream.write(arrayOfByte, 0, k);
                                l2 += k;
                            }
                        } catch (IOException e) {
                            out.print(e);
                        }
                    }
                }
                out.println("File " + str2 + " loaded.");
            }
            out.println("Command succeed.");
        } catch (Exception localException) {
            out.print(localException);
        }
        executor.currentStmt = null;
        executor.resultSet = null;
        executor.clearWarnings(executor.database, out);
        closeQuietly(localResultSet);
        closeQuietly(localSQLStatement);
        return true;
    }

    boolean procExplainPlan(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_EXPLAINPLAN]).size();
        String str = executor.skipWord(cmdLine, i);
        if (executor.checkNotConnected()) return false;
        DBRowCache localDBRowCache = executor.getExplainPlan(str);
        if ((localDBRowCache != null) && (localDBRowCache.getRowCount() > 0))
            executor.showDBRowCache(localDBRowCache, true);

        return true;
    }

    boolean procExplainMView(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_EXPMVIEW]).size();
        VariableTable table = new VariableTable();
        String str1 = executor.skipWord(cmdLine, i);
        String str2 = "P" + Math.random();
        DBRowCache rowCache = null;
        SQLStatement statement = null;
        SQLCallable callable = null;
        if (executor.checkNotConnected()) return false;
        try {
            table.add("PLAN_STMT_ID", 12);
            table.setValue("PLAN_STMT_ID", str2);
            table.add("PLAN_STMT_SQL", 12);
            table.setValue("PLAN_STMT_SQL", str1);
            statement = executor.prepareStatement(executor.database, "DELETE FROM MV_CAPABILITIES_TABLE WHERE STATEMENT_ID = :PLAN_STMT_ID", executor.sysVariable);
            if (statement != null) {
                statement.bind(table);
                statement.stmt.execute();
            }
            callable = executor.prepareCall(executor.database, "DBMS_MVIEW.EXPLAIN_MVIEW(MV=>:PLAN_STMT_SQL,STMT_ID=>:PLAN_STMT_ID)", table);
            callable.bind(table);
            callable.stmt.execute();
            rowCache = executor.executeQuery(executor.database, "select seq,CAPABILITY_NAME,POSSIBLE Y,MSGTXT from MV_CAPABILITIES_TABLE WHERE STATEMENT_ID = :PLAN_STMT_ID order by seq", table);
            executor.showDBRowCache(rowCache, true);
            if (statement != null) {
                statement.bind(table);
                statement.stmt.execute();
            }
            executor.database.commit();
        } catch (SQLException e) {
            out.print(e);
            return false;
        } finally {
            closeQuietly(statement);
            closeQuietly(callable);
        }
        return true;
    }

    boolean procExplainRewrite(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_EXPREWRITE]).size();
        VariableTable table = new VariableTable();
        String str1 = executor.skipWord(cmdLine, i);
        String str2 = "P" + Math.random();
        DBRowCache cache = null;
        SQLStatement statement = null;
        SQLCallable callable = null;
        if (executor.checkNotConnected()) return false;
        try {
            table.add("PLAN_STMT_ID", 12);
            table.setValue("PLAN_STMT_ID", str2);
            table.add("PLAN_STMT_SQL", 12);
            table.setValue("PLAN_STMT_SQL", str1);
            statement = executor.prepareStatement(executor.database, "DELETE FROM REWRITE_TABLE WHERE STATEMENT_ID = :PLAN_STMT_ID", executor.sysVariable);
            if (statement != null) {
                statement.bind(table);
                statement.stmt.execute();
            }
            callable = executor.prepareCall(executor.database, "DBMS_MVIEW.EXPLAIN_REWRITE(QUERY=>:PLAN_STMT_SQL,STATEMENT_ID=>:PLAN_STMT_ID)", table);
            callable.bind(table);
            callable.stmt.execute();
            cache = executor.executeQuery(executor.database, "select MESSAGE from REWRITE_TABLE WHERE STATEMENT_ID = :PLAN_STMT_ID", table);
            executor.showDBRowCache(cache, true);
            callable.close();
            if (statement != null) {
                statement.bind(table);
                statement.stmt.execute();
            }
            statement.close();
            executor.database.commit();
        } catch (SQLException localSQLException1) {
            out.print(localSQLException1);
            return false;
        } finally {
            closeQuietly(statement);
            closeQuietly(callable);
        }
        return true;
    }

    boolean procLoad(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_LOAD]).size();
        String str1 = executor.skipWord(cmdLine, i);
        OptionCommand optionCommand = new OptionCommand(str1);
        str1 = optionCommand.getCommand();
        String str2 = optionCommand.getOption("F", "|");
        int j = optionCommand.getInt("S", 0);
        String file = null;
        String query = null;
        if (executor.checkNotConnected()) return false;
        if (j < 0)
            j = 0;
        int k = str1.indexOf("<<");
        if (k >= 0) {
            query = str1.substring(0, k).trim();
            file = str1.substring(k + 2).trim();
        } else {
            loadUsage();
            return false;
        }
        file = file.trim();
        query = query.trim();
        if ((query.length() == 0) || (file.length() == 0)) {
            loadUsage();
            return false;
        }
        file = executor.sysVariable.parseString(file);
        SQLStatement statement = null;
        BufferedReader reader = null;
        int m = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            for (int n = 0; n < j; n++)
                reader.readLine();
        } catch (IOException localIOException1) {
            out.println(localIOException1.getMessage());
            return false;
        }
        try {
            statement = executor.prepareStatement(executor.database, query, executor.sysVariable);
            do {
                executor.loadBuffer.deleteAllRow();
                executor.loadBuffer.read(reader, str2, 200);
                statement.executeBatch(executor.sysVariable, executor.loadBuffer, 1, executor.loadBuffer.getRowCount());
                executor.database.commit();
                m += executor.loadBuffer.getRowCount();
            }
            while (executor.loadBuffer.getRowCount() == 200);
            out.println("Command Completed.");
        } catch (Exception e) {
            out.println(e.getMessage());
            return false;
        } finally {
            closeQuietly(statement);
            closeQuietly(reader);
        }
        out.println(m + " rows loaded!");
        return true;
    }

    boolean procUnload(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[5]).size();
        String str1 = executor.skipWord(cmdLine, i);
        OptionCommand optionCommand = new OptionCommand(str1);
        str1 = optionCommand.getCommand();
        String optF = optionCommand.getOption("F", "|");
        String optR = optionCommand.getOption("R", "\\r\\n");
        boolean optH = optionCommand.getBoolean("H", true);
        String file = null;
        String query = null;
        if (executor.checkNotConnected()) return false;
        int j = str1.indexOf(">>");
        if (j >= 0) {
            query = str1.substring(0, j).trim();
            file = str1.substring(j + 2).trim();
        } else {
            unloadUsage();
            return false;
        }
        file = file.trim();
        query = query.trim();
        if ((query.length() == 0) || (file.length() == 0)) {
            unloadUsage();
            return false;
        }
        file = executor.sysVariable.parseString(file);
        SQLStatement statement = null;
        ResultSet resultSet = null;
        PrintStream printStream = null;
        File localFile = new File(file);
        try {
            long l = System.currentTimeMillis();
            statement = executor.prepareStatement(executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            executor.currentStmt = statement.stmt;
            resultSet = statement.stmt.executeQuery();
            executor.resultSet = resultSet;
            out.println("Query executed in " + DBOperation.getElapsed(System.currentTimeMillis() - l));
            if (file.trim().endsWith(".gz"))
                printStream = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(localFile), 65536)));
            else
                printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(localFile), 262144));
            writeData(printStream, resultSet, executor.parseRecord(optF), executor.parseRecord(optR), optH);
            printStream.close();
            resultSet.close();
            statement.close();
            executor.resultSet = null;
            executor.currentStmt = null;
            return true;
        } catch (SQLException | IOException e) {
            out.print(e);
            return false;
        } finally {
            closeQuietly(printStream);
            closeQuietly(resultSet);
            closeQuietly(statement);
        }
    }

    boolean procCross(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ORACLE_CROSS]).size();
        String str = executor.skipWord(cmdLine, i);
        if (executor.checkNotConnected()) return false;
        try {
            DBRowCache localDBRowCache = DBOperation.crossQuery(executor.database, str, executor.sysVariable);
            if (localDBRowCache.getColumnCount() > 0) {
                localDBRowCache.getWidth(false);
                out.print(localDBRowCache);
            } else {
                out.println("Cross query SQL should return three columns!");
                return false;
            }
        } catch (SQLException localSQLException) {
            out.print(localSQLException);
            return false;
        }
        return true;
    }

    void procLOBREAD(String cmdLine1, String cmdLine2) {
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 3)) {
            out.println("Java VM 1.3 or above required to support this feature.");
            return;
        }
        if ((cmdLine2 == null) || (cmdLine1 == null) || (cmdLine2.length() == 0) || (cmdLine1.length() == 0)) {
            lobUsage();
            return;
        }
        cmdLine2 = executor.sysVariable.parseString(cmdLine2);
        if (executor.checkNotConnected()) return;
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        File localFile = new File(cmdLine2);
        try {
            localSQLStatement = executor.prepareStatement(executor.database, cmdLine1, executor.sysVariable);
            localSQLStatement.bind(executor.sysVariable);
            localResultSet = localSQLStatement.stmt.executeQuery();
            ResultSetMetaData localResultSetMetaData = localResultSet.getMetaData();
            if (localResultSet.next()) {

                if (localResultSetMetaData.getColumnType(1) == -1) {
                    Reader reader;
                    char[] buffer;
                    FileWriter writer;
                    reader = localResultSet.getCharacterStream(1);
                    if (reader != null) {
                        buffer = new char[16384];
                        try {
                            int i = 0;
                            writer = new FileWriter(localFile);
                            while ((i = reader.read(buffer)) > 0) {
                                writer.write(buffer, 0, i);
                            }
                            writer.close();
                            reader.close();
                        } catch (IOException localIOException1) {
                            out.print(localIOException1);
                        }
                    }
                } else if (localResultSetMetaData.getColumnType(1) == -4) {
                    InputStream inputStream;
                    byte[] buffer;
                    FileOutputStream outputStream;
                    inputStream = localResultSet.getBinaryStream(1);
                    if (inputStream != null) {
                        buffer = new byte[16384];
                        try {
                            int j = 0;
                            outputStream = new FileOutputStream(localFile);
                            while ((j = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, j);
                            }
                            outputStream.close();
                            inputStream.close();
                        } catch (IOException localIOException2) {
                            out.print(localIOException2);
                        }
                    }
                } else {
                    if (localResultSetMetaData.getColumnType(1) == 2005) {
                        Clob clob;
                        char[] buffer;
                        Reader reader;
                        FileWriter writer;
                        clob = localResultSet.getClob(1);
                        if (clob != null) {
                            reader = ((Clob) clob).getCharacterStream();
                            if (reader != null) {
                                buffer = new char[16384];
                                try {
                                    int k = 0;
                                    writer = new FileWriter(localFile);
                                    while ((k = reader.read(buffer)) > 0) {
                                        writer.write(buffer, 0, k);
                                    }
                                    writer.close();
                                    reader.close();
                                } catch (IOException localIOException3) {
                                    out.print(localIOException3);
                                }
                            }
                        }
                    } else if (localResultSetMetaData.getColumnType(1) == 2004) {
                        Blob blob = localResultSet.getBlob(1);
                        InputStream inputStream;
                        byte[] buffer;
                        FileOutputStream outputStream;
                        if (blob != null) {
                            inputStream = blob.getBinaryStream();
                            if (inputStream != null) {
                                buffer = new byte[16384];
                                try {
                                    int m = 0;
                                    outputStream = new FileOutputStream(localFile);
                                    while ((m = inputStream.read(buffer)) > 0) {
                                        outputStream.write(buffer, 0, m);
                                    }
                                    outputStream.close();
                                    inputStream.close();
                                } catch (IOException localIOException4) {
                                    out.print(localIOException4);
                                }
                            }
                        }
                    }
                }
                out.println("Command succeed.");
            } else {
                out.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            out.print(localException);
        } finally {
            executor.clearWarnings(executor.database, out);
            closeQuietly(localResultSet);
            closeQuietly(localSQLStatement);
        }
    }

    void procLOBWRITE(String cmdLine1, String cmdLine2) {
        long l1 = 0L;
        if ((JavaVM.MAIN_VERSION == 1) && (JavaVM.MINOR_VERSION < 4)) {
            out.println("Java VM 1.4 or above required to support this feature.");
            return;
        }
        if (((cmdLine2 == null) && (cmdLine1 == null)) || (cmdLine2.length() == 0) || (cmdLine1.length() == 0)) {
            lobUsage();
            return;
        }
        cmdLine2 = executor.sysVariable.parseString(cmdLine2);
        if (executor.checkNotConnected()) return;
        SQLStatement statement = null;
        ResultSet resultSet = null;
        File localFile = new File(cmdLine2);
        if (!localFile.exists()) {
            out.println("File " + cmdLine2 + " does not exists!");
            return;
        }
        if (!localFile.isFile()) {
            out.println(cmdLine2 + " is not a valid file!");
            return;
        }
        if (!localFile.canRead()) {
            out.println("Cannot read file " + cmdLine2 + "!");
            return;
        }
        try {
            statement = executor.prepareStatement(executor.database, cmdLine1, executor.sysVariable);
            statement.bind(executor.sysVariable);
            resultSet = statement.stmt.executeQuery();
            ResultSetMetaData localResultSetMetaData = resultSet.getMetaData();
            if (resultSet.next()) {
                if (localResultSetMetaData.getColumnType(1) == 2005) {
                    Clob clob = resultSet.getClob(1);
                    long l2 = 0L;
                    if (clob != null) {
                        char[] arrayOfChar = new char[16384];
                        clob.truncate(l1);
                        try (Writer writer = clob.setCharacterStream(l1);
                             FileReader fileReader = new FileReader(localFile);){
                            int i = 0;
                            while ((i = fileReader.read(arrayOfChar)) > 0) {
                                writer.write(arrayOfChar, 0, i);
                                l2 += i;
                            }
                        } catch (IOException localIOException1) {
                            out.print(localIOException1);
                        }
                    }
                } else if (localResultSetMetaData.getColumnType(1) == 2004) {
                    Blob blob = resultSet.getBlob(1);
                    if (blob != null) {
                        byte[] arrayOfByte = new byte[16384];
                        long l3 = 0L;
                        try (OutputStream outputStream = blob.setBinaryStream(l1);
                             FileInputStream fileInputStream = new FileInputStream(localFile);){
                            int j = 0;
                            blob.truncate(l1);
                            while ((j = fileInputStream.read(arrayOfByte)) > 0) {
                                outputStream.write(arrayOfByte, 0, j);
                                l3 += j;
                            }
                        } catch (IOException localIOException2) {
                            out.print(localIOException2);
                        }
                    }
                }
                out.println("Command succeed.");
            } else {
                out.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            out.print(localException);
        } finally {
            executor.clearWarnings(executor.database, out);
            closeQuietly(resultSet);
            closeQuietly(statement);
        }

    }

    private boolean time(Command cmd, Function<String, Boolean> function) {
        out.println();
        long l1 = System.currentTimeMillis();
        boolean r = function.apply(cmd.COMMAND);
        long l2 = System.currentTimeMillis();
        executor.printCost(l2, l1);
        out.println();
        return r;
    }

    private void lobImpUsage() {
        out.println("Usage:");
        out.println("  LOBIMP query");
        out.println("Note :");
        out.println("  Query should return tow column as following:");
        out.println("  col1 : CHAR or VARCHAR specify the filename.");
        out.println("  col2 : blob/clob field.");
    }

    private void loadUsage() {
        out.println("Usage:");
        out.println("  LOAD -option val query << file");
        out.println("Note :");
        out.println("  -F change field seperator(Default:|)");
        out.println("  -S skip lines (values > 0)");
    }

    private void unloadUsage() {
        out.println("Usage:");
        out.println("  UNLOAD -option val query >> file");
        out.println("Note :");
        out.println("  -F change field seperator(Default:|)");
        out.println("  -R change record seperator(Default:\\r\\n)");
        out.println("  -H display field name {ON|OFF}");
    }

    private void lobUsage() {
        out.println("Usage:");
        out.println("  LOB query >> file");
        out.println("  LOB query << file");
        out.println("Note :");
        out.println("  >> mean export long/long raw/blob/clob to a file ");
        out.println("  << mean import a file to blob/clob field, the query");
        out.println("     should include the for update clause");
    }

    private void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Throwable ignore) {
        }
    }

    private long writeData(PrintStream paramPrintStream,
                          ResultSet paramResultSet,
                          String cmdLine1,
                          String cmdLine2,
                          boolean paramBoolean)
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
            paramPrintStream.print(cmdLine1);
        }
        if (paramBoolean)
            paramPrintStream.print(cmdLine2);
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
                    paramPrintStream.print(cmdLine1);
                else
                    paramPrintStream.print(cmdLine2);
            }
            if (l1 % 100000L != 0L)
                continue;
            executor.getCommandLog().println(executor.lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(System.currentTimeMillis() - l2));
        }
        l2 = System.currentTimeMillis() - l2;
        if (l1 % 100000L != 0L)
            executor.getCommandLog().println(executor.lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(l2));
        if (l2 > 0L)
            executor.getCommandLog().println("Done, total:" + l1 + " , avg:" + l1 * 1000L / l2 + " rows/s.");
        return l1;
    }

}
