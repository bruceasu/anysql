package com.asql.mysql;

import com.asql.core.CMDType;

public final class MySqlCMDType extends CMDType {
    @Override
    public String[] getSQLQuery() {
        return new String[]{"SELECT", "SHOW", "DESCRIBE", "DESC",};
    }

    @Override
    public String[] getSQLDML() {
        return new String[]{"DELETE", "INSERT", "UPDATE", "CREATE", "ALTER", "DROP", "GRANT", "REVOKE"};
    }

    @Override
    public String[] getSQLDDL() {
        return new String[]{"DO", "HANDLER", "LOAD", "REPLACE", "TRUNCATE",  "START", "COMMIT", "SAVEPOINT", "ROLLBACK", "LOCK", "UNLOCK", "SET", "ANALYZE", "BACKUP", "CHECK", "CHECKSUM", "OPTIMIZE", "REPAIR", "RESTORE", "CACHE", "FLUSH", "KILL", "RESET", "PURGE", "CHANGE", "PREPARE", "EXECUTE", "DEALLOCATE", "RENAME", "STOP", "BEGIN"};
    }

    @Override
    public String[] getSQLScript() {
        return new String[]{"CREATE PROCEDURE", "CREATE FUNCTION", "CREATE TRIGGER",
                            "CREATE OR REPLACE PROCEDURE","CREATE OR REPLACE FUNCTION",
                            "CREATE OR REPLACE TRIGGER",};
    }

    @Override
    public String[] getSQLBlock() {
        return new String[]{"BEGIN", "DECLARE"};
    }

    @Override
    public String[] getSQLCall() {
        return new String[]{"CALL"};
    }

    @Override
    public String[] getDBCommand() {
        // 兼容 oracle ora
        return new String[]{"ORA","SQL"};
    }

    @Override
    public String[] getASQLSingle() {
        // 顺序同  ASQL_SINGLE_XXX 常量
        return new String[]{"CONNECT", "SET DEBUGLEVEL", "SET PAGESIZE", "SET HEADING",
                            "SET DELIMITER", "SET AUTOTRACE", "SET TIMING", "DISCONNECT",
                            "CONN", "VAR", "UNVAR", "PRINT", "DEFINE", "SQLSET", "SPOOL APPEND",
                            "SPOOL OFF", "SPOOL", "READ", "HELP", "HOST", "SET AUTOT", "USE",
                            "SET AUTOCOMMIT",
                            "SET QUERYONLY"};
    }

    @Override
    public String[] getSQLFile() {
        // 顺序同  ASQL_SINGLE_SQLFILE_X 常量
        return new String[]{"@@", "@"};
    }

    @Override
    public String[] getASQLMultiple() {
        // 顺序同  ASQL_MULTIPLE_XXX 常量
        return new String[]{"LOB", "LOBEXP", "LOBLEN", "LOBIMP", "EXPLAIN"
        // 未实现
        //                    "EXPLAIN","UNLOAD", "LOAD","CROSS"
        };
    }

    @Override
    public String[] getEnd() {
        return new String[]{"/"};
    }

    @Override
    public String[] getCancel() {
        return new String[]{"."};
    }

    @Override
    public String[] getComment() {
        return new String[]{"--", "//", "#"};
    }

    @Override
    public String[] getMultipleStart() {
        return new String[]{"/*"};
    }

    @Override
    public String[] getMultipleEnd() {
        return new String[]{"*/"};
    }

    @Override
    public char getCompleteChar() {
        return ';';
    }

    @Override
    public char getContinueChar() {
        return '&';
    }

    @Override
    public String[] getCommandHint() {
        return new String[]{"Commit", "Rollback", "Create or Replace Function",
                            "Create or Replace Procedure", "Create or Replace Trigger",
                            "Create Procedure", "Create Function", "Create Database",
                            "Create Trigger", "Create Table",
                            "Drop Procedure", "Drop View", "Drop TAble",
                            "Truncate Table"};
    }

    // type2

    // multiple
    public static final int ASQL_MULTIPLE_LOB = 0;
    public static final int ASQL_MULTIPLE_LOBEXP = 1;
    public static final int ASQL_MULTIPLE_LOBLEN = 2;
    public static final int ASQL_MULTIPLE_LOBIMP = 3;
    public static final int ASQL_MULTIPLE_EXPLAIN = 4;
    // single
    public static final int ASQL_SINGLE_CONNECT = 0;
    public static final int ASQL_SINGLE_DEBUGLEVEL = 1;
    public static final int ASQL_SINGLE_PAGESIZE = 2;
    public static final int ASQL_SINGLE_HEADING = 3;
    public static final int ASQL_SINGLE_DELIMITER = 4;
    public static final int ASQL_SINGLE_AUTOTRACE = 5;
    public static final int ASQL_SINGLE_TIMING = 6;
    public static final int ASQL_SINGLE_DISCONNECT = 7;
    public static final int ASQL_SINGLE_CONN = 8;
    public static final int ASQL_SINGLE_VAR = 9;
    public static final int ASQL_SINGLE_UNVAR = 10;
    public static final int ASQL_SINGLE_PRINT = 11;
    public static final int ASQL_SINGLE_DEFINE = 12;
    public static final int ASQL_SINGLE_SQLSET = 13;
    public static final int ASQL_SINGLE_SPOOLAPPEND = 14;
    public static final int ASQL_SINGLE_SPOOLOFF = 15;
    public static final int ASQL_SINGLE_SPOOL = 16;
    public static final int ASQL_SINGLE_READ = 17;
    public static final int ASQL_SINGLE_HELP = 18;
    public static final int ASQL_SINGLE_HOST = 19;
    public static final int ASQL_SINGLE_AUTOT = 20;
    public static final int ASQL_SINGLE_USE = 21;
    public static final int ASQL_SINGLE_AUTOCOMMIT = 22;
    public static final int ASQL_SINGLE_QUERYONLY = 23;
    // file
    public static final int ASQL_SINGLE_SQLFILE_1 = 0;
    public static final int ASQL_SINGLE_SQLFILE_2 = 1;


}
