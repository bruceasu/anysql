package com.asql.pgsql.invoker;

import static com.asql.pgsql.PgSqlCMDType.*;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.core.log.OutputCommandLog;
import com.asql.core.util.TextUtils;
import com.asql.pgsql.PgSqlSQLExecutor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author suk
 * @date 2017/8/13
 */
public class SingleInvoker implements ModuleInvoker {
    PgSqlSQLExecutor executor;
    CommandLog out;
    CMDType cmdType;
    public static final Map<Integer, String> DESC_SQLS = new HashMap<Integer, String>() {{
        put(0, "SELECT /* AnySQL D0 */  "
                + " USER OWNER,OBJECT_NAME,OBJECT_TYPE FROM USER_OBJECTS "
                + "WHERE OBJECT_NAME=:P_NAME AND "
                + "  OBJECT_TYPE IN ('TABLE','VIEW','PACKAGE','PROCEDURE', "
                + "'FUNCTION','CLUSTER')");

        put(1, "SELECT /* AnySQL D1 */ "
                + " OWNER,OBJECT_NAME,OBJECT_TYPE FROM ALL_OBJECTS "
                + "WHERE UPPER(OWNER) = :P_OWNER AND OBJECT_NAME=:P_NAME AND "
                + "  OBJECT_TYPE IN ('TABLE','VIEW','PACKAGE','PROCEDURE',"
                + "'FUNCTION','CLUSTER')");

        put(2, "SELECT /* AnySQL D2 */ O.OWNER,O.OBJECT_NAME,O.OBJECT_TYPE "
                + " FROM  ALL_OBJECTS O "
                + " WHERE (OWNER,OBJECT_NAME) "
                + "   IN (SELECT TABLE_OWNER,TABLE_NAME FROM USER_SYNONYMS "
                + "       WHERE SYNONYM_NAME=:P_NAME AND "
                + "       DB_LINK IS NULL) "
                + "   AND O.OBJECT_TYPE IN ('TABLE','VIEW','PACKAGE', "
                + "      'PROCEDURE','FUNCTION','CLUSTER')");

        put(3, "SELECT /* AnySQL D3 */ O.OWNER,O.OBJECT_NAME,O.OBJECT_TYPE "
                + " FROM  ALL_OBJECTS O "
                + " WHERE (OWNER,OBJECT_NAME)  "
                + "   IN (SELECT TABLE_OWNER,TABLE_NAME FROM ALL_SYNONYMS "
                + "       WHERE OWNER = :P_OWNER AND SYNONYM_NAME=:P_NAME AND "
                + "       DB_LINK IS NULL) "
                + "   AND O.OBJECT_TYPE IN ('TABLE','VIEW','PACKAGE', "
                + "       'PROCEDURE','FUNCTION','CLUSTER')");

        put(4, "SELECT /* AnySQL D4 */ O.OWNER,O.OBJECT_NAME,O.OBJECT_TYPE "
                + " FROM  ALL_OBJECTS O "
                + " WHERE (OWNER,OBJECT_NAME) "
                + "   IN (SELECT TABLE_OWNER,TABLE_NAME FROM ALL_SYNONYMS "
                + "       WHERE OWNER = 'PUBLIC' AND SYNONYM_NAME=:P_NAME AND "
                + "       DB_LINK IS NULL) "
                + "   AND O.OBJECT_TYPE IN ('TABLE','VIEW','PACKAGE', "
                + "      'PROCEDURE','FUNCTION','CLUSTER')");

        put(5, "select /* AnySQL D5 */ "
                + "  COLUMN_ID NO#,COLUMN_NAME NAME, "
                + "  DECODE(NULLABLE,'N','NOT NULL','') NULLABLE, "
                + "  (case  "
                + "     when data_type='CHAR' then data_type||'('||data_length||')' "
                + "     when data_type='VARCHAR' then data_type||'('||data_length||')' "
                + "     when data_type='VARCHAR2' then data_type||'('||data_length||')' "
                + "     when data_type='NCHAR' then data_type||'('||data_length||')' "
                + "     when data_type='NVARCHAR' then data_type||'('||data_length||')' "
                + "     when data_type='NVARCHAR2' then data_type||'('||data_length||')' "
                + "     when data_type='RAW' then data_type||'('||data_length||')' "
                + "     when data_type='NUMBER' then "
                + "        ( "
                + "           case "
                + "              when data_scale is null and data_precision is null then 'NUMBER' "
                + "              when data_scale <> 0  then 'NUMBER('||NVL(DATA_PRECISION,38)||','||DATA_SCALE||')' "
                + "             else 'NUMBER('||NVL(DATA_PRECISION,38)||')' "
                + "           end "
                + "        ) "
                + "     else"
                + "        ( case "
                + "            when data_type_owner is not null then data_type_owner||'.'||data_type "
                + "             else data_type "
                + "          end ) "
                + "   end) TYPE --,(case when default_length>0 then 'Default' else null end) \"Default\"  "
                + "   from all_tab_columns "
                + "   where UPPER(owner)=:P_OWNER AND TABLE_NAME=:P_NAME "
                + "   order by 1");

        put(6, "SELECT /* AnySQL D6 */ "
                + "   OBJECT_NAME||'('||NVL(OVERLOAD,-1)||')' NAME "
                + "   ,RPAD(' ',2*NVL(DATA_LEVEL,0),' ')||NVL(NVL(ARGUMENT_NAME,DATA_TYPE),'@return') ARG,IN_OUT, "
                + "  (case "
                + "     when pls_type is not null then pls_type "
                + "     when type_subname is not null then decode(package_name,type_name,'','.'||type_name)||type_subname "
                + "      else data_type "
                + "   end) TYPE --,(case when default_length>0 then 'Default' else null end) \"Default\" "
                + " FROM ALL_ARGUMENTS "
                + " WHERE UPPER(OWNER)=:P_OWNER AND PACKAGE_NAME=:P_NAME "
                + "  ORDER BY 1,OVERLOAD,SEQUENCE,POSITION");

        put(7, "SELECT /* AnySQL D7 */ "
                + "   RPAD('  ',2*NVL(DATA_LEVEL,0),' ')||NVL(ARGUMENT_NAME,'@return') NAME,IN_OUT, "
                + "  (case "
                + "     when pls_type is not null then pls_type "
                + "     when type_subname is not null then type_name||'.'||type_subname "
                + "      else data_type "
                + "   end) TYPE --,(case when default_length>0 then 'Default' else null end) \"Default\""
                + " FROM ALL_ARGUMENTS "
                + " WHERE PACKAGE_NAME IS NULL AND UPPER(OWNER)=:P_OWNER "
                + "   AND OBJECT_NAME=:P_NAME ORDER BY POSITION");

        put(8, "SELECT  /* AnySQL */  /*+ RULE */ "
                + "    DECODE(C.COLUMN_POSITION,1,I.INDEX_TYPE,'') TYPE, "
                + "    DECODE(C.COLUMN_POSITION,1,I.UNIQUENESS,'') ISUNQ, "
                + "    DECODE(C.COLUMN_POSITION,1,C.INDEX_NAME,'') INDEX_NAME, "
                + "    C.COLUMN_POSITION NO#,C.COLUMN_NAME, C.DESCEND  "
                + "  FROM ALL_IND_COLUMNS C, ALL_INDEXES I "
                + "  WHERE C.INDEX_OWNER=I.OWNER AND C.INDEX_NAME=I.INDEX_NAME "
                + "    AND I.TABLE_NAME=:P_NAME AND I.TABLE_OWNER=:P_OWNER "
                + "  ORDER BY C.INDEX_NAME,C.COLUMN_POSITION "
                + "");

        put(9, "SELECT  /* AnySQL */  /*+ RULE */ "
                + "    PARTITIONED,AVG_ROW_LEN,NUM_ROWS,BLOCKS,EMPTY_BLOCKS "
                + "  FROM DBA_TABLES T  "
                + "  WHERE T.TABLE_NAME=:P_NAME AND UPPER(T.OWNER)=:P_OWNER");
    }};

