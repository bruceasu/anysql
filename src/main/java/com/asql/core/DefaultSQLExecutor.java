package com.asql.core;

import com.asql.core.io.CommandReader;
import com.asql.core.io.DefaultCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.log.DefaultCommandLog;
import com.asql.core.util.DateOperator;
import com.asql.core.util.TextUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class DefaultSQLExecutor extends CommandExecutor {

    public CMDType       cmdType     = null;
    public CommandLog    out         = null;
    public CommandReader in          = null;
    public VariableTable sysVariable = new VariableTable();
    public Connection    database    = null;
    public boolean       timing      = false;
    public boolean       autoCommit  = false;

    public DefaultSQLExecutor(CMDType paramCMDType) {
        this.out     = new DefaultCommandLog(this);
        this.in      = new DefaultCommandReader();
        this.cmdType = paramCMDType;
    }

    public DefaultSQLExecutor(CMDType paramCMDType,
                              CommandReader paramCommandReader,
                              CommandLog paramCommandLog) {
        this.cmdType = paramCMDType;
        this.in      = paramCommandReader;
        this.out     = paramCommandLog;
    }

    @Override
    public final DBRowCache executeQuery(String paramString, VariableTable paramVariableTable)
    throws SQLException {
        if (!isConnected()) { return null; }
        return executeQuery(this.database, paramString, paramVariableTable);
    }

    @Override
    public final DBRowCache executeQuery(String paramString,
                                         VariableTable paramVariableTable,
                                         int paramInt)
    throws SQLException {
        if (!isConnected()) {
            return null;
        }
        return executeQuery(this.database, paramString, paramVariableTable, paramInt);
    }

    @Override
    public final boolean isConnected() {
        if (this.database == null) {
            return false;
        }
        try {
            return !this.database.isClosed();
        } catch (SQLException ignored) {
        }
        return false;
    }

    public boolean checkNotConnected() {
        if (!isConnected()) {
            out.println("Database not connected.");
            return true;
        }
        return false;
    }


    public final void procDisconnect(String param) {
        disconnect();
    }

    @Override
    public final void disconnect() {
        if (!isConnected()) {
            this.out.println();
            this.out.println("Database not connected.");
            this.out.println();
            return;
        }

        try {
            this.database.rollback();
        } catch (SQLException e) {
            this.out.print(e);
        }

        try {
            this.database.close();
            this.database = null;
            this.out.println();
            this.out.println("Disconnect from database!");
            this.out.println();
        } catch (SQLException e) {
            this.out.println();
            this.out.print(e);
            this.out.println();
        }
    }

    public final int getSingleID(String cmd) {
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(cmd));
        return commandAt(this.cmdType.getASQLSingle(), arr);
    }

    public final int getDBCommandID(String cmd) {
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(cmd));
        return commandAt(this.cmdType.getDBCommand(), arr);
    }

    public final int getMultipleID(String cmd) {
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(cmd));
        return commandAt(this.cmdType.getASQLMultiple(), arr);
    }

    public final String skipWord(String param, int num) {
        char[] arr = param.toCharArray();
        int    i   = 0;
        int    j   = 0;
        int    k   = 0;
        while ((j < num) && (i < arr.length)) {
            while ((i < arr.length) && isWhitespace(arr[i])) {
                i++;
            }
            while ((i < arr.length) && isNotWhitespace(arr[i])) {
                i++;
            }
            j++;
        }
        i++;
        if (i < arr.length) {
            return String.valueOf(arr, i, arr.length - i);
        }
        return "";
    }


    public void printCost(long end, long start) {
        if (timing) {
            this.out.println("Execute time: " + DBOperation.getElapsed(end - start));
        }
    }

    public <R> R time(Command cmd, Function<String, R> function) {
        this.out.println();
        long l1 = System.currentTimeMillis();
        R    r  = function.apply(cmd.command);
        long l2 = System.currentTimeMillis();
        printCost(l2, l1);
        this.out.println();
        return r;
    }

    @Override
    public boolean execute(Command paramCommand) {
        this.out.println();
        this.out.println("Command executed!");
        this.out.println();
        return true;
    }

    @Override
    public void showVersion() {
        this.out.println();
        this.out.println(" Default Command Executor, version 1.0 -- " + DateOperator
                .getDay("yyyy-MM-dd HH:mm:ss"));
        this.out.println();
        this.out.println(" (@) Copyright Lou Fangxin, all rights reserved.");
        this.out.println();
    }

    @Override
    public final CommandLog getCommandLog() {
        return this.out;
    }

    @Override
    public final void setCommandLog(CommandLog paramCommandLog) {
        this.out = paramCommandLog;
    }

    @Override
    public final CommandReader getCommandReader() {
        return this.in;
    }

    @Override
    public final void setCommandReader(CommandReader paramCommandReader) {
        this.in = paramCommandReader;
    }

    @Override
    public final CMDType getCmdType() {
        return this.cmdType;
    }

    @Override
    public void doServerMessage()
    throws SQLException {
    }

    @Override
    public String getLastCommand() {
        return null;
    }


    private boolean isNotWhitespace(char c) {
        return !isWhitespace(c);
    }

    private boolean isWhitespace(char c) {
        return (c == ' ') || (c == '\t') || (c == '\r') || (c == '\n');
    }

    public CommandLog getOut() {
        return out;
    }

    public CommandReader getIn() {
        return in;
    }

    public VariableTable getSysVariable() {
        return sysVariable;
    }

    public Connection getDatabase() {
        return database;
    }

    public boolean isTiming() {
        return timing;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }
}
