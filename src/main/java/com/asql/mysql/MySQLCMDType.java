package com.asql.mysql;

import com.asql.core.CMDType;

public final class MySQLCMDType extends CMDType {
    public String[] getSQLQuery() {
        return new String[]{"SELECT"};
    }

    public String[] getSQLDML() {
        return new String[]{"DELETE", "INSERT", "UPDATE", "CREATE", "ALTER", "DROP", "GRANT", "REVOKE"};
    }

    public String[] getSQLDDL() {
        return new String[]{"DO", "HANDLER", "LOAD", "REPLACE", "TRUNCATE", "DESCRIBE", "DESC", "START", "COMMIT", "SAVEPOINT", "ROLLBACK", "LOCK", "UNLOCK", "SET", "SHOW", "ANALYZE", "BACKUP", "CHECK", "CHECKSUM", "OPTIMIZE", "REPAIR", "RESTORE", "CACHE", "FLUSH", "KILL", "RESET", "PURGE", "CHANGE", "PREPARE", "EXECUTE", "DEALLOCATE", "EXPLAIN", "RENAME", "STOP", "BEGIN"};
    }

    public String[] getSQLScript() {
        return new String[0];
    }

    public String[] getSQLBlock() {
        return new String[0];
    }

    public String[] getSQLCall() {
        return new String[0];
    }

    public String[] getDBCommand() {
        return new String[0];
    }

    public String[] getASQLSingle() {
        return new String[]{"CONNECT", "SET DEBUGLEVEL", "SET PAGESIZE", "SET HEADING", "SET DELIMITER", "SET AUTOTRACE", "SET TIMING", "DISCONNECT", "CONN", "VAR", "UNVAR", "PRINT", "DEFINE", "SQLSET", "SPOOL APPEND", "SPOOL OFF", "SPOOL", "READ", "HELP", "HOST", "SET AUTOT", "USE", "SET AUTOCOMMIT"};
    }

    public String[] getSQLFile() {
        return new String[]{"@@", "@"};
    }

    public String[] getASQLMultiple() {
        return new String[]{"LOB", "LOBEXP"};
    }

    public String[] getEnd() {
        return new String[]{"/"};
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
        return new String[0];
    }
}
