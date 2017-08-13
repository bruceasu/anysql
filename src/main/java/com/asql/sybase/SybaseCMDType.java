package com.asql.sybase;

import com.asql.core.CMDType;

public final class SybaseCMDType extends CMDType {
    public String[] getSQLQuery() {
        return new String[]{"SELECT"};
    }

    public String[] getSQLDML() {
        return new String[]{"DELETE", "INSERT", "UPDATE"};
    }

    public String[] getSQLDDL() {
        return new String[]{"TRUNCATE", "ALTER", "CREATE", "DROP", "COMMIT", "ROLLBACK", "LOCK",
                "SET", "GRANT", "REVOKE", "CALL"};
    }

    public String[] getSQLScript() {
        return new String[]{"CREATE PROCEDURE", "CREATE FUNCTION", "CREATE TRIGGER"};
    }

    public String[] getSQLBlock() {
        return new String[]{"BEGIN", "IF"};
    }

    public String[] getSQLCall() {
        return new String[]{"EXECUTE", "EXEC"};
    }

    public String[] getDBCommand() {
        return new String[0];
    }

    public String[] getASQLSingle() {
        return new String[]{"MSSQL", "SET DEBUGLEVEL", "SET PAGESIZE", "SET HEADING",
                "SET DELIMITER", "SET AUTOTRACE", "SET TIMING", "DISCONNECT", "SYBASE",
                "VAR", "UNVAR", "PRINT", "DEFINE", "SQLSET", "SPOOL APPEND", "SPOOL OFF",
                "SPOOL", "READ", "HELP", "HOST", "SET AUTOT", "USE", "SET AUTOCOMMIT",
                "BUFFER ADD", "BUFFER LIST", "BUFFER RESET"};
    }

    public String[] getSQLFile() {
        return new String[]{"@@", "@"};
    }

    public String[] getASQLMultiple() {
        return new String[]{"LOB", "LOBEXP", "LOBIMP", "LOBLEN", "UNLOAD", "LOAD"};
    }

    public String[] getEnd() {
        return new String[]{"/", "go"};
    }

    public String[] getCancel() {
        return new String[]{"."};
    }

    public String[] getComment() {
        return new String[]{"--", "//", "#"};
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
        return new String[]{"Analyze", "Commit", "Rollback", "Create or Replace Function",
                "Create or Replace Procedure", "Create or Replace Package",
                "Create or Replace Trigger", "Create or Replace Type",
                "Create or Replace Java Source", "Create Materialized View Log",
                "Drop Materialized View Log", "Create Materialized View", "Drop Materialized View",
                "Create Public Database Link", "Drop Public Database Link", "Create Public Synonym",
                "Create Database Link", "Create Rollback Seg", "Alter Resource Cost",
                "Create Control File", "Create Snapshot Log", "Drop Public Synonym",
                "Create Package Body", "Drop Database Link", "Alter Snapshot Log",
                "Alter Package Body", "Alter Rollback Seg", "Drop Rollback Seg",
                "Drop Package Body", "Drop Snapshot Log", "Create Tablespace", "Create Procedure",
                "Create Dimension", "Create Indextype", "Alter Tablespace", "Create Directory",
                "Create Type Body", "Create Sequence", "Alter Dimension", "Create Function",
                "Drop Tablespace", "Create Database", "Create Operator", "Alter Type Body",
                "Create Snapshot", "Alter Procedure", "Create Cluster", "Create Context",
                "Drop Dimension", "Create Summary", "Drop Indextype", "Create Library",
                "Drop Directory", "Create Package", "Alter Function", "Drop Type Body",
                "Alter Operator", "Create Outline", "Alter Snapshot", "Drop Procedure",
                "Create Profile", "Create Trigger", "Alter Database", "Create Synonym",
                "Alter Sequence", "Alter Cluster", "Alter Outline", "Alter Summary",
                "Drop Operator", "Alter Package", "Drop Function", "Drop Snapshot",
                "Alter Profile", "Alter Trigger", "Create Schema", "Alter Session",
                "Drop Sequence", "Create Table", "Drop Synonym", "Drop Cluster", "Drop Outline",
                "Drop Context", "Drop Summary", "Drop Package", "Drop Library", "Drop Profile",
                "Drop Trigger", "Alter System", "Create Index", "Alter Index", "Alter Table",
                "Create View", "Create Role", "Create Java", "Create Type", "Create User",
                "Drop Index", "Drop Table", "Alter User", "Alter Java", "Alter Type", "Alter Role",
                "Drop View", "Drop User", "Drop Java", "Drop Type", "Drop Role", "Truncate Table"};
    }
}