    public SingleInvoker(PgSqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        int m = executor.getSingleID(cmd.command);
        switch (m) {
            case PGSQL_CONNECT:
            case PGSQL_CONN:
                procConnect(cmd.command);
                break;
            case PGSQL_DISCONNECT:
                executor.procDisconnect(cmd.command);
                break;
            case PGSQL_DEBUGLEVEL:
                procDebugLevel(cmd.command);
                break;
            case PGSQL_SCANLIMIT:
                procScanLimit(cmd.command);
                break;
            case PGSQL_TUNESORT:
                procTunesort(cmd.command);
                break;
            case PGSQL_ECHO:
                procEcho(cmd.command);
                break;
            case PGSQL_PAGESIZE:
                procPageSize(cmd.command);
                break;
            case PGSQL_HEADING:
                procHeading(cmd.command);
                break;
            case PGSQL_TERMOUT:
                procTermout(cmd.command);
                break;
            case PGSQL_DELIMITER:
                procDelimiter(cmd.command);
                break;
            case PGSQL_RECORD:
                procSetRecord(cmd.command);
                break;

            case PGSQL_VAR:
                procVariable(cmd.command);
                break;
            case PGSQL_UNVAR:
                procUnvariable(cmd.command);
                break;
            case PGSQL_PRINT:
                procPrint(cmd.command);
                break;
            case PGSQL_AUTOTRACE:
            case PGSQL_AUTOT:
                procAutotrace(cmd.command);
                break;
            case PGSQL_TIMING:
                procTiming(cmd.command);
                break;
            case PGSQL_AUTOCOMMIT:
                procAutoCommit(cmd.command);
                break;
            case PGSQL_QUERYONLY:
                procQueryOnly(cmd.command);
                break;
            case PGSQL_FETCHSIZE:
                procFetchSize(cmd.command);
                break;
            case PGSQL_DEFINE:
                procDefine(cmd.command);
                break;
            case PGSQL_SQLSET:
                procSQLSet(cmd.command);
                break;
            case PGSQL_SPOOLAPPEND:
                procSpoolAppend(cmd.command);
                break;
            case PGSQL_SPOOL:
                procSpool(cmd.command);
                break;
            case PGSQL_READ:
                procRead(cmd.command);
                break;
            case PGSQL_HELP:
                procHelp(cmd.command);
                break;
            case PGSQL_SPOOLOFF:
                procSpoolOff(cmd.command);
                break;
            case PGSQL_LCOMMAND:
                procListCommand(cmd.command);
                break;
            case PGSQL_BUFFER_ADD:
                procBUFFERADD(cmd.command);
                break;
            case PGSQL_BUFFER_LIST:
                procBUFFERLIST(cmd.command);
                break;
            case PGSQL_BUFFER_RESET:
                procBUFFERRESET(cmd.command);
                break;
            case PGSQL_ENCODING:
                procEncoding(cmd.command);
                break;
            case PGSQL_LOCALE:
                procLocale(cmd.command);
                break;
            case PGSQL_READONLY:
                procReadonly(cmd.command);
                break;

        }

        return true;
    }

