package com.asql.core;

import com.asql.core.log.CommandLog;
import java.sql.*;

public class SQLExecutor {
    public void clearWarnings(Connection connection, CommandLog log) {
        try {
            SQLWarning warnings = connection.getWarnings();
            if (warnings != null) {
                log.print(warnings);
                while ((warnings = warnings.getNextWarning()) != null)
                    log.print(warnings);
            }
            connection.clearWarnings();
        } catch (SQLException localSQLException) {
            log.print(localSQLException);
        }
    }

    public void doCommit(Connection connection, CommandLog log) {
        try {
            connection.commit();
            log.println("Transaction commit.");
        } catch (SQLException localSQLException) {
            log.print(localSQLException);
        }
        clearWarnings(connection, log);
    }

    public void doRollback(Connection connection, CommandLog log) {
        try {
            connection.rollback();
            log.println("Transaction rollback.");
        } catch (SQLException localSQLException) {
            log.print(localSQLException);
        }
        clearWarnings(connection, log);
    }

    public void executeSQL(Connection connection,
                           String sql,
                           CommandLog log) {
        VariableTable table = new VariableTable();
        executeSQL(connection, sql, table, log);
    }

    public void executeSQL(Connection connection,
                           String sql,
                           VariableTable table,
                           CommandLog log) {
        SQLStatement localSQLStatement = null;
        if (sql == null)
            return;
        if (sql.equalsIgnoreCase("COMMIT")) {
            doCommit(connection, log);
            return;
        }
        if (sql.equalsIgnoreCase("ROLLBACK")) {
            doRollback(connection, log);
            return;
        }
        PreparedStatement stmt = localSQLStatement.stmt;
        try {
            if ((sql.endsWith("/G")) || (sql.endsWith("/g"))) {
                log.setFormDisplay(true);
                localSQLStatement = DBOperation.prepareStatement(connection, sql.substring(1, sql.length() - 2), table);
            } else {
                localSQLStatement = DBOperation.prepareStatement(connection, sql, table);
            }
            if (stmt == null)
                return;
            localSQLStatement.bind(table);
            boolean bool = stmt.execute();
            displayResultSet(stmt, bool, log);
        } catch (SQLException localSQLException2) {
            log.print(localSQLException2);
        } finally {
            if ((localSQLStatement != null) && (stmt != null))
                try {
                    stmt.close();
                } catch (SQLException localSQLException3) {
                }
        }
        log.setFormDisplay(false);
        clearWarnings(connection, log);
    }


    public void executeCall(Connection connection,
                            String sql,
                            CommandLog log) {
        VariableTable localVariableTable = new VariableTable();
        executeCall(connection, sql, localVariableTable, log);
    }

    public void executeCall(Connection connection,
                            String sql,
                            VariableTable paramVariableTable,
                            CommandLog paramCommandLog) {
        SQLCallable localSQLCallable = null;
        if (sql == null)
            return;
        if (sql.equalsIgnoreCase("COMMIT")) {
            doCommit(connection, paramCommandLog);
            return;
        }
        if (sql.equalsIgnoreCase("ROLLBACK")) {
            doRollback(connection, paramCommandLog);
            return;
        }
        CallableStatement stmt = localSQLCallable.stmt;
        try {
            localSQLCallable = DBOperation.prepareCall(connection, sql, paramVariableTable);
            if (stmt == null)
                return;
            localSQLCallable.bind(paramVariableTable);
            boolean bool = stmt.execute();
            localSQLCallable.fetch(paramVariableTable);
            displayResultSet(stmt, bool, paramCommandLog);
        } catch (SQLException localSQLException2) {
            paramCommandLog.print(localSQLException2);
        } finally {
            if ((localSQLCallable != null) && (stmt != null))
                try {
                    stmt.close();
                } catch (SQLException localSQLException3) {
                }
        }
        clearWarnings(connection, paramCommandLog);
    }

    private void displayResultSet(PreparedStatement stmt,
                                  boolean bool,
                                  CommandLog log) throws SQLException {
        ResultSet localResultSet;
        int j = -1;
        do {
            if (bool) {
                localResultSet = stmt.getResultSet();
                log.print(localResultSet);
                localResultSet.close();
            } else {
                try {
                    j = stmt.getUpdateCount();
                } catch (SQLException localSQLException1) {
                    j = -1;
                }
                if (j >= 0)
                    log.print(j);
                else
                    log.println("Command completed.");
            }
            bool = stmt.getMoreResults();
        }
        while ((bool) || (j != -1));
    }
}