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

import static com.asql.core.CMDType.ASQL_END;
import static com.asql.core.CMDType.SQL_QUERY;
import static com.asql.oracle.OracleCMDType.*;
import static com.asql.oracle.invoker.OracleORAFunction.*;

import com.asql.core.*;
import com.asql.core.io.InputCommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.DateOperator;
import com.asql.core.util.TextUtils;
import com.asql.oracle.OracleSQLExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by suk on 2017/8/13.
 */
public class DBCommandInvoker implements ModuleInvoker {

    OracleSQLExecutor executor;
    CommandLog        out;
    CMDType           cmdType;

    public DBCommandInvoker(OracleSQLExecutor executor) {
        this.executor = executor;
        out           = executor.getCommandLog();
        cmdType       = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        long l1;
        long l2;
        int  k = executor.getDBCommandID(cmd.command);
        switch (k) {
            case ORACLE_HOST:
                time(cmd, str -> procHost(str));
                break;
            case ORACLE_DESCRIBE:
            case ORACLE_DESC:
                time(cmd, str -> procDescribe(str));
                break;
            case ORACLE_DEPEND:
                time(cmd, str -> procDepend(str));
                break;
            case ORACLE_START:
                boolean result = time(cmd, str -> procRun1(str));
                if (result) { return false; }
                break;
            case ORACLE_SOURCE:
                time(cmd, str -> {
                    int len = TextUtils.getWords(cmdType.getDBCommand()[ORACLE_SOURCE]).size();
                    str = executor.skipWord(str, len);
                    String[] args = TextUtils.toStringArray(TextUtils.getWords(str));
                    return new SourceProcessor(str, args).proc();
                });
                break;
            case ORACLE_SHOW:
                time(cmd, str -> {
                    int len = TextUtils.getWords(cmdType.getDBCommand()[ORACLE_SHOW]).size();
                    str = executor.skipWord(str, len);
                    String[] args = TextUtils.toStringArray(TextUtils.getWords(str));
                    return new ShowProcessor(str, args).proc();
                });
                break;
            case ORACLE_LIST:
                time(cmd, str -> {
                    int      lenOfOraCmd  = TextUtils.getWords(cmdType.getDBCommand()[ORACLE_LIST])
                                                     .size();
                    String   cmdLineOfOra = executor.skipWord(cmd.command, lenOfOraCmd);
                    String[] args         = TextUtils
                            .toStringArray(TextUtils.getWords(cmdLineOfOra));
                    return new ListProcessor(str, args).proc();
                });
                break;
            case ORACLE_ORAFUNCTION:
                time(cmd, str -> {
                    int      lenOfOraCmd  = TextUtils
                            .getWords(cmdType.getDBCommand()[ORACLE_ORAFUNCTION]).size();
                    String   cmdLineOfOra = executor.skipWord(cmd.command, lenOfOraCmd);
                    String[] args         = TextUtils
                            .toStringArray(TextUtils.getWords(cmdLineOfOra));
                    return new OraProcessor(cmdLineOfOra, args).proc();
                });
                break;
        }
        return true;
    }

    boolean procRun1(String cmdLine) {
        int    i    = TextUtils.getWords(cmdType.getSQLFile()[ORACLE_SQLFILE_1]).size();
        String str1 = executor.skipWord(cmdLine, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: @[@] file");
            return false;
        }
        String path = executor.sysVariable.parseString(str1.trim());
        File   file = new File(path);
        try (FileInputStream stream = new FileInputStream(file);
             InputCommandReader reader = new InputCommandReader(stream)) {
            reader.setWorkingDir(file.getParent());
            Command localCommand = executor.run(reader);
            reader.close();
            return localCommand.command != null;
        } catch (IOException localIOException) {
            out.print(localIOException);
        }
        return false;
    }


    boolean procHost(String cmdLine) {
        int    i   = TextUtils.getWords(cmdType.getDBCommand()[ORACLE_HOST]).size();
        String str = executor.skipWord(cmdLine, i);
        str = executor.sysVariable.parseString(str.trim());
        if (str.length() > 0) {
            try {
                executor.host(str);
                return true;
            } catch (IOException localIOException) {
                out.print(localIOException);
                return false;
            }
        } else { return false; }
    }

    boolean procDescribe(String cmdLine) {
        if (!executor.isConnected()) {
            out.println("Database not connected!");
            return false;
        }
        int    i    = TextUtils.getWords(cmdType.getDBCommand()[ORACLE_DESCRIBE]).size();
        String str1 = executor.skipWord(cmdLine, i);
        str1 = str1.trim();
        String[]      arrayOfString = TextUtils
                .toStringArray(TextUtils.getWords(str1, new String[]{"."}));
        VariableTable table         = new VariableTable();
        table.add("P_OWNER", 12);
        table.add("P_NAME", 12);
        table.add("P_TYPE", 12);
        setTable(arrayOfString, table);
        DBRowCache rowCache1 = null;
        DBRowCache rowCache2 = null;
        if (arrayOfString.length == 1) {
            try {
                rowCache2 = executor.executeQuery(executor.descSqls[0], table, 5000);
            } catch (SQLException localSQLException1) {
                out.print(localSQLException1);
            }
        }
        if (arrayOfString.length == 2) {
            try {
                rowCache2 = executor.executeQuery(executor.descSqls[1], table, 5000);
            } catch (SQLException localSQLException2) {
                out.print(localSQLException2);
            }
        }
        if ((rowCache2 == null) || (rowCache2.getRowCount() == 0)) {
            if (arrayOfString.length == 1) {
                try {
                    rowCache2 = executor.executeQuery(executor.descSqls[2], table, 5000);
                } catch (SQLException localSQLException3) {
                    out.print(localSQLException3);
                }
            }
            if (arrayOfString.length == 2) {
                try {
                    rowCache2 = executor.executeQuery(executor.descSqls[3], table, 5000);
                } catch (SQLException localSQLException4) {
                    out.print(localSQLException4);
                }
            }
        }
        if ((rowCache2 == null) || (rowCache2.getRowCount() == 0)) {
            try {
                rowCache2 = executor.executeQuery(executor.descSqls[4], table, 5000);
            } catch (SQLException localSQLException5) {
                out.print(localSQLException5);
            }
        }
        if ((rowCache2 == null) || (rowCache2.getRowCount() == 0)) {
            out.println("Object not exists!");
            return false;
        }
        for (int j = 1; j < 2; j++) {
            rowCache2.getWidth(false);
            table.setValue("P_OWNER", rowCache2.getItem(j, 1));
            table.setValue("P_NAME", rowCache2.getItem(j, 2));
            table.setValue("P_TYPE", rowCache2.getItem(j, 3));
            String str2 = (String) rowCache2.getItem(j, 3);
            try {
                if ((str2.equalsIgnoreCase("VIEW"))
                        || (str2.equalsIgnoreCase("TABLE"))
                        || (str2.equalsIgnoreCase("CLUSTER"))) {
                    rowCache1 = executor.executeQuery(executor.descSqls[5], table, 5000);
                } else if ((str2.equalsIgnoreCase("PACKAGE")) || (str2
                        .equalsIgnoreCase("PACKAGE BODY"))) {
                    rowCache1 = executor.executeQuery(executor.descSqls[6], table, 5000);
                } else if ((str2.equalsIgnoreCase("PROCEDURE")) || (str2
                        .equalsIgnoreCase("FUNCTION"))) {
                    rowCache1 = executor.executeQuery(executor.descSqls[7], table, 5000);
                }
            } catch (SQLException localSQLException6) {
                out.print(localSQLException6);
                continue;
            }
            int k = 0;
            if ((rowCache1 != null) && (rowCache1.getRowCount() > 0)) {
                rowCache1.getWidth(false);
                if ((str2.equalsIgnoreCase("VIEW"))
                        || (str2.equalsIgnoreCase("CLUSTER"))
                        || (str2.equalsIgnoreCase("PROCEDURE"))
                        || (str2.equalsIgnoreCase("FUNCTION"))) {
                    rowCache1.setColumnSize("NAME", 30);
                    out.println(rowCache1.getFixedHeader());
                    out.println(rowCache1.getSeperator());
                    k = 1;
                    while (k <= rowCache1.getRowCount()) {
                        out.println(rowCache1.getFixedRow(k));
                        k++;
                        continue;
                    }
                }
            } else if (str2.equalsIgnoreCase("TABLE")) {
                rowCache1.setColumnSize("NAME", 30);
                out.println(rowCache1.getFixedHeader());
                out.println(rowCache1.getSeperator());
                for (k = 1; k <= rowCache1.getRowCount(); k++) {
                    out.println(rowCache1.getFixedRow(k));
                }
                try {
                    rowCache1 = executor.executeQuery(executor.descSqls[8], table, 5000);
                    rowCache1.getWidth(false);
                    rowCache1.setColumnSize("INDEX_NAME", 30);
                    if (rowCache1.getRowCount() > 0) {
                        out.println();
                        out.println(rowCache1.getFixedHeader());
                        out.println(rowCache1.getSeperator());
                        for (k = 1; k <= rowCache1.getRowCount(); k++) {
                            out.println(rowCache1.getFixedRow(k));
                        }
                    }
                } catch (SQLException localSQLException7) {
                }
                try {
                    rowCache1 = executor.executeQuery(executor.descSqls[9], table, 5000);
                    rowCache1.getWidth(false);
                    if (rowCache1.getRowCount() > 0) {
                        out.println();
                        out.println(rowCache1.getFixedHeader());
                        out.println(rowCache1.getSeperator());
                        for (int m = 1; m <= rowCache1.getRowCount(); m++) {
                            out.println(rowCache1.getFixedRow(m));
                        }
                    }
                } catch (SQLException localSQLException8) {
                }
            } else {
                if ((!str2.equalsIgnoreCase("PACKAGE")) && (!str2
                        .equalsIgnoreCase("PACKAGE BODY"))) { break; }
                Object localObject = "#null";
                int    n           = 0;
                int    i1          = rowCache1.getColumnSize(1);
                for (int i2 = 1; i2 <= rowCache1.getRowCount(); i2++) {
                    String str3 = (String) rowCache1.getItem(i2, 1);
                    String str4 = str3.substring(0, str3.indexOf("("));
                    String str5 = (String) rowCache1.getItem(i2, 4);
                    String str6 = (String) rowCache1.getItem(i2, 2);
                    if (!str3.equalsIgnoreCase((String) localObject)) {
                        n = 0;
                        if ((str5 == null) || (str5.length() == 0)) {
                            out.println("PROCECURE " + str4);
                        } else if (str6.trim().equals("@return")) {
                            out.println("FUNCTION " + str4 + " RETURN " + str5);
                        } else {
                            out.println("PROCECURE " + str4);
                            out.println(" " + rowCache1.getFixedHeader().substring(i1));
                            out.println(" " + rowCache1.getSeperator().substring(i1));
                            out.println(" " + rowCache1.getFixedRow(i2).substring(i1));
                            n = 1;
                        }
                        localObject = str3;
                    } else {
                        if (n == 0) {
                            out.println(" " + rowCache1.getFixedHeader().substring(i1));
                            out.println(" " + rowCache1.getSeperator().substring(i1));
                            n = 1;
                        }
                        if (!str6.trim().equals("@return")) {
                            out.println(" " + rowCache1.getFixedRow(i2).substring(i1));
                        }
                    }
                    //continue;
                    //out.println("No column founded!");
                }
            }
        }
        return true;
    }

    boolean procDepend(String cmdLine) {
        if (!executor.isConnected()) {
            out.println("Database not connected!");
            return false;
        }
        DBRowCache rowCache = null;
        int        i        = TextUtils.getWords(cmdType.getDBCommand()[ORACLE_DEPEND]).size();
        String     str      = executor.skipWord(cmdLine, i);
        str = str.trim();
        String[] arr = TextUtils.toStringArray(TextUtils.getWords(str, new String[]{"."}));
        if (arr.length == 0) {
            out.println("Object name pattern required.");
            return false;
        }
        VariableTable table = new VariableTable();
        table.add("P_OWNER", 12);
        table.add("P_NAME", 12);
        setTable(arr, table);
        String sql1 = "SELECT /* AnySQL */ TYPE,REFERENCED_OWNER D_OWNER, "
                + "      REFERENCED_NAME D_NAME,REFERENCED_TYPE D_TYPE, "
                + "      REFERENCED_LINK_NAME DBLINK, DEPENDENCY_TYPE DEPEND"
                + "  FROM ALL_DEPENDENCIES "
                + "  WHERE "
                + "    UPPER(OWNER) = NVL(:P_OWNER,USER) "
                + "    AND NAME  = :P_NAME";
        queryDepend(sql1, table, str, "Reference:");
        String sql2 = "SELECT /* AnySQL */  REFERENCED_TYPE TYPE,OWNER R_OWNER,"
                + "       NAME R_NAME, TYPE R_TYPE,DEPENDENCY_TYPE DEPEND "
                + "  FROM ALL_DEPENDENCIES "
                + "  WHERE "
                + "    UPPER(REFERENCED_OWNER) = NVL(:P_OWNER,USER) "
                + "    AND REFERENCED_NAME  = :P_NAME AND REFERENCED_LINK_NAME IS NULL";
        queryDepend(sql2, table, str, "Reference By:");
        return true;
    }