    void procAutotrace(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[12]).size();
        String str = executor.skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        if (arrayOfString.length == 0) {
            out.println("Usage: SET AUTOTRACE {ON | OFF | TRACE} [STATISTICS]");
            return;
        }
        if ((!"ON".equalsIgnoreCase(arrayOfString[0])) && (!"OFF".equalsIgnoreCase(arrayOfString[0])) && (!"TRACE".equalsIgnoreCase(arrayOfString[0]))) {
            out.println("Usage: SET AUTOTRACE {ON | OFF | TRACE} [STATISTICS]");
            return;
        }
        executor.autotraceType = arrayOfString[0];
        if (arrayOfString.length > 1)
            executor.autotraceName = arrayOfString[1];
        else
            executor.autotraceName = "ALL";
        out.setAutoTrace(executor.autotraceType.equalsIgnoreCase("TRACE"));
        out.println("Autotrace set to : " + str);
    }

    void procDebugLevel(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[1]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        int j = executor.getInt(str, 0);
        executor.setDebugLevel(j);
        out.println("Debug level set to : " + executor.getDebugLevel());
    }

    void procConnect(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[PGSQL_CONNECT]).size();
        String connstr = executor.skipWord(paramString, i);
        String host = "";
        String password = "";
        String user = "";
        connstr = connstr.trim();
        Properties dbProp = DBConnection.getProperties("PGSQL");

        int j = connstr.indexOf("@");
        if (j == -1) {
            user = connstr;
            host = "";
        } else {
            user = connstr.substring(0, j);
            host = connstr.substring(j + 1);
        }

        j = user.indexOf("/");
        if (j == -1) {
            try {
                password = executor.in.readPassword();
            } catch (Exception localException) {
            }
        } else {
            password = user.substring(j + 1);
            user = user.substring(0, j);
        }

        if (executor.getDebugLevel() > 0) {
            out.println("==================== Name ====================");
            out.println(user + "@" + host);
            out.println("==================================================");
            out.println();
        }
        try {


            executor.database = DBConnection.getConnection("PGSQL", host, user, password, dbProp);
            executor.database.setAutoCommit(false);
            executor.autoCommit = executor.database.getAutoCommit();
            out.println("database connected.");
        } catch (SQLException localSQLException2) {
            out.print(localSQLException2);
        }
        try {
            prepareDescSQL(executor.isConnected());
        } catch (SQLException localSQLException3) {
            out.print(localSQLException3);
        }

    }

    void prepareDescSQL(boolean initial)
            throws SQLException {
        if (initial) {
            if (executor.descSqls[0] == null)
                executor.descSqls[0] = executor.prepareStatement(executor.database, DESC_SQLS.get(0), executor.sysVariable);
            if (executor.descSqls[1] == null)
                executor.descSqls[1] = executor.prepareStatement(executor.database,  DESC_SQLS.get(1), executor.sysVariable);
            if (executor.descSqls[2] == null)
                executor.descSqls[2] = executor.prepareStatement(executor.database,  DESC_SQLS.get(2), executor.sysVariable);
            if (executor.descSqls[3] == null)
                executor.descSqls[3] = executor.prepareStatement(executor.database,  DESC_SQLS.get(3), executor.sysVariable);
            if (executor.descSqls[4] == null)
                executor.descSqls[4] = executor.prepareStatement(executor.database,  DESC_SQLS.get(4), executor.sysVariable);
            if (executor.descSqls[5] == null)
                executor.descSqls[5] = executor.prepareStatement(executor.database,  DESC_SQLS.get(5), executor.sysVariable);
            if (executor.descSqls[6] == null)
                executor.descSqls[6] = executor.prepareStatement(executor.database,  DESC_SQLS.get(6), executor.sysVariable);
            if (executor.descSqls[7] == null)
                executor.descSqls[7] = executor.prepareStatement(executor.database,  DESC_SQLS.get(7), executor.sysVariable);
            if (executor.descSqls[8] == null)
                executor.descSqls[8] = executor.prepareStatement(executor.database,  DESC_SQLS.get(8), executor.sysVariable);
            if (executor.descSqls[9] == null)
                executor.descSqls[9] = executor.prepareStatement(executor.database,  DESC_SQLS.get(9), executor.sysVariable);
        } else {
            for (int i = 0; i < executor.descSqls.length; i++) {
                if (executor.descSqls[i] == null)
                    continue;
                executor.descSqls[i].close();
            }
            executor.descSqls[0] = null;
            executor.descSqls[1] = null;
            executor.descSqls[2] = null;
            executor.descSqls[3] = null;
            executor.descSqls[4] = null;
            executor.descSqls[5] = null;
            executor.descSqls[6] = null;
            executor.descSqls[7] = null;
            executor.descSqls[8] = null;
            executor.descSqls[9] = null;
        }
    }

    void procScanLimit(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[30]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        int j = executor.getInt(str, 0);
        if (j > 0) {
            if (j < 128)
                j = 128;
            executor.scanLimit = j;
        }
        out.println("Scan Limit set to : " + executor.scanLimit);
    }

    void procTunesort(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[31]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        int j = executor.getInt(str, 10);
        if (j < 1)
            j = 1;
        if (j > 500)
            j = 500;
        if (!executor.isConnected()) {
            out.println("Not connected to executor.database!");
            return;
        }
        try {
            SQLStatement localSQLStatement = null;
            localSQLStatement = executor.prepareStatement(executor.database, "ALTER SESSION SET SORT_AREA_SIZE=" + j * 1048576, executor.sysVariable);
            localSQLStatement.stmt.execute();
            localSQLStatement.close();
            localSQLStatement = executor.prepareStatement(executor.database, "ALTER SESSION SET SORT_AREA_RETAINED_SIZE=" + j * 1048576, executor.sysVariable);
            localSQLStatement.stmt.execute();
            localSQLStatement.close();
            localSQLStatement = executor.prepareStatement(executor.database, "ALTER SESSION SET SORT_MULTIBLOCK_READ_COUNT=128", executor.sysVariable);
            localSQLStatement.stmt.execute();
            localSQLStatement.close();
            out.println("Command completed.");
        } catch (SQLException localSQLException) {
            out.print(localSQLException);
        }
    }

    void procPageSize(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[2]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        int j = executor.getInt(str, 14);
        out.setPagesize(j);
        out.println("Page size set to : " + out.getPagesize());
    }

    void procFetchSize(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[16]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        int j = executor.getInt(str, 200);
        executor.setFetchSize(j);
    }

    void procTiming(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[13]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        executor.timing = "ON".equalsIgnoreCase(str);
        out.println("Timing set to : " + (executor.timing ? "ON" : "OFF"));
    }

    void procSpoolAppend(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[22]).size();
        String str1 = executor.skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = executor.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(str2, true);
            if (out.getLogFile() != null)
                out.getLogFile().close();
            out.setLogFile(new OutputCommandLog(executor, localFileOutputStream));
        } catch (IOException localIOException) {
            out.print(localIOException);
        }
    }

    void procSpool(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[24]).size();
        String str1 = executor.skipWord(paramString, i);
        if (str1.trim().length() == 0) {
            out.println("Usage: SPOOL [APPEND] file");
            return;
        }
        String str2 = executor.sysVariable.parseString(str1.trim());
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(str2);
            if (out.getLogFile() != null)
                out.getLogFile().close();
            out.setLogFile(new OutputCommandLog(executor, localFileOutputStream));
        } catch (IOException localIOException) {
            out.print(localIOException);
        }
    }

    void procRead(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[25]).size();
        String str1 = executor.skipWord(paramString, i);
        str1 = str1.trim();
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str1));
        if (arrayOfString.length < 2) {
            out.println("Usage: READ varname filename");
            return;
        }
        String str2 = arrayOfString[0];
        String str3 = str1.substring(str2.length()).trim();
        if (!executor.sysVariable.exists(str2)) {
            out.println("Variable " + str2 + " not defined!");
            return;
        }
        try {
            FileReader localFileReader = new FileReader(executor.sysVariable.parseString(str3));
            char[] arrayOfChar = new char[65536];
            int j = localFileReader.read(arrayOfChar);
            try {
                executor.sysVariable.setValue(str2, String.valueOf(arrayOfChar, 0, j));
            } catch (Exception localException) {
                out.print(localException);
            }
            localFileReader.close();
        } catch (IOException localIOException) {
            out.print(localIOException);
        }
    }

    void procHelp(String paramString) {
        out.println();
        out.println("Usage: HELP");
        out.println();
        out.println(" CONNECT SHOW SET DESCRIBE SOURCE DISCONNECT LOADTNS LIST");
        out.println(" DEPEND VAR UNVAR PRINT DEFINE SQLSET SPOOL START READ ");
        out.println(" @ @@ LOB LOBEXP LOBIMP LOBLEN UNLOAD");
        out.println();
    }

    void procListCommand(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[27]).size();
        String str = executor.skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        int j = 1;
        int k = 0;
        if (arrayOfString.length > 0) {
            j = executor.getInt(arrayOfString[0], 1);
            if (arrayOfString.length > 1)
                k = executor.getInt(arrayOfString[1], 0);
        }
        if ((executor.lastcommand == null) || (executor.lastcommand.command == null)) {
            out.println("No command in buffer.");
        } else {
            Vector localVector = TextUtils.getLines(executor.lastcommand.command);
            if ((j > localVector.size()) || (j < 1) || (k < 0)) {
                out.println("Invalid line number.");
                return;
            }
            out.println();
            for (int m = j - 1; (m < localVector.size()) && ((k == 0) || (m < j - 1 + k)); m++) {
                out.print(executor.rpad(m + 1 + " ", 5));
                out.println((String) localVector.elementAt(m));
            }
            out.println();
        }
    }


    void procSpoolOff(String paramString) {
        if (out.getLogFile() != null)
            out.getLogFile().close();
        out.setLogFile(null);
    }

    void procDefine(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[18]).size();
        String str1 = executor.skipWord(paramString, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            out.println("Usage:");
            out.println("  DEFINE variable=value");
            out.println("  SET    PAGESIZE   pagesize");
            out.println("  SET    DEBUGLEVEL debuglevel");
            out.println("  SET    AUTOTRACE  {OFF|ON|TRACE} [EXP|EVENT|STATS]");
            out.println("  SET    HEADING    {OFF|ON}");
            out.println("  SET    DELIMITER  delimiter");
            out.println("  SET    TIMING     {ON|OFF}");
            out.println("  SET    LOCALE     locale");
            out.println("  SET    READONLY   {TRUE|FALSE}");
            out.println("LOCALE: ENGLISH FRENCH GERMAN ITALIAN JAPANESE KOREAN CHINESE");
            out.println("        SMPLIFIED_CHINESE TRADITIONAL_CHINESE FRANCE GERMANY");
            out.println("        ITALY JAPAN KOREA CHINA PRC TAIWAN UK US CANADA");
            return;
        }
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.trim().length() == 0) {
            out.println("Usage:");
            out.println("  DEFINE variable=value");
            out.println("  SET    PAGESIZE   pagesize");
            out.println("  SET    DEBUGLEVEL debuglevel");
            out.println("  SET    AUTOTRACE  {OFF|ON|TRACE} [EXP|EVENT|STATS]");
            out.println("  SET    HEADING    {OFF|ON}");
            out.println("  SET    DELIMITER  delimiter");
            out.println("  SET    TIMING     {ON|OFF}");
            out.println("  SET    LOCALE     locale");
            out.println("  SET    READONLY   {TRUE|FALSE}");
            out.println("LOCALE: ENGLISH FRENCH GERMAN ITALIAN JAPANESE KOREAN CHINESE");
            out.println("        SMPLIFIED_CHINESE TRADITIONAL_CHINESE FRANCE GERMANY");
            out.println("        ITALY JAPAN KOREA CHINA PRC TAIWAN UK US CANADA");
            return;
        }
        if (!executor.sysVariable.exists(str2.trim()))
            executor.sysVariable.add(str2.trim(), 12);
        try {
            if (str3.length() == 0)
                executor.sysVariable.setValue(str2, null);
            else
                executor.sysVariable.setValue(str2, str3);
        } catch (Exception localException) {
            out.print("Invalid format : ");
            out.println(str3);
        }
    }

    void procSQLSet(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[18]).size();
        String str1 = executor.skipWord(paramString, i);
        int j = str1.indexOf("=");
        if (j == -1) {
            out.println("Usage: SQLSET variable=query");
            return;
        }
        String str2 = str1.substring(0, j);
        String str3 = str1.substring(j + 1);
        if (str2.trim().length() == 0) {
            out.println("Usage: SQLSET variable=query");
            return;
        }
        if (!executor.sysVariable.exists(str2.trim())) {
            out.println("Variable " + str2 + " not declared.");
            return;
        }
        if (executor.checkNotConnected()) return;
        try {
            DBRowCache localDBRowCache = executor.executeQuery(executor.database, str3, executor.sysVariable, 1000);
            if ((localDBRowCache.getRowCount() == 1) && (localDBRowCache.getColumnCount() == 1))
                try {
                    executor.sysVariable.setValue(str2, localDBRowCache.getItem(1, 1));
                } catch (Exception localException) {
                    out.print("Invalid format : ");
                    out.println(str3);
                }
            else
                out.println("Query does not return one row and one column.");
        } catch (SQLException localSQLException) {
            out.print(localSQLException);
        }
    }

    void procQueryOnly(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[36]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        if (str.length() == 0) {
            if (cmdType != null)
                out.println("QUERYONLY : " + (cmdType.getQueryOnly() ? "TRUE" : "FALSE"));
            return;
        }
        executor.autoCommit = "TRUE".equalsIgnoreCase(str);
        if (cmdType != null)
            cmdType.setQueryOnly(executor.autoCommit);
    }

    void procAutoCommit(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[15]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        if (executor.checkNotConnected()) return;
        if (str.length() == 0) {
            try {
                executor.autoCommit = executor.database.getAutoCommit();
                out.println("executor.autoCommit : " + (executor.autoCommit ? "ON" : "OFF"));
            } catch (SQLException localSQLException1) {
                out.print(localSQLException1);
            }
            return;
        }
        executor.autoCommit = "ON".equalsIgnoreCase(str);
        try {
            executor.database.setAutoCommit(executor.autoCommit);
            executor.autoCommit = executor.database.getAutoCommit();
        } catch (SQLException localSQLException2) {
            out.print(localSQLException2);
        }
    }

    void procTermout(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[3]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        boolean bool = "ON".equalsIgnoreCase(str);
        out.setTermOut(bool);
        executor.setTermOut(bool);
    }

    void procLocale(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[29]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        if (str.length() > 0)
            DBConnection.setLocale(str);
    }

    void procEncoding(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[32]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        if (str.length() > 0)
            DBConnection.setEncoding(str);
    }

    void procReadonly(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[3]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        if (executor.isConnected())
            try {
                executor.database.setReadOnly("TRUE".equalsIgnoreCase(str));
            } catch (SQLException localSQLException) {
                out.print(localSQLException);
            }
    }

    void procHeading(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[3]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        boolean bool = "ON".equalsIgnoreCase(str);
        out.setHeading(bool);
    }

    void procEcho(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[3]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        boolean bool = "ON".equalsIgnoreCase(str);
        executor.setEcho(bool);
    }

    void procDelimiter(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[4]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        if ("TAB".equalsIgnoreCase(str))
            str = "\t";
        out.setSeperator(str.length() == 0 ? " " : str);
        if (str.length() == 0)
            out.println("Delimiter set to : SPACE");
        else if (str.equals("\t"))
            out.println("Delimiter set to : TAB");
        else
            out.println("Delimiter set to : " + str);
    }

    void procSetRecord(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[5]).size();
        String str = executor.skipWord(paramString, i);
        str = str.trim();
        out.setRecord(str);
    }




    void procVariable(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(cmdType.getASQLSingle()[9]).size();
        String str2 = executor.skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length < 2) {
            out.println("Usage:");
            out.println("  VAR varname vartype");
            out.println("");
            out.println("  CHAR VARCHAR LONGVARCHAR BINARY VARBINARY LONGVARBINARY");
            out.println("  NUMERIC DECIMAL BIT TINYINT SMALLINT INTEGER BIGINT REAL");
            out.println("  FLOAT DOUBLE DATE TIME TIMESTAMP BLOB CLOB");
            return;
        }
        if (executor.sysVariable.exists(arrayOfString[0])) {
            out.println("Variable " + arrayOfString[0] + " already defined.");
            return;
        }
        executor.sysVariable.add(arrayOfString[0], SQLTypes.getTypeID(arrayOfString[1]));
    }

    void procUnvariable(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(cmdType.getASQLSingle()[10]).size();
        String str2 = executor.skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str2));
        if (arrayOfString.length == 0) {
            out.println("Usage: UNVAR varname");
            return;
        }
        if (!executor.sysVariable.exists(arrayOfString[0])) {
            out.println("Variable " + arrayOfString[0] + " not exist.");
            return;
        }
        executor.sysVariable.remove(arrayOfString[0]);
    }

    void procPrint(String paramString) {
        String str1 = "%";
        int i = TextUtils.getWords(cmdType.getASQLSingle()[11]).size();
        String str2 = executor.skipWord(paramString, i);
        out.println(executor.sysVariable.parseString(str2));
    }




    void procBUFFERADD(String paramString) {
        int i = TextUtils.getWords(cmdType.getASQLSingle()[33]).size();
        String str = executor.skipWord(paramString, i);
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        for (int j = 0; j < arrayOfString.length / 2; j++)
            executor.loadBuffer.addColumn(arrayOfString[(j * 2)], SQLTypes.getTypeID(arrayOfString[(j * 2 + 1)]));
        out.println("Command completed.");
    }

    void procBUFFERLIST(String paramString) {
        out.println("Structure of load buffer:");
        for (int i = 1; i <= executor.loadBuffer.getColumnCount(); i++)
            out.println("  " + executor.lpad(executor.loadBuffer.getColumnName(i), 20) + "    " + SQLTypes.getTypeName(executor.loadBuffer.getColumnType(i)));
    }

    void procBUFFERRESET(String paramString) {
        executor.loadBuffer.deleteAllRow();
        executor.loadBuffer.removeAllColumn();
        out.println("Command completed.");
    }

    void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Throwable ignore) {
        }
    }
}
