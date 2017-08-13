package com.asql.oracle;

import com.asql.core.CMDType;

public final class OracleCMDType extends CMDType {
    public String[] getSQLQuery() {
        return new String[]{"SELECT", "WITH"};
    }

    public String[] getSQLDML() {
        return new String[]{"INSERT", "UPDATE", "DELETE", "LOCK TABLE", "MERGE"};
    }

    public String[] getSQLDDL() {
        return new String[]{"ANALYZE", "ALTER", "ASSOCIATE", "AUDIT", "CALL", "COMMENT",
                "COMMIT", "ROLLBACK", "SAVEPOINT", "CREATE", "DEASSOCIATE", "DROP",
                "GRANT", "NOAUDIT", "RENAME", "REVOKE", "SET CONSTRAINT", "SET CONSTRAINTS",
                "SET ROLE", "SET TRANSACTION", "TRUNCATE", "PURGE", "FLASHBACK"};
    }

    public String[] getSQLScript() {
        return new String[]{"CREATE FUNCTION", "CREATE PROCEDURE", "CREATE PACKAGE",
                "CREATE TRIGGER", "CREATE TYPE", "CREATE JAVA SOURCE", "CREATE OR REPLACE FUNCTION",
                "CREATE OR REPLACE PROCEDURE", "CREATE OR REPLACE PACKAGE",
                "CREATE OR REPLACE TRIGGER", "CREATE OR REPLACE TYPE",
                "CREATE OR REPLACE JAVA SOURCE"};
    }

    public String[] getSQLBlock() {
        return new String[]{"BEGIN", "DECLARE"};
    }

    public String[] getSQLCall() {
        return new String[]{"EXECUTE", "EXEC"};
    }

    public String[] getDBCommand() {
        return new String[]{"SHOW", "DESCRIBE", "SOURCE", "DESC", "LIST", "DEPEND", "ORA", "START", "HOST"};
    }

    public String[] getASQLSingle() {
        return new String[]{"CONNECT", "SET DEBUGLEVEL", "SET PAGESIZE", "SET HEADING",
                "SET DELIMITER", "SET RECORD", "DISCONNECT", "LOADTNS",
                "CONN", "VAR", "UNVAR", "PRINT", "SET AUTOTRACE", "SET TIMING",
                "SET AUTOT", "SET AUTOCOMMIT", "SET FETCHSIZE", "SET ECHO", "DEFINE",
                "SET DRIVER", "SET TERMOUT", "SQLSET", "SPOOL APPEND", "SPOOL OFF", "SPOOL",
                "READ", "HELP", "L", "SET READONLY", "SET LOCALE", "SET SCANLIMIT", "TUNESORT",
                "SET ENCODING", "BUFFER ADD", "BUFFER LIST", "BUFFER RESET", "SET QUERYONLY",
                "OL HASH", "OL DROP", "OL SHIFT", "OL SHOW", "OL MOVE", "OL LIST", "OL SQL",
                "OL RENAME", "OL"};
    }

    public String[] getSQLFile() {
        return new String[]{"@@", "@"};
    }

    public String[] getASQLMultiple() {
        return new String[]{"LOB", "LOBEXP", "CROSS", "LOBIMP", "EXPLAIN PLAN", "UNLOAD", "LOBLEN",
                "LOAD", "EXPLAIN MVIEW", "EXPLAIN REWRITE"};
    }

    public String[] getEnd() {
        return new String[]{"/", "GO"};
    }

    public String[] getCancel() {
        return new String[]{"."};
    }

    public String[] getComment() {
        return new String[]{"--", "//", "#", "REM"};
    }

    public String[] getMultipleStart() {
        return new String[]{"/*"};
    }

    public String[] getMultipleEnd() {
        return new String[]{"*/"};
    }

    public char getCompleteChar() {
        return ';';
    }

    public char getContinueChar() {
        return '&';
    }

