package com.asql.core;

import com.asql.core.io.CommandReader;
import com.asql.core.io.DefaultCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.log.DefaultCommandLog;
import com.asql.core.util.DateOperator;
import com.asql.core.util.TextUtils;
import java.sql.Connection;
import java.sql.SQLException;

public class DefaultSQLExecutor extends CommandExecutor {
    public CMDType cmdType = null;
    public CommandLog out = null;
    public CommandReader in = null;
    public VariableTable sysVariable = new VariableTable();
    public Connection database = null;
    public boolean timing = false;
    public boolean autoCommit = false;

    public DefaultSQLExecutor(CMDType paramCMDType) {
        this.out = new DefaultCommandLog(this);
        this.in = new DefaultCommandReader();
        this.cmdType = paramCMDType;
    }

    public DefaultSQLExecutor(CMDType paramCMDType,
                              CommandReader paramCommandReader,
                              CommandLog paramCommandLog) {
        this.cmdType = paramCMDType;
        this.in = paramCommandReader;
        this.out = paramCommandLog;
    }

    public final DBRowCache executeQuery(String paramString, VariableTable paramVariableTable)
            throws SQLException {
        if (!isConnected())
            return null;
        return executeQuery(this.database, paramString, paramVariableTable);
    }

    public final DBRowCache executeQuery(String paramString,
                                         VariableTable paramVariableTable,
                                         int paramInt)
            throws SQLException {
        if (!isConnected())
            return null;
        return executeQuery(this.database, paramString, paramVariableTable, paramInt);
    }

    public final boolean isConnected() {
        if (this.database == null)
            return false;
        try {
            return !this.database.isClosed();
        } catch (SQLException localSQLException) {
        }
        return false;
    }

    public final void procDisconnect(String paramString) {
        if (!isConnected()) {
            this.out.println("Database not connected.");
            return;
        }
        try {
            this.database.rollback();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            this.database.close();
            this.database = null;
            this.out.println("Disconnect from database!");
        } catch (SQLException localSQLException2) {
            this.out.print(localSQLException2);
        }
    }

    public final void disconnect() {
        if (!isConnected()) {
            this.out.println();
            this.out.println("Database not connected.");
            this.out.println();
            return;
        }
        try {
            this.database.rollback();
        } catch (SQLException localSQLException1) {
            this.out.print(localSQLException1);
        }
        try {
            this.database.close();
            this.database = null;
            this.out.println();
            this.out.println("Disconnect from database!");
            this.out.println();
        } catch (SQLException localSQLException2) {
            this.out.println();
            this.out.print(localSQLException2);
            this.out.println();
        }
    }

    public final int getSingleID(String paramString) {
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(paramString));
        return commandAt(this.cmdType.getASQLSingle(), arrayOfString);
    }

    public final int getDBCommandID(String paramString) {
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(paramString));
        return commandAt(this.cmdType.getDBCommand(), arrayOfString);
    }

    public final int getMultipleID(String paramString) {
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(paramString));
        return commandAt(this.cmdType.getASQLMultiple(), arrayOfString);
    }

    public final String skipWord(String paramString, int paramInt) {
        char[] arrayOfChar = paramString.toCharArray();
        int i = 0;
        int j = 0;
        int k = 0;
        while ((j < paramInt) && (i < arrayOfChar.length)) {
            while ((i < arrayOfChar.length) && isWhitespace(arrayOfChar[i]))
                i++;
            while ((i < arrayOfChar.length) && isNotWhitespace(arrayOfChar[i]))
                i++;
            j++;
        }
        i++;
        if (i < arrayOfChar.length)
            return String.valueOf(arrayOfChar, i, arrayOfChar.length - i);
        return "";
    }

    private boolean isNotWhitespace(char c) {
        return !isWhitespace(c);
    }

    private boolean isWhitespace(char c) {
        return (c == ' ') || (c == '\t') || (c == '\r') || (c == '\n');
    }

    public boolean execute(Command paramCommand) {
        this.out.println();
        this.out.println("Command executed!");
        this.out.println();
        return true;
    }

    public void showVersion() {
        this.out.println();
        this.out.println(" Default Command Executor, version 1.0 -- " + DateOperator.getDay("yyyy-MM-dd HH:mm:ss"));
        this.out.println();
        this.out.println(" (@) Copyright Lou Fangxin, all rights reserved.");
        this.out.println();
    }

    public final CommandLog getCommandLog() {
        return this.out;
    }

    public final void setCommandLog(CommandLog paramCommandLog) {
        this.out = paramCommandLog;
    }

    public final CommandReader getCommandReader() {
        return this.in;
    }

    public final void setCommandReader(CommandReader paramCommandReader) {
        this.in = paramCommandReader;
    }

    public final CMDType getCommandType() {
        return this.cmdType;
    }

    public void doServerMessage()
            throws SQLException {
    }

    public String getLastCommand() {
        return null;
    }
}