    private void queryDepend(String sql, VariableTable table, String obj, String msg) {
        DBRowCache rowCache;
        try {
            rowCache = executor.executeQuery(executor.database, sql, table, 2000);
            if (rowCache.getRowCount() >= 0) {
                out.println(msg);
                executor.showDBRowCache(rowCache, true);
                out.println();
            } else {
                out.println("No dependency founded for " + obj + ".");
            }
        } catch (SQLException localSQLException1) {
            out.print(localSQLException1);
        }
    }

    boolean time(Command cmd, Function<String, Boolean> function) {
        out.println();
        long    l1 = System.currentTimeMillis();
        boolean r  = function.apply(cmd.command);
        long    l2 = System.currentTimeMillis();
        executor.printCost(l2, l1);
        out.println();
        return r;
    }

    void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) { closeable.close(); }
        } catch (Throwable ignore) {
        }
    }

    private void setTable(String[] ownerAndName, VariableTable table) {
        if ((ownerAndName.length == 1) || (ownerAndName.length == 2)) {
            if (ownerAndName.length == 1) {
                table.setValue("P_NAME", ownerAndName[0].toUpperCase());
            }
            if (ownerAndName.length == 2) {
                table.setValue("P_OWNER", ownerAndName[0].toUpperCase());
                table.setValue("P_NAME", ownerAndName[1].toUpperCase());
            }
        }
    }

    class ListProcessor {

        String   str;
        String[] args;

        public ListProcessor(String str, String[] args) {
            this.str  = str;
            this.args = args;
        }

        boolean proc() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return false;
            }
            if (args.length == 0) {
                listUsage(out);
                return false;
            }
            int j = TextUtils.indexOf(ORACLE_LIST_TYPES, args[0]);
            if (j == -1) {
                out.println("Invalid type " + args[0] + " specified.");
                return false;
            }
            VariableTable localVariableTable = new VariableTable();
            localVariableTable.add("P_OWNER", 12);
            localVariableTable.add("P_NAME", 12);
            localVariableTable.add("P_TYPE", 12);
            localVariableTable.setValue("P_NAME", "%");
            if (args.length > 1) {
                String[] arrayOfString2 = TextUtils
                        .toStringArray(TextUtils.getWords(args[1], new String[]{"."}));
                if (arrayOfString2.length > 1) {
                    localVariableTable.setValue("P_OWNER", arrayOfString2[0].toUpperCase());
                    localVariableTable.setValue("P_NAME", arrayOfString2[1].toUpperCase());
                } else if (arrayOfString2.length > 0) {
                    localVariableTable.setValue("P_NAME", arrayOfString2[0].toUpperCase());
                }
            }
            switch (j) {
                case ORACLE_LIST_OBJECT:
                case ORACLE_LIST_CLUSTER:
                case ORACLE_LIST_INDEX:
                case ORACLE_LIST_SEQUENCE:
                case ORACLE_LIST_SYNONYM:
                case ORACLE_LIST_TABLE:
                case ORACLE_LIST_TRIGGER:
                case ORACLE_LIST_TYPE:
                case ORACLE_LIST_VIEW:
                case ORACLE_LIST_MVIEW:
                case ORACLE_LIST_TABPART:
                case ORACLE_LIST_INDPART:
                case ORACLE_LIST_SEGMENT:
                case ORACLE_LIST_REBUILD:
                case ORACLE_LIST_QUEUE:
                case ORACLE_LIST_LOB:
                case ORACLE_LIST_LOBPART:
                    executor.executeSQL(executor.database,
                            new Command(SQL_QUERY, ASQL_END, LIST_SQLS.get(j)), localVariableTable,
                            out);
                    break;
                case ORACLE_LIST_PROCEDURE:
                case ORACLE_LIST_FUNCTION:
                case ORACLE_LIST_PACKAGE:
                default:
                    localVariableTable.setValue("P_TYPE", args[0].toUpperCase());
                    executor.executeSQL(executor.database, new Command(SQL_QUERY, ASQL_END,
                            LIST_SQLS.get(ORACLE_LIST_ALL_OBJECTS)), localVariableTable, out);
            }
            return true;
        }

        final String[] ORACLE_LIST_TYPES = {"OBJECT", "CLUSTER", "INDEX", "SEQUENCE",
                "SYNONYM", "TABLE", "TRIGGER", "TYPE", "VIEW", "MVIEW", "TABPART", "INDPART",
                "PROCEDURE", "FUNCTION", "PACKAGE", "SEGMENT", "REBUILD", "LOB", "QUEUE",
                "LOBPART"};

        public static final int ORACLE_LIST_OBJECT      = 0;
        public static final int ORACLE_LIST_CLUSTER     = 1;
        public static final int ORACLE_LIST_INDEX       = 2;
        public static final int ORACLE_LIST_SEQUENCE    = 3;
        public static final int ORACLE_LIST_SYNONYM     = 4;
        public static final int ORACLE_LIST_TABLE       = 5;
        public static final int ORACLE_LIST_TRIGGER     = 6;
        public static final int ORACLE_LIST_TYPE        = 7;
        public static final int ORACLE_LIST_VIEW        = 8;
        public static final int ORACLE_LIST_MVIEW       = 9;
        public static final int ORACLE_LIST_TABPART     = 10;
        public static final int ORACLE_LIST_INDPART     = 11;
        public static final int ORACLE_LIST_PROCEDURE   = 12;
        public static final int ORACLE_LIST_FUNCTION    = 13;
        public static final int ORACLE_LIST_PACKAGE     = 14;
        public static final int ORACLE_LIST_SEGMENT     = 15;
        public static final int ORACLE_LIST_REBUILD     = 16;
        public static final int ORACLE_LIST_LOB         = 17;
        public static final int ORACLE_LIST_QUEUE       = 18;
        public static final int ORACLE_LIST_LOBPART     = 19;
        public static final int ORACLE_LIST_ALL_OBJECTS = 20;

        public final HashMap<Integer, String> LIST_SQLS = new HashMap<Integer, String>() {{
            put(ORACLE_LIST_OBJECT,
                    "SELECT /* AnySQL */ OBJECT_TYPE TYPE,OBJECT_ID ID,OWNER,OBJECT_NAME, "
                            + "      TO_CHAR(CREATED,'YYYY/MM/DD') CREATED, "
                            + "      TO_CHAR(LAST_DDL_TIME,'YYYY/MM/DD HH24:MI:SS') MODIFIED,STATUS "
                            + "  FROM ALL_OBJECTS "
                            + "  WHERE OBJECT_TYPE IN ('CLUSTER','FUNCTION','INDEX',"
                            + "       'PACKAGE','PROCEDURE','SEQUENCE','SYNONYM',"
                            + "       'TABLE','TRIGGER','TYPE','VIEW') "
                            + "    AND (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                            + "    AND OBJECT_NAME LIKE :P_NAME");
            put(ORACLE_LIST_CLUSTER, "SELECT /* AnySQL */ OWNER,CLUSTER_NAME,CLUSTER_TYPE TYPE, "
                    + "   TABLESPACE_NAME TS_NAME,KEY_SIZE KSIZE,HASHKEYS KEYS, "
                    + "   INITIAL_EXTENT/1024 INI_K, NEXT_EXTENT/1024 NEXT_K, MAX_EXTENTS MAXEXT,PCT_INCREASE PCT"
                    + "   PCT_INCREASE PCTINC,SINGLE_TABLE SIGTBL "
                    + "  FROM ALL_CLUSTERS "
                    + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                    + "    AND CLUSTER_NAME LIKE :P_NAME");
            put(ORACLE_LIST_INDEX, "SELECT /* AnySQL */ OWNER,INDEX_NAME,INDEX_TYPE TYPE, "
                    + "   TABLESPACE_NAME TS_NAME,"
                    + "   UNIQUENESS UK,INITIAL_EXTENT/1024 INI_K, NEXT_EXTENT/1024 NEXT_K,PCT_INCREASE PCT,"
                    + "   STATUS,PARTITIONED PART,"
                    + "   TO_CHAR(LAST_ANALYZED,'YYYY/MM/DD') ANA_DATE, FREELISTS FLS, FREELIST_GROUPS FLGS "
                    + "  FROM ALL_INDEXES "
                    + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                    + "    AND INDEX_NAME LIKE :P_NAME");
            put(ORACLE_LIST_SEQUENCE, "SELECT /* AnySQL */ SEQUENCE_OWNER OWNER,SEQUENCE_NAME, "
                    + "   MIN_VALUE LOW,MAX_VALUE HIGH,INCREMENT_BY STEP,CYCLE_FLAG CYC,"
                    + "   ORDER_FLAG ORD,CACHE_SIZE CACHE,LAST_NUMBER CURVAL"
                    + "  FROM ALL_SEQUENCES "
                    + "  WHERE (:P_OWNER IS NULL OR UPPER(SEQUENCE_OWNER) = :P_OWNER) "
                    + "    AND SEQUENCE_NAME LIKE :P_NAME");
            put(ORACLE_LIST_SYNONYM, "SELECT /* AnySQL */ * "
                    + "  FROM ALL_SYNONYMS "
                    + "  WHERE (:P_OWNER IS NULL OR OWNER = :P_OWNER) "
                    + "    AND SYNONYM_NAME LIKE :P_NAME");
            put(ORACLE_LIST_TABLE, "SELECT /* AnySQL */ OWNER,TABLE_NAME, "
                    + "   TABLESPACE_NAME TS_NAME,"
                    + "   INITIAL_EXTENT/1024 INI_K, NEXT_EXTENT/1024 NEXT_K,PCT_INCREASE PCT,"
                    + "   PARTITIONED PART,IOT_TYPE IOT,TO_CHAR(LAST_ANALYZED,'YYYY/MM/DD') ANA_DATE,"
                    + "   DECODE(CLUSTER_NAME,NULL,NULL,CLUSTER_OWNER||'.'||CLUSTER_NAME) CNAME,NESTED, "
                    + "   FREELISTS FLS, FREELIST_GROUPS FLGS "
                    + "  FROM ALL_TABLES "
                    + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                    + "    AND TABLE_NAME LIKE :P_NAME");
            put(ORACLE_LIST_TRIGGER, "SELECT /* AnySQL */ OWNER,TRIGGER_NAME,"
                    + "     TABLE_OWNER||'.'||TABLE_NAME TABLE_NAME,STATUS,"
                    + "     TRIGGER_TYPE TYPE,TRIGGERING_EVENT EVENT"
                    + "  FROM ALL_TRIGGERS "
                    + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                    + "    AND TRIGGER_NAME LIKE :P_NAME");
            put(ORACLE_LIST_TYPE,
                    "SELECT /* AnySQL */ OWNER,TYPE_NAME,TYPECODE TYPE,PREDEFINED,INCOMPLETE "
                            + "  FROM ALL_TYPES "
                            + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                            + "    AND TYPE_NAME LIKE :P_NAME AND OWNER IS NOT NULL");
            put(ORACLE_LIST_VIEW, "SELECT /* AnySQL */ OWNER,VIEW_NAME, "
                    + "   DECODE(VIEW_TYPE_OWNER,NULL,NULL,VIEW_TYPE_OWNER||'.'||VIEW_TYPE) TYPE_NAME"
                    + "  FROM ALL_VIEWS "
                    + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                    + "    AND VIEW_NAME LIKE :P_NAME");
            put(ORACLE_LIST_MVIEW,
                    "SELECT /* AnySQL */ OWNER,MVIEW_NAME,CONTAINER_NAME TABLE_NAME, "
                            + "   UPDATABLE UPD,REWRITE_ENABLED REWRITE,REFRESH_MODE||' '||REFRESH_METHOD REFRESH,"
                            + "   STALENESS,FAST_REFRESHABLE FASTABLE "
                            + "  FROM ALL_MVIEWS "
                            + "  WHERE (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                            + "    AND MVIEW_NAME LIKE :P_NAME");
            put(ORACLE_LIST_TABPART,
                    "SELECT /* AnySQL */ PARTITION_POSITION NO#,PARTITION_NAME,TABLESPACE_NAME TS_NAME, "
                            + "   HIGH_VALUE,INITIAL_EXTENT/1024 INI_K, NEXT_EXTENT/1024 NEXT_K,PCT_INCREASE PCT, "
                            + "   FREELISTS FLS, FREELIST_GROUPS FLGS "
                            + "  FROM ALL_TAB_PARTITIONS "
                            + "  WHERE UPPER(TABLE_OWNER) = NVL(:P_OWNER,USER) "
                            + "    AND TABLE_NAME LIKE :P_NAME "
                            + "  ORDER BY 1 ");
            put(ORACLE_LIST_INDPART,
                    "SELECT /* AnySQL */ PARTITION_POSITION NO#,PARTITION_NAME,TABLESPACE_NAME TS_NAME, "
                            + "   HIGH_VALUE,INITIAL_EXTENT/1024 INI_K, NEXT_EXTENT/1024 NEXT_K,PCT_INCREASE PCT, "
                            + "   FREELISTS FLS, FREELIST_GROUPS FLGS "
                            + "  FROM ALL_IND_PARTITIONS "
                            + "  WHERE UPPER(INDEX_OWNER) = NVL(:P_OWNER,USER) "
                            + "    AND INDEX_NAME LIKE :P_NAME "
                            + "  ORDER BY 1 ");
            put(ORACLE_LIST_PROCEDURE, "");
            put(ORACLE_LIST_FUNCTION, "");
            put(ORACLE_LIST_PACKAGE, "");
            put(ORACLE_LIST_SEGMENT,
                    "SELECT /* AnySQL */ OWNER,SEGMENT_NAME SEG_NAME,PARTITION_NAME SUB_NAME, "
                            + "   SEGMENT_TYPE SEG_TYPE,TABLESPACE_NAME TS_NAME,TRUNC(BYTES/1024) SIZE_KB,"
                            + "   INITIAL_EXTENT/1024 INI_K, NEXT_EXTENT/1024 NEXT_K,PCT_INCREASE PCT,EXTENTS EXTS"
                            + "  FROM DBA_SEGMENTS "
                            + "  WHERE UPPER(OWNER) = NVL(:P_OWNER,USER) "
                            + "    AND SEGMENT_NAME LIKE :P_NAME");
            put(ORACLE_LIST_REBUILD, "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    I.OWNER,I.INDEX_NAME,P.PARTITION_NAME,I.INDEX_TYPE,  "
                    + "    DECODE(I.PARTITIONED,'YES',P.TABLESPACE_NAME,I.TABLESPACE_NAME) TS_NAME,  "
                    + "    DECODE(I.PARTITIONED,'YES',P.STATUS,I.STATUS) STATUS "
                    + " FROM ALL_INDEXES I, ALL_IND_PARTITIONS P "
                    + " WHERE I.OWNER=P.INDEX_OWNER(+) AND I.INDEX_NAME=P.INDEX_NAME(+) "
                    + "   AND UPPER(I.TABLE_OWNER)=NVL(:P_OWNER,USER) AND I.TABLE_NAME LIKE :P_NAME "
                    + "    AND I.STATUS<>'VALID' AND NVL(P.STATUS,'UNUSABLE') <> 'USABLE'");
            put(ORACLE_LIST_LOB, "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    OWNER,TABLE_NAME TNAME,COLUMN_NAME COL,SEGMENT_NAME,INDEX_NAME,PCTVERSION PCT, RETENTION RET, CHUNK, CACHE, LOGGING LOG, IN_ROW  "
                    + " FROM ALL_LOBS "
                    + " WHERE UPPER(OWNER)=NVL(:P_OWNER,USER) AND TABLE_NAME LIKE :P_NAME||'%' ");
            put(ORACLE_LIST_QUEUE, "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    OWNER,NAME QNAME,QUEUE_TYPE QTYPE,QUEUE_TABLE TNAME,  "
                    + "    ENQUEUE_ENABLED ENQABLE,DEQUEUE_ENABLED DEQABLE,RETENTION "
                    + " FROM ALL_QUEUES "
                    + " WHERE (:P_OWNER IS NULL OR UPPER(OWNER)=:P_OWNER) AND NAME LIKE :P_NAME");
            put(ORACLE_LIST_LOBPART, "SELECT /* AnySQL */ "
                    + "    TABLE_NAME TNAME,COLUMN_NAME COL,  "
                    + "    PARTITION_NAME PNAME,LOB_PARTITION_NAME LPNAME,  "
                    + "    INITIAL_EXTENT/1024 INIEXT, NEXT_EXTENT/1024 NXTEXT, "
                    + "    PCT_INCREASE PCT, FREELISTS FLS  "
                    + " FROM ALL_LOB_PARTITIONS "
                    + " WHERE UPPER(TABLE_OWNER)=:P_OWNER AND LOB_NAME=:P_NAME ORDER BY 3");
            put(ORACLE_LIST_ALL_OBJECTS,
                    "SELECT /* AnySQL */ OBJECT_TYPE TYPE,OBJECT_ID ID,OWNER,OBJECT_NAME, "
                            + "      TO_CHAR(CREATED,'YYYY/MM/DD') CREATED, "
                            + "      TO_CHAR(LAST_DDL_TIME,'YYYY/MM/DD') MODIFIED,STATUS "
                            + "  FROM ALL_OBJECTS "
                            + "  WHERE OBJECT_TYPE = :P_TYPE "
                            + "    AND (:P_OWNER IS NULL OR UPPER(OWNER) = :P_OWNER) "
                            + "    AND OBJECT_NAME LIKE :P_NAME");
        }};

        final void listUsage(CommandLog log) {
            log.println("Usage: LIST type pattern");
            log.println();
            log.println("TYPE   :");
            log.println("  OBJECT CLUSTER INDEX SEQUENCE SYNONYM TABLE TRIGGER");
            log.println("  TYPE VIEW MVIEW TABPART INDPART SEGMENT REBUILD LOB QUEUE");
            log.println("PATTERN:");
            log.println("  user.name ('_' as any one char, '%' as any string)");
        }

    }

    class SourceProcessor {

        String   str;
        String[] args;

        public SourceProcessor(String str, String[] args) {
            this.str  = str;
            this.args = args;
        }

        boolean proc() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return false;
            }
            if (args.length == 0) {
                out.println("Usage: SOURCE user.pattern");
                return false;
            }
            VariableTable table = new VariableTable();
            table.add("P_OWNER", 12);
            table.add("P_NAME", 12);
            table.add("P_TYPE", 12);
            setTable(args, table);
            DBRowCache rowCache1 = null;
            DBRowCache rowCache2 = null;
            if (args.length == 1) {
                try {
                    rowCache2 = executor
                            .executeQuery(executor.database, SOURCE_SQLS.get("USER_OBJECTS"), table,
                                    10000);
                } catch (SQLException localSQLException1) {
                    out.print(localSQLException1);
                }
            } else if (args.length == 2) {
                try {
                    rowCache2 = executor
                            .executeQuery(executor.database, SOURCE_SQLS.get("ALL_OBJECTS"), table,
                                    10000);
                } catch (SQLException localSQLException2) {
                    out.print(localSQLException2);
                }
            }

            if ((rowCache2 == null) || (rowCache2.getRowCount() == 0)) {
                if (args.length == 1) {
                    try {
                        rowCache2 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("RETRY_1"), table,
                                        10000);
                    } catch (SQLException localSQLException3) {
                        out.print(localSQLException3);
                    }
                }
                if (args.length == 2) {
                    try {
                        rowCache2 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("RETRY_2"), table,
                                        10000);
                    } catch (SQLException localSQLException4) {
                        out.print(localSQLException4);
                    }
                }
            }

            if (((rowCache2 == null) || (rowCache2.getRowCount() == 0))
                    && (args.length == 1)) {
                try {
                    rowCache2 = executor
                            .executeQuery(executor.database, SOURCE_SQLS.get("RETRY_3"), table,
                                    10000);
                } catch (SQLException localSQLException5) {
                    out.print(localSQLException5);
                }
            }
            String str2;
            for (int j = rowCache2.getRowCount(); j > 1; j--) {
                str2 = (String) rowCache2.getItem(j, 1);
                String str3 = (String) rowCache2.getItem(j, 3);
                if ((!str2.equals("SYS")) || ((!str3.equals("PACKAGE BODY")) && (!str3
                        .equals("TYPE BODY")))) { continue; }
                rowCache2.deleteRow(j);
            }
            if ((rowCache2 == null) || (rowCache2.getRowCount() == 0)) {
                out.println("No source object founded!");
                return false;
            }

            for (int j = 1; j <= rowCache2.getRowCount(); j++) {
                table.setValue("P_OWNER", rowCache2.getItem(j, 1));
                table.setValue("P_NAME", rowCache2.getItem(j, 2));
                table.setValue("P_TYPE", rowCache2.getItem(j, 3));
                str2 = (String) rowCache2.getItem(j, 3);
                try {
                    if (str2.equalsIgnoreCase("VIEW")) {
                        rowCache1 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("VIEW"), table,
                                        5000);
                    } else if (str2.equalsIgnoreCase("MATERIALIZED VIEW")) {
                        rowCache1 = executor.executeQuery(executor.database,
                                SOURCE_SQLS.get("MATERIALIZED VIEW"), table, 5000);
                    } else if (str2.equalsIgnoreCase("TRIGGER")) {
                        rowCache1 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("TRIGGER"), table,
                                        5000);
                    } else if (str2.equalsIgnoreCase("INDEX")) {
                        rowCache1 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("INDEX"), table,
                                        5000);
                    } else if (str2.equalsIgnoreCase("TABLE")) {
                        rowCache1 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("TABLE"), table,
                                        5000);
                    } else {
                        rowCache1 = executor
                                .executeQuery(executor.database, SOURCE_SQLS.get("ALL_SOURCE"),
                                        table, 10000);
                        out.print("CREATE OR REPLACE ");
                    }
                } catch (SQLException localSQLException6) {
                    out.print(localSQLException6);
                    continue;
                }
                if ((rowCache1 != null) && (rowCache1.getRowCount() > 0)) {
                    for (int k = 1; k <= rowCache1.getRowCount(); k++) {
                        out.println(executor.removeNewLine((String) rowCache1.getItem(k, 1)));
                    }
                    if (j >= rowCache2.getRowCount()) { continue; }
                    out.println();
                    out.println();
                } else {
                    out.println("No source founded!");
                }
            }
            return true;
        }


        final Map<String, String> SOURCE_SQLS = new HashMap<String, String>() {{
            put("USER_OBJECTS",
                    "SELECT /* AnySQL */ USER OWNER,OBJECT_NAME,OBJECT_TYPE FROM USER_OBJECTS "
                            + "  WHERE OBJECT_NAME=:P_NAME AND "
                            + "  OBJECT_TYPE IN ('VIEW','PACKAGE','PACKAGE BODY','PROCEDURE',"
                            + "  'FUNCTION','TRIGGER','TYPE','TYPE BODY','MATERIALIZED VIEW','INDEX','TABLE') "
                            + "  ORDER BY OBJECT_TYPE");

            put("ALL_OBJECTS", "SELECT /* AnySQL */ OWNER,OBJECT_NAME,OBJECT_TYPE FROM ALL_OBJECTS "
                    + "  WHERE OWNER=:P_OWNER AND OBJECT_NAME=:P_NAME AND "
                    + "  OBJECT_TYPE IN ('VIEW','PACKAGE','PACKAGE BODY','PROCEDURE', "
                    + "  'FUNCTION','TRIGGER','TYPE','TYPE BODY','MATERIALIZED VIEW','INDEX','TABLE') "
                    + "  ORDER BY OBJECT_TYPE");

            put("RETRY_1", "SELECT /* AnySQL */ OWNER,OBJECT_NAME,OBJECT_TYPE FROM ALL_OBJECTS "
                    + "WHERE (OWNER,OBJECT_NAME)  "
                    + "   IN (SELECT TABLE_OWNER,TABLE_NAME FROM USER_SYNONYMS "
                    + "       WHERE SYNONYM_NAME=:P_NAME AND "
                    + "       DB_LINK IS NULL) "
                    + "  AND OBJECT_TYPE IN ('VIEW','PACKAGE','PACKAGE BODY','PROCEDURE',"
                    + "      'FUNCTION','TRIGGER','TYPE','TYPE BODY','MATERIALIZED VIEW','INDEX','TABLE') "
                    + "  ORDER BY OBJECT_TYPE");

            put("RETRY_2", "SELECT /* AnySQL */ OWNER,OBJECT_NAME,OBJECT_TYPE FROM ALL_OBJECTS "
                    + "WHERE (OWNER,OBJECT_NAME)  "
                    + "   IN (SELECT TABLE_OWNER,TABLE_NAME FROM ALL_SYNONYMS "
                    + "       WHERE OWNER = :P_OWNER AND SYNONYM_NAME=:P_NAME AND "
                    + "       DB_LINK IS NULL) "
                    + "  AND OBJECT_TYPE IN ('VIEW','PACKAGE','PACKAGE BODY','PROCEDURE',"
                    + "  'FUNCTION','TRIGGER','TYPE','TYPE BODY','MATERIALIZED VIEW','INDEX','TABLE') "
                    + "  ORDER BY OBJECT_TYPE");

            put("RETRY_3", "SELECT /* AnySQL */ OWNER,OBJECT_NAME,OBJECT_TYPE FROM ALL_OBJECTS "
                    + "WHERE (OWNER,OBJECT_NAME)  "
                    + "   IN (SELECT TABLE_OWNER,TABLE_NAME FROM ALL_SYNONYMS "
                    + "       WHERE OWNER = 'PUBLIC' AND SYNONYM_NAME=:P_NAME AND "
                    + "       DB_LINK IS NULL) "
                    + "  AND OBJECT_TYPE IN ('VIEW','PACKAGE','PACKAGE BODY','PROCEDURE',"
                    + "      'FUNCTION','TRIGGER','TYPE','TYPE BODY','MATERIALIZED VIEW','INDEX','TABLE') "
                    + "  ORDER BY OBJECT_TYPE");

            put("VIEW", "SELECT /* AnySQL */ "
                    + "DBMS_METADATA.GET_DDL('VIEW',:P_NAME,:P_OWNER) FROM DUAL");

            put("MATERIALIZED VIEW", "SELECT /* AnySQL */ "
                    + "DBMS_METADATA.GET_DDL('MATERIALIZED_VIEW',:P_NAME,:P_OWNER) FROM DUAL");

            put("TRIGGER", "SELECT /* AnySQL */ "
                    + "DBMS_METADATA.GET_DDL('TRIGGER',:P_NAME,:P_OWNER) FROM DUAL");

            put("INDEX",
                    "SELECT /* AnySQL */ DBMS_METADATA.GET_DDL('INDEX',:P_NAME,:P_OWNER) FROM DUAL");

            put("TABLE", "SELECT /* AnySQL */ "
                    + "DBMS_METADATA.GET_DDL('TABLE',:P_NAME,:P_OWNER) FROM DUAL");
            put("ALL_SOURCE", "SELECT /* AnySQL */ TEXT FROM ALL_SOURCE "
                    + " WHERE OWNER=:P_OWNER AND NAME=:P_NAME AND TYPE=:P_TYPE "
                    + " ORDER BY LINE");
        }};

    }

    class ShowProcessor {

        String   str;
        String[] args;

        public ShowProcessor(String str, String[] args) {
            this.str  = str;
            this.args = args;
        }

        boolean proc() {
            if (args.length > 0) {
                int j = executor.commandAt(ORACLE_SHOW_KEYS, new String[]{args[0]});
                switch (j) {
                    case ORACLE_SHOW_USER:
                        procShowUser();
                        break;
                    case ORACLE_SHOW_SGA:
                        procShowSGA();
                        break;
                    case ORACLE_SHOW_SESSION:
                        procShowSession();
                        break;
                    case ORACLE_SHOW_CONSTRAINT:
                    case ORACLE_SHOW_CONS:
                        procShowConstraint();
                        break;
                    case ORACLE_SHOW_PARENT:
                        procShowParent();
                        break;
                    case ORACLE_SHOW_CHILD:
                        procShowChild();
                        break;
                    case ORACLE_SHOW_VERSION:
                        procShowVersion();
                        break;
                    case ORACLE_SHOW_SPACE:
                        procShowSpace();
                        break;
                    case ORACLE_SHOW_TABLE:
                        procShowTable();
                        break;
                    case ORACLE_SHOW_STATS:
                        procShowStats();
                        break;
                    case ORACLE_SHOW_LOAD:
                        procShowLoad();
                        break;
                    case ORACLE_SHOW_TOP_SQL:
                        procShowTopSQL();
                        break;
                    case ORACLE_SHOW_WAIT:
                        procShowWait();
                        break;
                    case ORACLE_SHOW_VARIABLE:
                        procShowVariable();
                        break;
                    case ORACLE_SHOW_ERRORS:
                        procShowErrors();
                        break;
                    default:
                        showUsage(out);
                        return false;
                }
                return true;
            } else {
                showUsage(out);
                return false;
            }
        }

        void procShowUser() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            try {
                DBRowCache localDBRowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_USER),
                                executor.sysVariable, 5000);
                executor.showDBRowCache(localDBRowCache, false);
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
        }

        void procShowSGA() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            try {
                DBRowCache localDBRowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_SGA),
                                executor.sysVariable, 5000);
                executor.showDBRowCache(localDBRowCache, false);
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
        }

        void procShowVersion() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            try {
                DBRowCache localDBRowCache = executor.executeQuery(executor.database,
                        SHOW_SQLS.get(ORACLE_SHOW_VERSION), executor.sysVariable, 5000);
                executor.showDBRowCache(localDBRowCache, false);
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
        }

        void procShowParent() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            DBRowCache rowCache = null;
            String     newStr   = executor.skipWord(str, 1);
            newStr = newStr.trim();
            String[] arrayOfString = TextUtils.toStringArray(
                    TextUtils.getWords(newStr, new String[]{"."}));
            if (arrayOfString.length == 0) {
                out.println("Table name required.");
                return;
            }
            VariableTable table = new VariableTable();
            table.add("P_OWNER", 12);
            table.add("P_NAME", 12);
            setTable(arrayOfString, table);
            try {
                rowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_PARENT), table,
                                2000);
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
                return;
            }
            if ((rowCache == null) || (rowCache.getRowCount() == 0)) {
                out.println("Table " + newStr + " not exists or have no parent table!");
                return;
            }
            executor.showDBRowCache(rowCache, true);
        }

        void procShowTable() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            DBRowCache localDBRowCache = null;
            String     newStr          = executor.skipWord(str, 1);
            newStr = newStr.trim();
            String[] arrayOfString = TextUtils
                    .toStringArray(TextUtils.getWords(newStr, new String[]{"."}));
            if (arrayOfString.length == 0) {
                out.println("Table name required.");
                return;
            }
            VariableTable table = new VariableTable();
            table.add("P_OWNER", 12);
            table.add("P_NAME", 12);
            if ((arrayOfString.length == 1) || (arrayOfString.length == 2)) {
                if (arrayOfString.length == 1) {
                    table.setValue("P_NAME", arrayOfString[0].toUpperCase());
                }
                if (arrayOfString.length == 2) {
                    table.setValue("P_OWNER", arrayOfString[0].toUpperCase());
                    table.setValue("P_NAME", arrayOfString[1].toUpperCase());
                }
            }
            try {
                localDBRowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_INDEX), table,
                                2000);
            } catch (SQLException localSQLException1) {
                out.print(localSQLException1);
                return;
            }
            out.println("Index List:");
            out.print(localDBRowCache);
            try {
                localDBRowCache = executor.executeQuery(executor.database,
                        SHOW_SQLS.get(ORACLE_SHOW_TABLE_CONSTRAINT), table, 2000);
            } catch (SQLException localSQLException2) {
                out.print(localSQLException2);
                return;
            }
            out.println();
            out.println("Constraint List:");
            out.print(localDBRowCache);
            try {
                localDBRowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_TABLE_TRIGGER),
                                table, 2000);
            } catch (SQLException localSQLException3) {
                out.print(localSQLException3);
                return;
            }
            out.println();
            out.println("Trigger List:");
            out.print(localDBRowCache);
        }

        void procShowWait() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            Object        localObject        = null;
            String        str1               = executor.skipWord(str, 1);
            OptionCommand localOptionCommand = new OptionCommand(str1);
            String        str2               = null;
            int           i                  = 1000000;
            int           j                  = 5;
            executor.exitShowLoop = false;
            str2                  = localOptionCommand.getOption("S", null);
            i                     = localOptionCommand.getInt("C", 1000000);
            j                     = localOptionCommand.getInt("T", 5);
            if (i < 1) { i = 1; }
            if (j < 5) { j = 5; }
            if (j > 300) { j = 300; }
            DBRowCache       rowCache1             = null;
            DBRowCache       rowCache2             = getSessionWait(str2);
            SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
            localSimpleDBRowCache.copyStruct(rowCache2);
            for (int k = 0; (k < i) && (!executor.exitShowLoop); k++) {
                try {
                    Thread.currentThread();
                    Thread.sleep(j * 1000);
                } catch (InterruptedException localInterruptedException) {
                    return;
                }
                rowCache1 = getSessionWait(str2);
                localSimpleDBRowCache.deleteAllRow();
                out.println("Time: " + DateOperator.getDay("yyyy-MM-dd HH:mm:ss"));
                out.println("  Waits     Time  Event");
                out.println("-----------------------------------------------------");
                if ((rowCache2 == null) || (rowCache1 == null)) { return; }
                if (rowCache2.getColumnCount() == 0) { return; }
                if (rowCache2.getRowCount() == rowCache1.getRowCount()) {
                    for (int m = 1; m <= rowCache2.getRowCount(); m++) {
                        String     str3             = (String) rowCache2.getItem(m, 1);
                        BigInteger localBigInteger1 = new BigInteger(
                                rowCache2.getItem(m, 2).toString());
                        BigInteger localBigInteger3 = new BigInteger(
                                rowCache2.getItem(m, 3).toString());
                        BigInteger localBigInteger2 = new BigInteger(
                                rowCache1.getItem(m, 2).toString());
                        BigInteger localBigInteger4 = new BigInteger(
                                rowCache1.getItem(m, 3).toString());
                        BigInteger localBigInteger5 = localBigInteger2
                                .add(localBigInteger1.negate());
                        BigInteger localBigInteger6 = localBigInteger4
                                .add(localBigInteger3.negate());
                        if ((localBigInteger6.longValue() <= 0L) || (!str3
                                .equals(rowCache1.getItem(m, 1)))) { continue; }
                        int n = localSimpleDBRowCache.appendRow();
                        localSimpleDBRowCache.setItem(n, 1, str3);
                        localSimpleDBRowCache.setItem(n, 2, localBigInteger5);
                        localSimpleDBRowCache.setItem(n, 3, localBigInteger6);
                    }
                    localSimpleDBRowCache.quicksort(3, false);
                    for (int m = 1; m <= localSimpleDBRowCache.getRowCount(); m++) {
                        out.print(executor.rpad(localSimpleDBRowCache.getItem(m, 2) + " ", 8));
                        out.print(executor.rpad(localSimpleDBRowCache.getItem(m, 3) + "  ", 10));
                        out.println((String) localSimpleDBRowCache.getItem(m, 1));
                    }
                }
                if (k < i - 1) { out.println(); }
                rowCache2 = rowCache1;
            }
            rowCache2 = null;
        }

        void procShowVariable() {
            String[] arrayOfString = executor.sysVariable.getNames();
            if (arrayOfString.length > 0) {
                out.println(" Name                           Type");
                out.println(" ------------------------------ ------------");
                for (int i = 0; i < arrayOfString.length; i++) {
                    out.print(" " + executor.lpad(arrayOfString[i], 30));
                    out.print(" ");
                    out.println(
                            SQLTypes.getTypeName(executor.sysVariable.getType(arrayOfString[i])));
                }
            }
            out.println("No variables defined.");
        }

        void procShowErrors() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            try {
                VariableTable table = new VariableTable();
                table.add("P_OWNER", 12);
                table.add("P_TYPE", 12);
                table.add("P_NAME", 12);
                table.setValue("P_OWNER", executor.lastObjectOwner);
                table.setValue("P_TYPE", executor.lastObjectType);
                table.setValue("P_NAME", executor.lastObjectName);
                DBRowCache localDBRowCache = executor.executeQuery(executor.database,
                        SHOW_SQLS.get(ORACLE_SHOW_ERRORS), table, 1000);
                if (localDBRowCache.getRowCount() > 0) {
                    out.print(executor.lastObjectType);
                    out.print(" ");
                    out.print(executor.lastObjectName);
                    out.println(" ERRORS:");
                    out.println();
                    executor.showDBRowCache(localDBRowCache, true);
                } else {
                    out.println("No errors.");
                }
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
        }

        void procShowStats() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            String        str1               = executor.skipWord(str, 1);
            OptionCommand localOptionCommand = new OptionCommand(str1);
            String        str2               = null;
            int           i                  = 10000000;
            int           j                  = 5;
            String        str3               = "%";
            executor.exitShowLoop = false;
            str2                  = localOptionCommand.getOption("S", null);
            i                     = localOptionCommand.getInt("C", 1000000);
            j                     = localOptionCommand.getInt("T", 5);
            if (i < 1) { i = 1; }
            if (j < 5) { j = 5; }
            if (j > 300) { j = 300; }
            DBRowCache rowCache1 = null;
            DBRowCache rowCache2 = getSessionStats(str2, str3);
            for (int k = 0; (k < i) && (!executor.exitShowLoop); k++) {
                try {
                    Thread.currentThread();
                    Thread.sleep(j * 1000);
                } catch (InterruptedException localInterruptedException) {
                    return;
                }
                rowCache1 = getSessionStats(str2, str3);
                out.println("Statistics (" + DateOperator.getDay("yyyy-MM-dd HH:mm:ss") + ")");
                out.println("-----------------------------------------------------------");
                if ((rowCache2 == null) || (rowCache1 == null)) { return; }
                if (rowCache2.getRowCount() == rowCache1.getRowCount()) {
                    for (int m = 1; m <= rowCache2.getRowCount(); m++) {
                        Object     obj2 = rowCache2.getItem(m, 1);
                        Object     obj1 = rowCache1.getItem(m, 1);
                        BigInteger big1 = new BigInteger(rowCache2.getItem(m, 2).toString());
                        BigInteger big2 = new BigInteger(rowCache1.getItem(m, 2).toString());
                        BigInteger big3 = big2.add(big1.negate());
                        if ((big3.longValue() <= 0L) || (!obj2.equals(obj1))) { continue; }
                        out.println(executor.rpad(new StringBuilder().append(" ")
                                                                     .append(formatStatsValue(
                                                                             big3.longValue()))
                                                                     .toString(), 8)
                                + "   " + executor
                                .lpad(executor.hashtable.get(obj2).toString(), 32));
                    }
                }
                if (k < i - 1) { out.println(); }
                rowCache2 = rowCache1;
            }
        }

        void procShowLoad() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            String        str1               = executor.skipWord(str, 1);
            OptionCommand localOptionCommand = new OptionCommand(str1);
            int           i                  = 10000000;
            int           j                  = 3;
            int           k                  = 8;
            String        str2               = null;
            long          l1                 = 0L;
            long          l2                 = 0L;
            float         f                  = 0.0F;
            Hashtable     localHashtable     = new Hashtable();
            executor.exitShowLoop = false;
            str2                  = localOptionCommand.getString("S", null);
            i                     = localOptionCommand.getInt("C", 1000000);
            j                     = localOptionCommand.getInt("T", 10);
            k                     = localOptionCommand.getInt("N", 8);
            if (k < 5) { k = 5; }
            if (i < 1) { i = 1; }
            if (j < 5) { j = 5; }
            if (j > 900) { j = 900; }
            DBRowCache       localDBRowCache1       = null;
            DBRowCache       localDBRowCache2       = null;
            DBRowCache       localDBRowCache3       = null;
            DBRowCache       localDBRowCache4       = getSessionWait(str2);
            DBRowCache       localDBRowCache5       = getSQLExecution();
            DBRowCache       localDBRowCache6       = getSessionStats(str2, "");
            SimpleDBRowCache localSimpleDBRowCache1 = new SimpleDBRowCache();
            SimpleDBRowCache localSimpleDBRowCache2 = new SimpleDBRowCache();
            localSimpleDBRowCache1.copyStruct(localDBRowCache4);
            localSimpleDBRowCache2.copyStruct(localDBRowCache5);
            if (localDBRowCache5 != null) {
                for (int m = 1; m < localDBRowCache5.getRowCount(); m++) {
                    localHashtable.put(localDBRowCache5.getItem(m, 1), new Integer(m));
                }
            }
            l1 = System.currentTimeMillis();
            for (int m = 0; (m < i) && (!executor.exitShowLoop); m++) {
                try {
                    Thread.currentThread();
                    Thread.sleep(j * 1000);
                } catch (InterruptedException localInterruptedException) {
                    return;
                }
                localDBRowCache2 = getSessionWait(str2);
                localDBRowCache1 = getSessionStats(str2, "");
                localDBRowCache3 = getSQLExecution();
                l2               = System.currentTimeMillis();
                f                = 1.0F * (float) (l2 - l1) / 1000.0F;
                l1               = l2;
                localSimpleDBRowCache1.deleteAllRow();
                if (str2 == null) {
                    out.println("System Load (" + DateOperator.getDay("yyyy-MM-dd HH:mm:ss") + ")");
                } else {
                    out.println("Session: " + str2 + " Load (" + DateOperator
                            .getDay("yyyy-MM-dd HH:mm:ss") + ")");
                }
                out.println(
                        "---System Statistics------------------------------------------------------------------");
                if ((localDBRowCache6 == null) || (localDBRowCache1 == null)) { return; }
                Object     localObject2;
                Object     localObject3;
                BigInteger localBigInteger1;
                BigInteger bi2;
                BigInteger bi3;
                if (localDBRowCache6.getRowCount() == localDBRowCache1.getRowCount()) {
                    for (int i1 = 1; i1 <= localDBRowCache6.getRowCount(); i1++) {
                        localObject2     = localDBRowCache1.getItem(i1, 1);
                        localObject3     = localDBRowCache6.getItem(i1, 1);
                        localBigInteger1 = new BigInteger(
                                localDBRowCache6.getItem(i1, 2).toString());
                        bi2              = new BigInteger(
                                localDBRowCache1.getItem(i1, 2).toString());
                        bi3              = bi2.add(localBigInteger1.negate());
                        if (!localObject2.equals(localObject3)) { continue; }
                        if (i1 % 2 == 1) {
                            long v = (long) ((float) bi3.longValue() / f);
                            out.print(executor.rpad(new StringBuilder().append(" ").append(
                                    formatStatsValue(bi3.longValue())).toString(), 6)
                                    + executor.rpad(new StringBuilder().append(" ")
                                                                       .append(formatStatsValue(v))
                                                                       .toString(), 6)
                                    + "  " + executor
                                    .lpad(executor.hashtable.get(localObject2).toString(), 32));
                        } else {
                            long v = (long) ((float) bi3.longValue() / f);
                            out.println(executor.rpad(new StringBuilder().append(" ").append(
                                    formatStatsValue(bi3.longValue())).toString(), 6)
                                    + executor.rpad(new StringBuilder().append(" ")
                                                                       .append(formatStatsValue(v))
                                                                       .toString(), 6)
                                    + "  " + executor
                                    .lpad(executor.hashtable.get(localObject2).toString(), 32));
                        }

                    }
                    if (localDBRowCache6.getRowCount() % 2 == 1) { out.println(); }
                }
                localDBRowCache6 = localDBRowCache1;
                BigInteger bi4;
                BigInteger bi5;
                int        i4;
                if ((localDBRowCache2 != null) && (localDBRowCache6 != null) && (
                        localDBRowCache4.getRowCount() == localDBRowCache2.getRowCount())) {
                    out.println();
                    out.println(
                            "----Waits---Waits/S-----Time-----Time/S--Pct--Event-----------------------------------");
                    for (int i2 = 1; i2 <= localDBRowCache4.getRowCount(); i2++) {
                        localObject2     = (String) localDBRowCache4.getItem(i2, 1);
                        localObject3     = new BigInteger(
                                localDBRowCache4.getItem(i2, 2).toString());
                        bi2              = new BigInteger(
                                localDBRowCache4.getItem(i2, 3).toString());
                        localBigInteger1 = new BigInteger(
                                localDBRowCache2.getItem(i2, 2).toString());
                        bi3              = new BigInteger(
                                localDBRowCache2.getItem(i2, 3).toString());
                        bi4              = localBigInteger1
                                .add(((BigInteger) localObject3).negate());
                        bi5              = bi3.add(bi2.negate());
                        if ((bi5.longValue() <= 0L) || (!((String) localObject2)
                                .equals(localDBRowCache2.getItem(i2, 1)))) { continue; }
                        int i3 = localSimpleDBRowCache1.appendRow();
                        localSimpleDBRowCache1.setItem(i3, 1, localObject2);
                        localSimpleDBRowCache1.setItem(i3, 2, bi4);
                        localSimpleDBRowCache1.setItem(i3, 3, bi5);
                    }
                    localSimpleDBRowCache1.quicksort(3, false);
                    double d = localSimpleDBRowCache1.sum("WTIME");
                    for (i4 = 1; (i4 <= localSimpleDBRowCache1.getRowCount()) && (i4 <= 5); i4++) {
                        bi4 = (BigInteger) localSimpleDBRowCache1.getItem(i4, 2);
                        bi5 = (BigInteger) localSimpleDBRowCache1.getItem(i4, 3);
                        out.print(executor.rpad(bi4.toString() + " ", 10));
                        out.print(executor.rpad((long) ((float) bi4.longValue() / f) + " ", 10));
                        out.print(executor.rpad(bi5.toString() + "  ", 10));
                        out.print(executor.rpad((long) ((float) bi5.longValue() / f) + " ", 10));
                        out.print(executor.rpad((int) (100.0D * bi5.doubleValue() / d) + "  ", 6));
                        out.println((String) localSimpleDBRowCache1.getItem(i4, 1));
                    }
                }
                localDBRowCache4 = localDBRowCache2;
                if ((localDBRowCache5 != null) && (localDBRowCache3 != null) && (
                        localDBRowCache5.getRowCount() > 0) && (localDBRowCache3.getRowCount()
                        > 0)) {
                    int n = 0;
                    localSimpleDBRowCache2.deleteAllRow();
                    for (i4 = 1; i4 < localDBRowCache3.getRowCount(); i4++) {
                        if (localHashtable.containsKey(localDBRowCache3.getItem(i4, 1))) {
                            int i5 = ((Integer) localHashtable.get(localDBRowCache3.getItem(i4, 1)))
                                    .intValue();
                            localObject3     = new BigInteger(
                                    localDBRowCache5.getItem(i5, 2).toString());
                            localBigInteger1 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 2).toString());
                            if (localBigInteger1.compareTo((BigInteger) localObject3) <= 0) {
                                continue;
                            }
                            n = localSimpleDBRowCache2.appendRow();
                            localSimpleDBRowCache2.setItem(n, 1, localDBRowCache3.getItem(i4, 1));
                            localSimpleDBRowCache2.setItem(n, 2,
                                    localBigInteger1.add(((BigInteger) localObject3).negate()));
                            localObject3     = new BigInteger(
                                    localDBRowCache5.getItem(i5, 3).toString());
                            localBigInteger1 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 3).toString());
                            localSimpleDBRowCache2.setItem(n, 3,
                                    localBigInteger1.add(((BigInteger) localObject3).negate()));
                            localObject3     = new BigInteger(
                                    localDBRowCache5.getItem(i5, 4).toString());
                            localBigInteger1 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 4).toString());
                            localSimpleDBRowCache2.setItem(n, 4,
                                    localBigInteger1.add(((BigInteger) localObject3).negate()));
                            localObject3     = new BigInteger(
                                    localDBRowCache5.getItem(i5, 5).toString());
                            localBigInteger1 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 5).toString());
                            localSimpleDBRowCache2.setItem(n, 5,
                                    localBigInteger1.add(((BigInteger) localObject3).negate()));
                            localObject3     = new BigInteger(
                                    localDBRowCache5.getItem(i5, 6).toString());
                            localBigInteger1 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 6).toString());
                            localSimpleDBRowCache2.setItem(n, 6,
                                    localBigInteger1.add(((BigInteger) localObject3).negate()));
                        } else {
                            localObject3 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 2).toString());
                            if (((BigInteger) localObject3).longValue() <= 0L) { continue; }
                            n = localSimpleDBRowCache2.appendRow();
                            localSimpleDBRowCache2.setItem(n, 1, localDBRowCache3.getItem(i4, 1));
                            localSimpleDBRowCache2.setItem(n, 2, localObject3);
                            localObject3 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 3).toString());
                            localSimpleDBRowCache2.setItem(n, 3, localObject3);
                            localObject3 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 4).toString());
                            localSimpleDBRowCache2.setItem(n, 4, localObject3);
                            localObject3 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 5).toString());
                            localSimpleDBRowCache2.setItem(n, 5, localObject3);
                            localObject3 = new BigInteger(
                                    localDBRowCache3.getItem(i4, 6).toString());
                            localSimpleDBRowCache2.setItem(n, 6, localObject3);
                        }
                    }
                    localSimpleDBRowCache2.quicksort(3, false);
                    out.println();
                    out.println(
                            "---Exec--Exe/S--Get/S--Get/E--Dsk/S--Dsk/E--Sorts--Row/E------HASH---(Order by Gets)--");
                    BigInteger bi6;
                    for (i4 = 1; (i4 <= k) && (i4 <= localSimpleDBRowCache2.getRowCount()); i4++) {
                        bi2 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 2);
                        bi3 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 3);
                        bi4 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 4);
                        bi5 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 5);
                        bi6 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 6);
                        out.println(executor.rpad(new StringBuilder().append(" ").append(
                                formatStatsValue(bi2.longValue())).toString(), 7)
                                + executor
                                .rpad(formatStatsValue((long) ((float) bi2.longValue() / f)), 7)
                                + executor
                                .rpad(formatStatsValue((long) ((float) bi3.longValue() / f)), 7)
                                + executor
                                .rpad(formatStatsValue(bi3.longValue() / bi2.longValue()), 7)
                                + executor
                                .rpad(formatStatsValue((long) ((float) bi4.longValue() / f)), 7)
                                + executor
                                .rpad(formatStatsValue(bi4.longValue() / bi2.longValue()), 7)
                                + executor.rpad(formatStatsValue(bi6.longValue()), 7)
                                + executor
                                .rpad(formatStatsValue(bi5.longValue() / bi2.longValue()), 7)
                                + executor
                                .rpad(localSimpleDBRowCache2.getItem(i4, 1).toString(), 16));
                    }
                    localSimpleDBRowCache2.quicksort(4, false);
                    out.println();
                    out.println(
                            "---Exec--Exe/S--Get/S--Get/E--Dsk/S--Dsk/E--Sorts--Row/E------HASH---(Order by Disk)--");
                    for (i4 = 1; (i4 <= k) && (i4 <= localSimpleDBRowCache2.getRowCount()); i4++) {
                        bi2 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 2);
                        bi3 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 3);
                        bi4 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 4);
                        bi5 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 5);
                        bi6 = (BigInteger) localSimpleDBRowCache2.getItem(i4, 6);
                        out.println(executor.rpad(new StringBuilder().append(" ").append(
                                formatStatsValue(bi2.longValue())).toString(), 7)
                                + executor
                                .rpad(formatStatsValue((long) ((float) bi2.longValue() / f)), 7)
                                + executor
                                .rpad(formatStatsValue((long) ((float) bi3.longValue() / f)), 7)
                                + executor
                                .rpad(formatStatsValue(bi3.longValue() / bi2.longValue()), 7)
                                + executor
                                .rpad(formatStatsValue((long) ((float) bi4.longValue() / f)), 7)
                                + executor
                                .rpad(formatStatsValue(bi4.longValue() / bi2.longValue()), 7)
                                + executor.rpad(formatStatsValue(bi6.longValue()), 7)
                                + executor
                                .rpad(formatStatsValue(bi5.longValue() / bi2.longValue()), 7)
                                + executor
                                .rpad(localSimpleDBRowCache2.getItem(i4, 1).toString(), 16))
                        ;
                    }
                    if (localDBRowCache3 != null) {
                        localHashtable.clear();
                        for (i4 = 1; i4 < localDBRowCache3.getRowCount(); i4++) {
                            localHashtable.put(localDBRowCache3.getItem(i4, 1), new Integer(i4));
                        }
                    }
                }
                localDBRowCache5 = localDBRowCache3;
                out.println();
                System.runFinalization();
                System.gc();
            }
        }

        DBRowCache getSessionStats(String paramString1, String paramString2) {
            DBRowCache    localDBRowCache    = null;
            VariableTable localVariableTable = new VariableTable();
            localVariableTable.add("P_SID", 12);
            localVariableTable.add("P_NAME", 12);
            localVariableTable.setValue("P_SID", paramString1);
            localVariableTable.setValue("P_NAME", paramString2);
            try {
                if (paramString1 == null) {
                    if (executor.traceSqls[1] != null) {
                        localDBRowCache = executor
                                .executeQuery(executor.traceSqls[1], localVariableTable, 1000);
                    }
                } else if (executor.traceSqls[2] != null) {
                    localDBRowCache = executor
                            .executeQuery(executor.traceSqls[2], localVariableTable, 1000);
                }
                return localDBRowCache;
            } catch (SQLException localSQLException) {
            }
            return null;
        }

        DBRowCache getSessionWait(String paramString) {
            DBRowCache    localDBRowCache    = null;
            VariableTable localVariableTable = new VariableTable();
            localVariableTable.add("P_SID", 12);
            localVariableTable.setValue("P_SID", paramString);
            try {
                if ((paramString != null) && (paramString.length() > 0)) {
                    if (executor.traceSqls[4] != null) {
                        localDBRowCache = executor
                                .executeQuery(executor.traceSqls[4], localVariableTable, 2000);
                    }
                } else if (executor.traceSqls[5] != null) {
                    localDBRowCache = executor
                            .executeQuery(executor.traceSqls[5], executor.sysVariable, 2000);
                }
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
            return localDBRowCache;
        }

        DBRowCache getSQLExecution() {
            DBRowCache localDBRowCache = null;
            try {
                if (executor.traceSqls[8] != null) {
                    localDBRowCache = executor
                            .executeQuery(executor.traceSqls[8], executor.sysVariable, 10000);
                }
                return localDBRowCache;
            } catch (SQLException localSQLException) {
            }
            return null;
        }

        void procShowSession() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            try {
                DBRowCache localDBRowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_LOAD),
                                executor.sysVariable, 5000);
                executor.showDBRowCache(localDBRowCache, true);
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
        }

        void procShowTopSQL() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            Object        localObject1       = null;
            String        str1               = executor.skipWord(str, 1);
            OptionCommand localOptionCommand = new OptionCommand(str1);
            int           i                  = 10000000;
            int           j                  = 3;
            int           k                  = 10;
            String        str2               = "ALL";
            Object        localObject2       = null;
            long          l1                 = 0L;
            long          l2                 = 0L;
            float         f                  = 0.0F;
            Hashtable     localHashtable     = new Hashtable();
            executor.exitShowLoop = false;
            k                     = localOptionCommand.getInt("N", 12);
            i                     = localOptionCommand.getInt("C", 1000000);
            j                     = localOptionCommand.getInt("T", 10);
            str2                  = localOptionCommand.getString("O", "ALL");
            if (i < 1) { i = 1; }
            if (j < 5) { j = 5; }
            if (j > 900) { j = 900; }
            if (k < 8) { k = 8; }
            DBRowCache       rowCache1 = null;
            DBRowCache       rowCache2 = getSQLExecution();
            SimpleDBRowCache rowCache  = new SimpleDBRowCache();
            rowCache.copyStruct(rowCache2);
            l1 = System.currentTimeMillis();
            if (rowCache2 != null) {
                for (int m = 1; m < rowCache2.getRowCount(); m++) {
                    localHashtable.put(rowCache2.getItem(m, 1), new Integer(m));
                }
            }
            for (int m = 0; (m < i) && (!executor.exitShowLoop); m++) {
                try {
                    Thread.currentThread();
                    Thread.sleep(j * 1000);
                } catch (InterruptedException localInterruptedException) {
                    return;
                }
                rowCache1 = getSQLExecution();
                l2        = System.currentTimeMillis();
                f         = 1.0F * (float) (l2 - l1) / 1000.0F;
                l1        = l2;
                if ((rowCache2 != null) && (rowCache1 != null) && (rowCache2.getRowCount() > 0)
                        && (rowCache1.getRowCount() > 0)) {
                    int n = 0;
                    rowCache.deleteAllRow();
                    for (int i1 = 1; i1 < rowCache1.getRowCount(); i1++) {
                        BigInteger bi1;
                        if (localHashtable.containsKey(rowCache1.getItem(i1, 1))) {
                            int i2 = ((Integer) localHashtable.get(rowCache1.getItem(i1, 1)))
                                    .intValue();
                            bi1 = new BigInteger(rowCache2.getItem(i2, 2).toString());
                            BigInteger bi2 = new BigInteger(rowCache1.getItem(i1, 2).toString());
                            if (bi2.compareTo(bi1) <= 0) { continue; }
                            n = rowCache.appendRow();
                            rowCache.setItem(n, 1, rowCache1.getItem(i1, 1));
                            rowCache.setItem(n, 2, bi2.add(bi1.negate()));
                            bi1 = new BigInteger(rowCache2.getItem(i2, 3).toString());
                            bi2 = new BigInteger(rowCache1.getItem(i1, 3).toString());
                            rowCache.setItem(n, 3, bi2.add(bi1.negate()));
                            bi1 = new BigInteger(rowCache2.getItem(i2, 4).toString());
                            bi2 = new BigInteger(rowCache1.getItem(i1, 4).toString());
                            rowCache.setItem(n, 4, bi2.add(bi1.negate()));
                            bi1 = new BigInteger(rowCache2.getItem(i2, 5).toString());
                            bi2 = new BigInteger(rowCache1.getItem(i1, 5).toString());
                            rowCache.setItem(n, 5, bi2.add(bi1.negate()));
                            bi1 = new BigInteger(rowCache2.getItem(i2, 6).toString());
                            bi2 = new BigInteger(rowCache1.getItem(i1, 6).toString());
                            rowCache.setItem(n, 6, bi2.add(bi1.negate()));
                        } else {
                            bi1 = new BigInteger(rowCache1.getItem(i1, 2).toString());
                            if (bi1.longValue() <= 0L) { continue; }
                            n = rowCache.appendRow();
                            rowCache.setItem(n, 1, rowCache1.getItem(i1, 1));
                            rowCache.setItem(n, 2, bi1);
                            bi1 = new BigInteger(rowCache1.getItem(i1, 3).toString());
                            rowCache.setItem(n, 3, bi1);
                            bi1 = new BigInteger(rowCache1.getItem(i1, 4).toString());
                            rowCache.setItem(n, 4, bi1);
                            bi1 = new BigInteger(rowCache1.getItem(i1, 5).toString());
                            rowCache.setItem(n, 5, bi1);
                            bi1 = new BigInteger(rowCache1.getItem(i1, 6).toString());
                            rowCache.setItem(n, 6, bi1);
                        }
                    }
                    out.println("Top SQL (" + DateOperator.getDay("yyyy-MM-dd HH:mm:ss") + ")");
                    BigInteger bi3;
                    BigInteger bi4;
                    BigInteger bi5;
                    BigInteger bi6;
                    BigInteger bi7;
                    if ((str2.equalsIgnoreCase("ALL")) || (str2.equalsIgnoreCase("EXEC"))) {
                        rowCache.quicksort(2, false);
                        out.println(
                                "---Exec--Exe/S--Get/S--Get/E--Dsk/S--Dsk/E--Sorts--Row/E------HASH---(Order by Exec)--");
                        for (int i1 = 1; (i1 <= k) && (i1 <= rowCache.getRowCount()); i1++) {
                            bi3 = (BigInteger) rowCache.getItem(i1, 2);
                            bi4 = (BigInteger) rowCache.getItem(i1, 3);
                            bi5 = (BigInteger) rowCache.getItem(i1, 4);
                            bi6 = (BigInteger) rowCache.getItem(i1, 5);
                            bi7 = (BigInteger) rowCache.getItem(i1, 6);
                            out.println(executor.rpad(new StringBuilder().append(" ").append(
                                    formatStatsValue(bi3.longValue())).toString(), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi3.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi4.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi4.longValue() / bi3.longValue()), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi5.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi5.longValue() / bi3.longValue()), 7)
                                    + executor.rpad(formatStatsValue(bi7.longValue()), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi6.longValue() / bi3.longValue()), 7)
                                    + executor.rpad(rowCache.getItem(i1, 1).toString(), 16))
                            ;
                        }
                        out.println();
                    }
                    if ((str2.equalsIgnoreCase("ALL")) || (str2.equalsIgnoreCase("GETS"))) {
                        rowCache.quicksort(3, false);
                        out.println(
                                "---Exec--Exe/S--Get/S--Get/E--Dsk/S--Dsk/E--Sorts--Row/E------HASH---(Order by Gets)--");
                        for (int i1 = 1; (i1 <= k) && (i1 <= rowCache.getRowCount()); i1++) {
                            bi3 = (BigInteger) rowCache.getItem(i1, 2);
                            bi4 = (BigInteger) rowCache.getItem(i1, 3);
                            bi5 = (BigInteger) rowCache.getItem(i1, 4);
                            bi6 = (BigInteger) rowCache.getItem(i1, 5);
                            bi7 = (BigInteger) rowCache.getItem(i1, 6);
                            out.println(executor.rpad(new StringBuilder().append(" ").append(
                                    formatStatsValue(bi3.longValue())).toString(), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi3.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi4.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi4.longValue() / bi3.longValue()), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi5.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi5.longValue() / bi3.longValue()), 7)
                                    + executor.rpad(formatStatsValue(bi7.longValue()), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi6.longValue() / bi3.longValue()), 7)
                                    + executor.rpad(rowCache.getItem(i1, 1).toString(), 16))
                            ;
                        }
                        out.println();
                    }
                    if ((str2.equalsIgnoreCase("ALL")) || (str2.equalsIgnoreCase("DISK"))) {
                        rowCache.quicksort(4, false);
                        out.println(
                                "---Exec--Exe/S--Get/S--Get/E--Dsk/S--Dsk/E--Sorts--Row/E------HASH---(Order by Disk)--");
                        for (int i1 = 1; (i1 <= k) && (i1 <= rowCache.getRowCount()); i1++) {
                            bi3 = (BigInteger) rowCache.getItem(i1, 2);
                            bi4 = (BigInteger) rowCache.getItem(i1, 3);
                            bi5 = (BigInteger) rowCache.getItem(i1, 4);
                            bi6 = (BigInteger) rowCache.getItem(i1, 5);
                            bi7 = (BigInteger) rowCache.getItem(i1, 6);
                            out.println(executor.rpad(new StringBuilder().append(" ").append(
                                    formatStatsValue(bi3.longValue())).toString(), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi3.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi4.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi4.longValue() / bi3.longValue()), 7)
                                    + executor
                                    .rpad(formatStatsValue((long) ((float) bi5.longValue() / f)), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi5.longValue() / bi3.longValue()), 7)
                                    + executor.rpad(formatStatsValue(bi7.longValue()), 7)
                                    + executor
                                    .rpad(formatStatsValue(bi6.longValue() / bi3.longValue()), 7)
                                    + executor.rpad(rowCache.getItem(i1, 1).toString(), 16));
                        }
                        out.println();
                    }
                    if (rowCache1 != null) {
                        localHashtable.clear();
                        for (int i1 = 1; i1 < rowCache1.getRowCount(); i1++) {
                            localHashtable.put(rowCache1.getItem(i1, 1), new Integer(i1));
                        }
                    }
                }
                rowCache2 = rowCache1;
                System.runFinalization();
                System.gc();
            }
        }

        void procShowChild() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            DBRowCache rowCache = null;
            String     newStr   = executor.skipWord(str, 1);
            newStr = newStr.trim();
            String[] arrayOfString = TextUtils.toStringArray(
                    TextUtils.getWords(newStr, new String[]{"."}));
            if (arrayOfString.length == 0) {
                out.println("Table name required.");
                return;
            }
            VariableTable table = new VariableTable();
            table.add("P_OWNER", 12);
            table.add("P_NAME", 12);
            if ((arrayOfString.length == 1) || (arrayOfString.length == 2)) {
                if (arrayOfString.length == 1) {
                    table.setValue("P_NAME", arrayOfString[0].toUpperCase());
                }
                if (arrayOfString.length == 2) {
                    table.setValue("P_OWNER", arrayOfString[0].toUpperCase());
                    table.setValue("P_NAME", arrayOfString[1].toUpperCase());
                }
            }
            try {
                rowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_CHILD), table,
                                2000);
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
                return;
            }
            if ((rowCache == null) || (rowCache.getRowCount() == 0)) {
                out.println("Table " + newStr + " not exists or have no child table!");
                return;
            }
            executor.showDBRowCache(rowCache, true);
        }

        void procShowConstraint() {
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return;
            }
            DBRowCache rowCache = null;
            String     str1     = executor.skipWord(str, 1);
            str1 = str1.trim();
            String[] arrayOfString = TextUtils
                    .toStringArray(TextUtils.getWords(str1, new String[]{"."}));
            if (arrayOfString.length == 0) {
                out.println("Constraint name required.");
                return;
            }
            VariableTable table = new VariableTable();
            table.add("P_OWNER", 12);
            table.add("P_NAME", 12);
            if ((arrayOfString.length == 1) || (arrayOfString.length == 2)) {
                if (arrayOfString.length == 1) {
                    table.setValue("P_NAME", arrayOfString[0].toUpperCase());
                }
                if (arrayOfString.length == 2) {
                    table.setValue("P_OWNER", arrayOfString[0].toUpperCase());
                    table.setValue("P_NAME", arrayOfString[1].toUpperCase());
                }
            }
            try {
                rowCache = executor
                        .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_CONSTRAINT),
                                table, 2000);
            } catch (SQLException e) {
                out.print(e);
                return;
            }
            if ((rowCache == null) || (rowCache.getRowCount() == 0)) {
                out.println("Constraint " + str1 + " not exists!");
                return;
            }
            executor.showDBRowCache(rowCache, true);
            String str2 = (String) rowCache.getItem(1, 1);
            if ((str2.equals("Primary Key"))
                    || (str2.equals("Unique"))
                    || (str2.equals("Foreign Key"))) {
                try {
                    rowCache = executor
                            .executeQuery(executor.database, SHOW_SQLS.get(ORACLE_SHOW_TABLE_KEYS),
                                    table, 2000);
                    if (rowCache.getRowCount() > 0) {
                        out.println();
                        executor.showDBRowCache(rowCache, true);
                    }
                } catch (SQLException localSQLException2) {
                    out.print(localSQLException2);
                    return;
                }
            } else {
                try {
                    rowCache = executor.executeQuery(executor.database,
                            SHOW_SQLS.get(ORACLE_SHOW_TABLE_OTHERS), table, 100);
                    if (rowCache.getRowCount() > 0) {
                        out.println();
                        executor.showDBRowCache(rowCache, false);
                    }
                } catch (SQLException localSQLException3) {
                    out.print(localSQLException3);
                    return;
                }
            }
        }

        void procShowSpace() {
            if (executor.checkNotConnected()) { return; }
            VariableTable table = new VariableTable();
            table.add("P_OWNER", 12);
            table.add("P_NAME", 12);
            table.add("P_LIMIT", 4);
            int    j      = TextUtils.getWords(ORACLE_SHOW_KEYS[8]).size();
            String newStr = executor.skipWord(str, j);
            String[] arrayOfString = TextUtils.toStringArray(
                    TextUtils.getWords(newStr, new String[]{"."}));
            if (arrayOfString.length == 0) {
                out.println("Usage: SHOW SPACE user.pattern");
                return;
            }
            if (arrayOfString.length > 1) {
                table.setValue("P_OWNER", arrayOfString[0].toUpperCase());
                table.setValue("P_NAME", arrayOfString[1].toUpperCase());
            } else {
                table.setValue("P_NAME", arrayOfString[0].toUpperCase());
            }
            table.setValue("P_LIMIT", String.valueOf(executor.scanLimit));
            DBRowCache rowCache = null;

            try {
                rowCache = executor.executeQuery(executor.database,
                        "SELECT OWNER,SEGMENT_NAME,PARTITION_NAME,SEGMENT_TYPE"
                                + "  FROM DBA_SEGMENTS "
                                + "  WHERE UPPER(OWNER)=NVL(:P_OWNER,USER) AND SEGMENT_NAME LIKE :P_NAME "
                                + "    AND SEGMENT_TYPE IN ('TABLE','INDEX','CLUSTER',"
                                + "          'TABLE PARTITION','INDEX PARTITION',"
                                + "          'TABLE SUBPARTITION','INDEX SUBPARTITION','LOB') "
                                + "  ORDER BY 1,4,2,3", table, 1000);
            } catch (SQLException e) {
                out.print(e);
                return;
            }
            SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
            localSimpleDBRowCache.addColumn("OWNER", 12);
            localSimpleDBRowCache.addColumn("SEG_NAME", 12);
            localSimpleDBRowCache.addColumn("SUB_NAME", 12);
            localSimpleDBRowCache.addColumn("SEG_TYPE", 12);
            localSimpleDBRowCache.addColumn("FLG0", -5);
            localSimpleDBRowCache.addColumn("BLKS", -5);
            localSimpleDBRowCache.addColumn("BYTES", -5);
            localSimpleDBRowCache.addColumn("UBLKS", -5);
            localSimpleDBRowCache.addColumn("UBYTES", -5);
            localSimpleDBRowCache.addColumn("FID", -5);
            localSimpleDBRowCache.addColumn("BID", -5);
            localSimpleDBRowCache.addColumn("OFF", -5);
            VariableTable table2 = new VariableTable();
            table2.add("OWNER", 12);
            table2.add("SEG_NAME", 12);
            table2.add("SUB_NAME", 12);
            table2.add("SEG_TYPE", 12);
            table2.add("FLG0", -5);
            table2.add("BLKS", -5);
            table2.add("BYTES", -5);
            table2.add("UBLKS", -5);
            table2.add("UBYTES", -5);
            table2.add("FID", -5);
            table2.add("BID", -5);
            table2.add("OFF", -5);
            table2.add("P_LIMIT", 4);
            table2.setValue("P_LIMIT", String.valueOf(executor.scanLimit));

            SQLCallable sqlCallable1 = null;
            SQLCallable sqlCallable2 = null;
            try {
                sqlCallable1 = executor.prepareCall(executor.database,
                        "DBMS_SPACE.FREE_BLOCKS(SEGMENT_OWNER => :OWNER,"
                                + "    SEGMENT_NAME => :SEG_NAME,SEGMENT_TYPE => :SEG_TYPE,"
                                + "    PARTITION_NAME => :SUB_NAME,FREELIST_GROUP_ID => 0,"
                                + "    FREE_BLKS => :FLG0 OUT,SCAN_LIMIT=>:P_LIMIT)", table2);
                sqlCallable2 = executor.prepareCall(executor.database,
                        "DBMS_SPACE.UNUSED_SPACE(SEGMENT_OWNER => :OWNER,"
                                + "    SEGMENT_NAME => :SEG_NAME,SEGMENT_TYPE => :SEG_TYPE,"
                                + "    PARTITION_NAME=>:SUB_NAME,TOTAL_BLOCKS => :BLKS OUT,"
                                + "    TOTAL_BYTES => :BYTES OUT,UNUSED_BLOCKS => :UBLKS OUT,"
                                + "    UNUSED_BYTES => :UBYTES OUT,LAST_USED_EXTENT_FILE_ID => :FID OUT,"
                                + "    LAST_USED_EXTENT_BLOCK_ID => :BID OUT,LAST_USED_BLOCK => :OFF OUT)",
                        table2);
            } catch (SQLException e) {
                out.print(e);
                return;
            }
            for (int k = 1; k <= rowCache.getRowCount(); k++) {
                table2.setValue("OWNER", rowCache.getItem(k, 1));
                table2.setValue("SEG_NAME", rowCache.getItem(k, 2));
                table2.setValue("SUB_NAME", rowCache.getItem(k, 3));
                table2.setValue("SEG_TYPE", rowCache.getItem(k, 4));
                try {
                    sqlCallable1.bind(table2);
                    sqlCallable1.stmt.execute();
                    sqlCallable1.fetch(table2);
                    sqlCallable2.bind(table2);
                    sqlCallable2.stmt.execute();
                    sqlCallable2.fetch(table2);
                    int i = localSimpleDBRowCache.appendRow();
                    for (int m = 1; m <= localSimpleDBRowCache.getColumnCount(); m++) {
                        localSimpleDBRowCache.setItem(i, m,
                                table2.getValue(localSimpleDBRowCache.getColumnName(m)));
                    }
                } catch (SQLException e) {
                    out.print(e);
                }
            }
            closeQuietly(sqlCallable1);
            closeQuietly(sqlCallable2);
            out.print(localSimpleDBRowCache);
        }

        String formatStatsValue(long paramLong) {
            if (paramLong > 10000000L) { return paramLong / 1000000L + "M"; }
            if (paramLong > 10000L) { return paramLong / 1000L + "K"; }
            if (paramLong >= 0L) { return String.valueOf(paramLong); }
            return "n/a";
        }

        void showUsage(CommandLog log) {
            log.println("Usage: SHOW keyword argument");
            log.println();
            log.println("Keyword:");
            log.println("  USER SGA SESSION CHILD PARENT CONSTRAINT VERSION SPACE");
            log.println("  TABLE STATS WAIT VARIABLE ERRORS TOPSQL");
            log.println("Argument:");
            log.println("  for CHILD and PARENT, it should be table name,");
            log.println("  for CONSTRAINT, it should be constraint name.");
        }

        final String[] ORACLE_SHOW_KEYS = {"USER", "SGA", "SESSION", "CHILD", "PARENT",
                "CONSTRAINT",
                "CONS", "VERSION", "SPACE", "TABLE", "STATS", "WAIT", "VARIABLE", "ERRORS", "LOAD",
                "TOPSQL"};

        public static final int ORACLE_SHOW_USER             = 0;
        public static final int ORACLE_SHOW_SGA              = 1;
        public static final int ORACLE_SHOW_SESSION          = 2;
        public static final int ORACLE_SHOW_CHILD            = 3;
        public static final int ORACLE_SHOW_PARENT           = 4;
        public static final int ORACLE_SHOW_CONSTRAINT       = 5;
        public static final int ORACLE_SHOW_CONS             = 6;
        public static final int ORACLE_SHOW_VERSION          = 7;
        public static final int ORACLE_SHOW_SPACE            = 8;
        public static final int ORACLE_SHOW_TABLE            = 9;
        public static final int ORACLE_SHOW_STATS            = 10;
        public static final int ORACLE_SHOW_WAIT             = 11;
        public static final int ORACLE_SHOW_VARIABLE         = 12;
        public static final int ORACLE_SHOW_ERRORS           = 13;
        public static final int ORACLE_SHOW_LOAD             = 14;
        public static final int ORACLE_SHOW_TOP_SQL          = 15;
        public static final int ORACLE_SHOW_INDEX            = 16;
        public static final int ORACLE_SHOW_TABLE_CONSTRAINT = 17;
        public static final int ORACLE_SHOW_TABLE_TRIGGER    = 18;
        public static final int ORACLE_SHOW_TABLE_KEYS       = 19;
        public static final int ORACLE_SHOW_TABLE_OTHERS     = 20;

        final Map<Integer, String> SHOW_SQLS = new HashMap<Integer, String>() {{
            put(ORACLE_SHOW_USER, "SELECT 'Current user : ' || USER FROM DUAL");
            put(ORACLE_SHOW_SGA, "SELECT * FROM V$SGA");
            put(ORACLE_SHOW_VERSION, "SELECT * FROM V$VERSION");
            put(ORACLE_SHOW_TABLE_OTHERS, "SELECT /* AnySQL */ "
                    + "  SEARCH_CONDITION "
                    + " FROM ALL_CONSTRAINTS "
                    + "WHERE OWNER = NVL(:P_OWNER,USER) AND CONSTRAINT_NAME = :P_NAME");
            put(ORACLE_SHOW_TABLE_KEYS, "SELECT /* AnySQL */ "
                    + "  POSITION NO#,COLUMN_NAME "
                    + " FROM ALL_CONS_COLUMNS "
                    + "WHERE OWNER = NVL(:P_OWNER,USER) AND CONSTRAINT_NAME = :P_NAME");
            put(ORACLE_SHOW_CONSTRAINT, "SELECT /* AnySQL */ "
                    + "  DECODE(CONSTRAINT_TYPE , "
                    + "\t'P','Primary Key', "
                    + "\t'R','Foreign Key', "
                    + "\t'U','Unique', "
                    + "\t'C','Check','Not Null') TYPE, "
                    + "\tTABLE_NAME,STATUS,VALIDATED "
                    + " FROM ALL_CONSTRAINTS "
                    + "WHERE OWNER = NVL(:P_OWNER,USER) AND CONSTRAINT_NAME = :P_NAME");
            put(ORACLE_SHOW_CHILD, "SELECT /* AnySQL */ T.CONSTRAINT_NAME PKNAME, "
                    + "       R.OWNER||'.'||R.TABLE_NAME TNAME, "
                    + "       R.CONSTRAINT_NAME FKNAME "
                    + "  FROM ALL_CONSTRAINTS R, ALL_CONSTRAINTS T "
                    + "  WHERE T.CONSTRAINT_TYPE IN ('P','U') "
                    + "    AND R.R_OWNER = T.OWNER AND R.R_CONSTRAINT_NAME = T.CONSTRAINT_NAME "
                    + "    AND T.OWNER = NVL(:P_OWNER,USER) AND T.TABLE_NAME = :P_NAME");
            put(ORACLE_SHOW_LOAD, "SELECT /* AnySQL */ /*+ ORDERED USE_NL(S,P) */"
                    + "    S.SADDR,P.SPID,S.SID,S.SERIAL#,S.USERNAME "
                    + "  FROM V$SESSION S,V$PROCESS P "
                    + "  WHERE S.PADDR=P.ADDR AND S.AUDSID = USERENV('SESSIONID')");
            put(ORACLE_SHOW_ERRORS, "SELECT /* AnySQL */ /*+ RULE */  "
                    + "  TO_CHAR(LINE)||'/'||TO_CHAR(POSITION) \"LINE/COL\",  "
                    + "  TEXT \"ERROR\"  "
                    + "FROM ALL_ERRORS A  "
                    + "WHERE A.NAME = UPPER(:P_NAME)  "
                    + "  AND A.OWNER = UPPER(NVL(:P_OWNER,USER))  "
                    + "  AND A.TYPE  = UPPER(:P_TYPE) "
                    + "ORDER BY LINE, POSITION");
            put(ORACLE_SHOW_TABLE_TRIGGER, "SELECT /* AnySQL */ OWNER,TRIGGER_NAME, "
                    + "  TRIGGER_TYPE,TRIGGERING_EVENT EVENT,STATUS "
                    + "  FROM ALL_TRIGGERS "
                    + "  WHERE TABLE_OWNER=NVL(:P_OWNER,USER) AND TABLE_NAME=:P_NAME ");
            put(ORACLE_SHOW_TABLE_CONSTRAINT, "SELECT /* AnySQL */ OWNER,CONSTRAINT_NAME, "
                    + "  DECODE(CONSTRAINT_TYPE , "
                    + "        'P','Primary Key', "
                    + "        'R','Foreign Key', "
                    + "        'U','Unique', "
                    + "        'C','Check','Not Null') TYPE, "
                    + "        STATUS,VALIDATED "
                    + "  FROM ALL_CONSTRAINTS "
                    + "  WHERE OWNER=NVL(:P_OWNER,USER) AND TABLE_NAME=:P_NAME ");
            put(ORACLE_SHOW_INDEX, "SELECT /* AnySQL */ OWNER,INDEX_NAME,INDEX_TYPE,UNIQUENESS, "
                    + "       TABLESPACE_NAME TS_NAME,STATUS,PARTITIONED "
                    + "  FROM ALL_INDEXES "
                    + "  WHERE TABLE_OWNER=NVL(:P_OWNER,USER) AND TABLE_NAME=:P_NAME ");
            put(ORACLE_SHOW_PARENT, "SELECT /* AnySQL */ R.CONSTRAINT_NAME FKNAME, "
                    + "       T.OWNER||'.'||T.TABLE_NAME TNAME, "
                    + "       T.CONSTRAINT_NAME PKNAME "
                    + "  FROM ALL_CONSTRAINTS R, ALL_CONSTRAINTS T "
                    + "  WHERE R.CONSTRAINT_TYPE = 'R' "
                    + "    AND R.R_OWNER = T.OWNER AND R.R_CONSTRAINT_NAME = T.CONSTRAINT_NAME "
                    + "    AND R.OWNER = NVL(:P_OWNER,USER) AND R.TABLE_NAME = :P_NAME");
        }};

    }

    class OraProcessor {

        final String   str;
        final String[] args;

        public OraProcessor(String str, String[] args) {
            this.str  = str;
            this.args = args;
        }

        public boolean proc() {
            if (args.length == 0) {
                oraUsage(out);
                return false;
            }
            if (!executor.isConnected()) {
                out.println("Database not connected!");
                return false;
            }
            String sql = null;
            int    idx = TextUtils.indexOf(ORACLE_ORA_FUNCTIONS, args[0]);
            if (idx == -1) {
                sql = loadSqlScript(args[0], out);
                if (sql == null) {
                    return false;
                }
            } else {
                sql = getORASQL(idx);
            }
            VariableTable table = getVariableTable();
            if (sql != null) {
                if ((idx == ORACLE_ORA_SQL) || (idx == ORACLE_ORA_HASH)) {
                    sqlOrHash(sql, table);
                } else if (idx == ORACLE_ORA_XPLAN) {
                    xplan(sql, table);
                } else {
                    executor.executeSQL(executor.database, new Command(SQL_QUERY, ASQL_END, sql),
                            table, out);
                }
            }
            return true;
        }

        void sqlOrHash(String sql, VariableTable table) {
            try {
                DBRowCache rowCache = executor.executeQuery(executor.database, sql, table, 5000);
                if (rowCache.getRowCount() > 0) {
                    String string = getRowCacheStringBuilder(rowCache);
                    out.println(string.trim());
                } else {
                    out.println("Invalid SQL address specified.");
                }
            } catch (SQLException localSQLException1) {
                out.print(localSQLException1);
            }
        }

        VariableTable getVariableTable() {
            VariableTable table = new VariableTable();
            table.add("V1", 12);
            for (int k = 1; k < args.length; k++) {
                table.add("V" + k, 12);
                table.setValue("V" + k, args[k]);
                Object[] arrayOfObject = TextUtils.getFields(args[k], ".").toArray();
                if (arrayOfObject.length > 1) {
                    table.add("V" + k + "_OWNER", 12);
                    table.setValue("V" + k + "_OWNER", arrayOfObject[0].toString());
                    table.add("V" + k + "_NAME", 12);
                    table.setValue("V" + k + "_NAME", arrayOfObject[1].toString());
                } else {
                    table.add("V" + k + "_OWNER", 12);
                    table.add("V" + k + "_NAME", 12);
                    table.setValue("V" + k + "_NAME", arrayOfObject[0].toString());
                }
            }
            return table;
        }

        void xplan(String sql, VariableTable table) {
            try {
                DBRowCache rowCache = executor.executeQuery(executor.database, sql, table, 5000);
                if (rowCache.getRowCount() > 0) {
                    String string = getRowCacheStringBuilder(rowCache);
                    out.println(string.trim());
                    out.println();
                    rowCache = executor.getExplainPlan(string);
                    if (rowCache != null) { executor.showDBRowCache(rowCache, true); }
                } else {
                    out.println("Invalid SQL address specified.");
                }
            } catch (SQLException localSQLException2) {
                out.print(localSQLException2);
            }
        }


        private String getRowCacheStringBuilder(DBRowCache rowCache) {
            StringBuilder builder = new StringBuilder();
            for (int n = 1; n <= rowCache.getRowCount(); n++) {
                String item = (String) rowCache.getItem(n, 1);
                builder.append(item);
                if (item.getBytes().length >= 64) { continue; }
                builder.append("\n");
            }
            return builder.toString();
        }
    }
}