    public String[] getCommandHint() {
        return new String[]{"Analyze", "Commit", "Rollback", "Create or Replace Function", "Create or Replace Procedure", "Create or Replace Package", "Create or Replace Trigger", "Create or Replace Type", "Create or Replace Java Source", "Create Materialized View Log", "Drop Materialized View Log", "Create Materialized View", "Drop Materialized View", "Create Public Database Link", "Drop Public Database Link", "Create Public Synonym", "Create Database Link", "Create Rollback Seg", "Alter Resource Cost", "Create Control File", "Create Snapshot Log", "Drop Public Synonym", "Create Package Body", "Drop Database Link", "Alter Snapshot Log", "Alter Package Body", "Alter Rollback Seg", "Drop Rollback Seg", "Drop Package Body", "Drop Snapshot Log", "Create Tablespace", "Create Procedure", "Create Dimension", "Create Indextype", "Alter Tablespace", "Create Directory", "Create Type Body", "Create Sequence", "Alter Dimension", "Create Function", "Drop Tablespace", "Create Database", "Create Operator", "Alter Type Body", "Create Snapshot", "Alter Procedure", "Create Cluster", "Create Context", "Drop Dimension", "Create Summary", "Drop Indextype", "Create Library", "Drop Directory", "Create Package", "Alter Function", "Drop Type Body", "Alter Operator", "Create Outline", "Alter Snapshot", "Drop Procedure", "Create Profile", "Create Trigger", "Alter Database", "Create Synonym", "Alter Sequence", "Alter Cluster", "Alter Outline", "Alter Summary", "Drop Operator", "Alter Package", "Drop Function", "Drop Snapshot", "Alter Profile", "Alter Trigger", "Create Schema", "Alter Session", "Drop Sequence", "Create Table", "Drop Synonym", "Drop Cluster", "Drop Outline", "Drop Context", "Drop Summary", "Drop Package", "Drop Library", "Drop Profile", "Drop Trigger", "Alter System", "Create Index", "Alter Index", "Alter Table", "Create View", "Create Role", "Create Java", "Create Type", "Create User", "Drop Index", "Drop Table", "Alter User", "Alter Java", "Alter Type", "Alter Role", "Drop View", "Drop User", "Drop Java", "Drop Type", "Drop Role", "Truncate Table"};
    }

    // DBCommand
    public static final int ORACLE_SHOW = 0;
    public static final int ORACLE_DESCRIBE = 1;
    public static final int ORACLE_SOURCE = 2;
    public static final int ORACLE_DESC = 3;
    public static final int ORACLE_LIST = 4;
    public static final int ORACLE_DEPEND = 5;
    public static final int ORACLE_ORAFUNCTION = 6;
    public static final int ORACLE_START = 7;
    public static final int ORACLE_HOST = 8;
    // multiple
    public static final int ORACLE_LOB = 0;
    public static final int ORACLE_LOBEXP = 1;
    public static final int ORACLE_CROSS = 2;
    public static final int ORACLE_LOBIMP = 3;
    public static final int ORACLE_EXPLAINPLAN = 4;
    public static final int ORACLE_UNLOAD = 5;
    public static final int ORACLE_LOBLEN = 6;
    public static final int ORACLE_LOAD = 7;
    public static final int ORACLE_EXPMVIEW = 8;
    public static final int ORACLE_EXPREWRITE = 9;
    // single
    public static final int ORACLE_CONNECT = 0;
    public static final int ORACLE_DEBUGLEVEL = 1;
    public static final int ORACLE_PAGESIZE = 2;
    public static final int ORACLE_HEADING = 3;
    public static final int ORACLE_DELIMITER = 4;
    public static final int ORACLE_RECORD = 5;
    public static final int ORACLE_DISCONNECT = 6;
    public static final int ORACLE_LOADTNS = 7;
    public static final int ORACLE_CONN = 8;
    public static final int ORACLE_VAR = 9;
    public static final int ORACLE_UNVAR = 10;
    public static final int ORACLE_PRINT = 11;
    public static final int ORACLE_AUTOTRACE = 12;
    public static final int ORACLE_TIMING = 13;
    public static final int ORACLE_AUTOT = 14;
    public static final int ORACLE_AUTOCOMMIT = 15;
    public static final int ORACLE_FETCHSIZE = 16;
    public static final int ORACLE_ECHO = 17;
    public static final int ORACLE_DEFINE = 18;
    public static final int ORACLE_DRIVER = 19;
    public static final int ORACLE_TERMOUT = 20;
    public static final int ORACLE_SQLSET = 21;
    public static final int ORACLE_SPOOLAPPEND = 22;
    public static final int ORACLE_SPOOLOFF = 23;
    public static final int ORACLE_SPOOL = 24;
    public static final int ORACLE_READ = 25;
    public static final int ORACLE_HELP = 26;
    public static final int ORACLE_LCOMMAND = 27;
    public static final int ORACLE_READONLY = 28;
    public static final int ORACLE_LOCALE = 29;
    public static final int ORACLE_SCANLIMIT = 30;
    public static final int ORACLE_TUNESORT = 31;
    public static final int ORACLE_ENCODING = 32;
    public static final int ORACLE_BUFFER_ADD = 33;
    public static final int ORACLE_BUFFER_LIST = 34;
    public static final int ORACLE_BUFFER_RESET = 35;
    public static final int ORACLE_QUERYONLY = 36;
    public static final int ORACLE_OLHASH = 37;
    public static final int ORACLE_OLDROP = 38;
    public static final int ORACLE_OLSHIFT = 39;
    public static final int ORACLE_OLSHOW = 40;
    public static final int ORACLE_OLMOVE = 41;
    public static final int ORACLE_OLLIST = 42;
    public static final int ORACLE_OLSQL = 43;
    public static final int ORACLE_OLRENAME = 44;
    public static final int ORACLE_OL = 45;
    // file
    public static final int ORACLE_SQLFILE_0 = 0;
    public static final int ORACLE_SQLFILE_1 = 1;

}
