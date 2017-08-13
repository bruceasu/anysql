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

import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVM;
import java.io.FileReader;
import java.io.IOException;

public class OracleORAFunction {
    public static final String[] ORACLE_ORA_FUNCTIONS = {"PARAMETER", "INITORA", "SPID", "2PC",
            "ACTIVE", "SQL", "SORT", "SESSION", "BGSESS", "DEAD", "TSFREE", "FILEFREE", "SEGMENT",
            "EXTENT", "NOINDEX", "INDEX", "RBS", "LOCKWAIT", "TSBH", "OBJBH", "USERBH", "RECOVER",
            "TRAN", "BUSY", "CHAIN", "PGA", "LIBCACHE", "ROWCACHE", "SYSSTAT", "WAITSTAT",
            "SYSEVENT", "TSSTAT", "FILESTAT", "SESSTAT", "SEGSTAT", "BLOCK", "DDLLOCK", "BACKUP",
            "LOGHIS", "TOPEXE", "TOPGETS", "TOPREAD", "TOPGET", "WAIT", "SGASTAT", "LATCH", "LOCK",
            "OBJECT", "LONGOPS", "TABLESPACE", "DATAFILE", "ARCLOG", "LATCHWAIT", "CURSOR",
            "GLOBAL", "MOVEIND", "MOVETAB", "SHARE", "UNDO", "UNDOHDR", "_PARAMETER", "CHARSET",
            "SQLMEM", "HASH", "HOLD", "ACTIVESQL", "BLOCKING", "PXSTAT", "PX", "PXSESS",
            "PXPROCESS", "PQSLAVE", "XPLAN", "SQLLIKE", "WSQL", "PLAN", "FREESPACE", "MACHINE",
            "KILLMACHINE", "KILLUSER", "HOT", "KILLSQL", "KILLHOLD", "TSTAT", "ISTAT", "SIZE",
            "UNUSABLE", "INVALID", "FILEOBJ", "RESIZE", "NOLOGGING", "OBJSQL", "SPTOPSQL", "SPSQL",
            "OBJGRANT", "PLAN+", "SPSTAT", "SPEVENT"};

    public static final int ORACLE_ORA_PARAMETER = 0;
    public static final int ORACLE_ORA_INITORA = 1;
    public static final int ORACLE_ORA_SPID = 2;
    public static final int ORACLE_ORA_2PC = 3;
    public static final int ORACLE_ORA_ACTIVE = 4;
    public static final int ORACLE_ORA_SQL = 5;
    public static final int ORACLE_ORA_SORT = 6;
    public static final int ORACLE_ORA_SESSION = 7;
    public static final int ORACLE_ORA_BGSESSION = 8;
    public static final int ORACLE_ORA_DEAD = 9;
    public static final int ORACLE_ORA_TSFREE = 10;
    public static final int ORACLE_ORA_FILEFREE = 11;
    public static final int ORACLE_ORA_SEGMENT = 12;
    public static final int ORACLE_ORA_EXTENT = 13;
    public static final int ORACLE_ORA_NOINDEX = 14;
    public static final int ORACLE_ORA_INDEX = 15;
    public static final int ORACLE_ORA_RBS = 16;
    public static final int ORACLE_ORA_LOCKWAIT = 17;
    public static final int ORACLE_ORA_TSBH = 18;
    public static final int ORACLE_ORA_OBJBH = 19;
    public static final int ORACLE_ORA_USERBH = 20;
    public static final int ORACLE_ORA_RECOVER = 21;
    public static final int ORACLE_ORA_TRANSACTION = 22;
    public static final int ORACLE_ORA_BUSY = 23;
    public static final int ORACLE_ORA_CHAIN = 24;
    public static final int ORACLE_ORA_PGA = 25;
    public static final int ORACLE_ORA_LIBCACHE = 26;
    public static final int ORACLE_ORA_ROWCACHE = 27;
    public static final int ORACLE_ORA_SYSSTAT = 28;
    public static final int ORACLE_ORA_WAITSTAT = 29;
    public static final int ORACLE_ORA_SYSEVENT = 30;
    public static final int ORACLE_ORA_TSSTAT = 31;
    public static final int ORACLE_ORA_FILESTAT = 32;
    public static final int ORACLE_ORA_SESSTAT = 33;
    public static final int ORACLE_ORA_SEGSTAT = 34;
    public static final int ORACLE_ORA_BLOCK = 35;
    public static final int ORACLE_ORA_DDLLOCK = 36;
    public static final int ORACLE_ORA_BACKUP = 37;
    public static final int ORACLE_ORA_LOGHIS = 38;
    public static final int ORACLE_ORA_TOPEXE = 39;
    public static final int ORACLE_ORA_TOPGETS = 40;
    public static final int ORACLE_ORA_TOPREAD = 41;
    public static final int ORACLE_ORA_TOPGET = 42;
    public static final int ORACLE_ORA_WAIT = 43;
    public static final int ORACLE_ORA_SGASTAT = 44;
    public static final int ORACLE_ORA_LOCK = 45;
    public static final int ORACLE_ORA_OBJECT = 46;
    public static final int ORACLE_ORA_GLOBAL = 47;
    public static final int ORACLE_ORA_MOVEIND = 48;
    public static final int ORACLE_ORA_MOVETAB = 49;
    public static final int ORACLE_ORA_SHARE = 50;
    public static final int ORACLE_ORA_UNDO = 51;
    public static final int ORACLE_ORA_UNDOHDR = 52;
    public static final int ORACLE_ORA__PARAMETER = 53;
    public static final int ORACLE_ORA_CHARSET = 54;
    public static final int ORACLE_ORA_SQLMEM = 55;
    public static final int ORACLE_ORA_HASH = 63;
    public static final int ORACLE_ORA_XPLAN = 72;

    public static final String[] COMMANDSQL = {
            "SELECT /* AnySQL */ NAME,ISDEFAULT,ISSES_MODIFIABLE SESMOD, ISSYS_MODIFIABLE SYSMOD,VALUE  "
                    + "  FROM V$PARAMETER  "
                    + "  WHERE NAME LIKE '%' || LOWER(:V1) || '%' AND NAME <> 'control_files' and name <> 'rollback_segments'",
            "SELECT /* AnySQL */ NAME,ISSES_MODIFIABLE SESMOD, ISSYS_MODIFIABLE SYSMOD,VALUE  "
                    + "  FROM V$PARAMETER  "
                    + "  WHERE ISDEFAULT='FALSE' AND NAME LIKE '%' || LOWER(:V1) || '%'  "
                    + "    AND NAME <> 'control_files' and name <> 'rollback_segments'",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    S.SID,S.SERIAL#,P.SPID,S.USERNAME,A.NAME JOB,  "
                    + "    S.MACHINE,S.PROGRAM PROGRAM,S.STATUS,S.SQL_HASH_VALUE HASH_VALUE   "
                    + "  FROM V$PROCESS P, V$SESSION S,AUDIT_ACTIONS A    "
                    + "  WHERE P.ADDR = S.PADDR AND S.COMMAND = A.ACTION  "
                    + "    AND P.SPID = TO_NUMBER(:V1)",
            "SELECT /* AnySQL */ P.LOCAL_TRAN_ID LOCAL_ID,DECODE(N.IN_OUT,'in',N.DBUSER_OWNER, "
                    + "  N.DBUSER_OWNER||'@'||N.DATABASE) USERNAME,  "
                    + "  P.STATE,P.MIXED,DECODE(P.ADVICE,'C','Commit','R','Rollback',null) ADVICE, "
                    + "  HOST,FAIL_TIME,TRAN_COMMENT \"COMMENT\"  "
                    + " FROM DBA_2PC_PENDING P,DBA_2PC_NEIGHBORS N  "
                    + " WHERE P.LOCAL_TRAN_ID = N.LOCAL_TRAN_ID",
            "SELECT /* AnySQL leading(s) first_rows */  "
                    + "    S.SID,S.SERIAL#,P.PID,P.SPID,NVL(S.USERNAME,SUBSTR(P.PROGRAM, LENGTH(P.PROGRAM) - 6)) USERNAME,  "
                    + "    S.MACHINE,w.event,w.p1,w.p2,w.p3,w.wait_time wt, "
                    + "    SQL_HASH_VALUE SQL_HASH,s.PREV_HASH_VALUE PREV_HASH   "
                    + "  FROM V$PROCESS P, V$SESSION S, V$SESSION_WAIT W   "
                    + "  WHERE P.ADDR = S.PADDR AND S.SID=W.SID    "
                    + "    AND S.STATUS='ACTIVE'  "
                    + "    AND (S.USERNAME LIKE '%'||:V1||'%'  "
                    + "    OR S.MACHINE LIKE '%'||:V1||'%' OR S.PROGRAM LIKE '%'||:V1||'%')",
            "SELECT /* AnySQL */ SQL_TEXT \"SQL Executing\" FROM V$SQLTEXT_WITH_NEWLINES  "
                    + " WHERE (ADDRESS = (SELECT SQL_ADDRESS FROM V$SESSION WHERE TO_CHAR(SID)=:V1)  "
                    + "        OR ADDRESS = HEXTORAW(:V1))  "
                    + " ORDER BY PIECE",
            "SELECT /* AnySQL */ /*+ ordered */   "
                    + "  B.SID,B.SERIAL#,P.SPID,B.USERNAME,B.MACHINE,A.BLOCKS,A.TABLESPACE, "
                    + "  A.SEGTYPE,A.SEGFILE# FILE#,A.SEGBLK# BLOCK# "
                    + "  FROM V$SORT_USAGE A,V$SESSION B,V$PROCESS P  "
                    + "  WHERE A.SESSION_ADDR = B.SADDR AND B.PADDR = P.ADDR",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    S.SID,S.SERIAL#,P.SPID,S.USERNAME, S.MACHINE,S.STATUS,  "
                    + "    S.PROGRAM PROGRAM,S.SQL_HASH_VALUE HASH_VALUE,s.PREV_HASH_VALUE PREV_HASH  "
                    + "  FROM V$PROCESS P, V$SESSION S    "
                    + "  WHERE P.ADDR = S.PADDR      "
                    + "    AND S.USERNAME IS NOT NULL AND ( TO_CHAR(S.SID)=:V1  "
                    + "    OR S.USERNAME LIKE '%'||:V1||'%'  "
                    + "    OR S.MACHINE LIKE '%'||:V1||'%' OR S.PROGRAM LIKE '%'||:V1||'%')", "select /* AnySQL */ /*+ RULE */    "
                    + "   PID,SPID,USERNAME,PROGRAM||' ('||BP.NAME||')' PROGRAM  "
                    + " from v$process p,v$bgprocess bp    "
                    + " where p.addr = bp.paddr",
            "SELECT /* AnySQL */  "
                    + "   P.PID,P.SPID,P.USERNAME,P.TERMINAL,   "
                    + "   P.PROGRAM,P.BACKGROUND   "
                    + "  FROM V$PROCESS  P,   "
                    + "       (SELECT PADDR FROM V$SESSION WHERE STATUS <> 'KILLED') S  "
                    + "  WHERE P.ADDR = S.PADDR(+) AND S.PADDR IS NULL AND P.PID <> 1  "
                    + "        AND P.PROGRAM NOT LIKE '%(P%)%'",
            "SELECT /* AnySQL */ DF.TABLESPACE_NAME TABLESPACE,   "
                    + "  COUNT(*) FILES, ROUND(SUM(DF.BYTES)/1048576) SIZE_MB,    "
                    + "  ROUND(SUM(FREE.BYTES)/1048576,2) FREE_MB,  "
                    + "  ROUND(SUM(DF.BYTES)/1048576 - SUM(FREE.BYTES)/1048576,2)  USED_MB,  "
                    + "  ROUND(MAX(FREE.MAXBYTES)/1048576,2)  MAXFREE,  "
                    + "  100 - ROUND(100.0 * SUM(FREE.BYTES)/SUM(DF.BYTES) ,2) PCT_USED,    "
                    + "  ROUND(100.0 * SUM(FREE.BYTES)/SUM(DF.BYTES) ,2) PCT_FREE  "
                    + "FROM (SELECT * FROM DBA_DATA_FILES WHERE TABLESPACE_NAME LIKE UPPER(:V1)||'%') DF,  "
                    + "     (SELECT TABLESPACE_NAME,FILE_ID,SUM(BYTES)  BYTES,  "
                    + "      MAX(BYTES) MAXBYTES FROM DBA_FREE_SPACE WHERE TABLESPACE_NAME LIKE UPPER(:V1)||'%' "
                    + "      GROUP BY TABLESPACE_NAME,FILE_ID) FREE  "
                    + "WHERE DF.TABLESPACE_NAME=FREE.TABLESPACE_NAME(+) AND DF.FILE_ID = FREE.FILE_ID(+)  "
                    + "GROUP BY DF.TABLESPACE_NAME",
            "SELECT /* AnySQL */  "
                    + "  DF.FILE_ID FILE#,DF.FILE_NAME NAME,  "
                    + "  DF.SIZE_MB, NVL(FREE.MAXFREE,0) MAX_FREE,  "
                    + "  ROUND(NVL(FREE.FREE_MB,0),2) FREE_MB,  "
                    + "  100 - ROUND(100.0 * NVL(FREE.FREE_MB,0)/DF.SIZE_MB,2) PCT_USED,  "
                    + "  ROUND(100.0 * NVL(FREE.FREE_MB,0)/DF.SIZE_MB,2) PCT_FREE  "
                    + "FROM (SELECT  "
                    + "        FILE_ID,FILE_NAME,TABLESPACE_NAME,BYTES/1048576 SIZE_MB   "
                    + "        FROM DBA_DATA_FILES WHERE TABLESPACE_NAME=UPPER(:V1)) DF,  "
                    + "    (SELECT FILE_ID,SUM(BYTES)/1048576 FREE_MB,TRUNC(MAX(BYTES/1024/1024),2) MAXFREE  "
                    + "        FROM DBA_FREE_SPACE WHERE TABLESPACE_NAME=UPPER(:V1)  "
                    + "        GROUP BY FILE_ID) FREE  "
                    + "WHERE DF.FILE_ID = FREE.FILE_ID(+)",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "   SEGMENT_TYPE,OWNER SEGMENT_OWNER,SEGMENT_NAME,   "
                    + "        TRUNC(SUM(BYTES)/1024/1024,1) SIZE_MB  "
                    + "   FROM DBA_SEGMENTS WHERE OWNER NOT IN ('SYS','SYSTEM')  "
                    + "   GROUP BY SEGMENT_TYPE,OWNER,SEGMENT_NAME  "
                    + "   HAVING SUM(BYTES) > TO_NUMBER(NVL(:V1,'100')) * 1048576   "
                    + "   ORDER BY 1,2,3,4 DESC",
            "SELECT /* AnySQL */ /*+ RULE */    "
                    + "   E.OWNER,E.SEGMENT_NAME,E.SEGMENT_TYPE,  "
                    + "   E.PARTITION_NAME PARTITION,E.TABLESPACE_NAME TABLESPACE,     "
                    + "   E.EXTENTS,E.MAX_EXTENTS,E.NEXT_EXTENT,T.MAXFREE   "
                    + " FROM DBA_SEGMENTS E,   "
                    + "      (SELECT TABLESPACE_NAME,MAX(BYTES) MAXFREE FROM DBA_FREE_SPACE   "
                    + "        GROUP BY TABLESPACE_NAME) T   "
                    + " WHERE E.TABLESPACE_NAME = T.TABLESPACE_NAME AND E.MAX_EXTENTS > 0   "
                    + "   AND (E.MAX_EXTENTS <= E.EXTENTS + 5 OR    "
                    + "      NVL(E.NEXT_EXTENT,E.INITIAL_EXTENT) > T.MAXFREE)",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "   OWNER,SEGMENT_NAME,PARTITION_NAME,SEGMENT_TYPE,   "
                    + "   TABLESPACE_NAME,TRUNC(BYTES/1024/1024,1) SIZE_MB   "
                    + " FROM DBA_SEGMENTS T  "
                    + " WHERE NOT EXISTS (  "
                    + "    SELECT 'X' FROM DBA_INDEXES I  "
                    + "    WHERE T.OWNER = I.TABLE_OWNER AND T.SEGMENT_NAME = I.TABLE_NAME)  "
                    + "    AND T.BYTES > TO_NUMBER(NVL(:V1,'100')) * 1048576  "
                    + "    AND T.SEGMENT_TYPE IN ('TABLE','TABLE PARTITION')  "
                    + "    AND T.OWNER NOT IN ('SYS','SYSTEM')  "
                    + " ORDER BY 6 DESC",
            "SELECT /* AnySQL */ /*+ ORDERED USE_NL(T2 I) */  "
                    + "       I.TABLE_OWNER||'.'||I.TABLE_NAME TABLE_NAME,TRUNC(T2.BYTES/1024/1024,1) TSIZE_MB,   "
                    + "       DECODE(I.OWNER,I.TABLE_OWNER,'',I.OWNER||'.')||I.INDEX_NAME INDEX_NAME,  "
                    + "       TRUNC(T1.BYTES/1024/1024,1) ISIZE_MB,ROUND(100 * T1.BYTES/T2.BYTES,2) IND2TAB  "
                    + "FROM  "
                    + "     (SELECT OWNER,SEGMENT_NAME,SUM(BYTES) BYTES   "
                    + "          FROM DBA_SEGMENTS WHERE SEGMENT_TYPE IN ('TABLE','TABLE PARTITION')  "
                    + "               AND OWNER NOT IN ('SYS','SYSTEM') AND BYTES >= 20971520 GROUP BY OWNER,SEGMENT_NAME   "
                    + "          HAVING SUM(BYTES) >= TO_NUMBER(NVL(:V1,'100')) * 1048576) T2,  "
                    + "     DBA_INDEXES I,  "
                    + "     (SELECT OWNER,SEGMENT_NAME,SUM(BYTES) BYTES   "
                    + "          FROM DBA_SEGMENTS WHERE SEGMENT_TYPE IN ('INDEX','INDEX PARTITION')  "
                    + "               AND OWNER NOT IN ('SYS','SYSTEM') AND BYTES > 10485760 GROUP BY OWNER,SEGMENT_NAME  "
                    + "          HAVING  SUM(BYTES) >= TO_NUMBER(NVL(:V1,'100')) * 1048576/20 ) T1   "
                    + "WHERE I.INDEX_NAME=T1.SEGMENT_NAME AND I.OWNER=T1.OWNER AND  "
                    + "      I.TABLE_NAME=T2.SEGMENT_NAME AND I.TABLE_OWNER=T2.OWNER AND  "
                    + "      I.TABLE_OWNER NOT IN ('SYS','SYSTEM') AND  "
                    + "      I.INDEX_TYPE NOT IN ('LOB','IOT - TOP','CLUSTER') AND  "
                    + "      ROUND(100 * T1.BYTES/T2.BYTES,2) > 5.0   "
                    + "ORDER BY 5 DESC",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "  RS.USN,NAME,RSSIZE/1024 SIZE_KB,WRITES,  "
                    + "  GETS,WAITS,XACTS,OPTSIZE,HWMSIZE/1024 HWM_KB,STATUS  "
                    + "  FROM V$ROLLSTAT RS,V$ROLLNAME RN  "
                    + "  WHERE RS.USN = RN.USN AND NAME LIKE '%'||:V1||'%'",
            "SELECT /* AnySQL */ /*+ ORDERED USE_HASH(H,R) */  "
                    + "   H.SID HOLD_SID,  "
                    + "   R.SID WAIT_SID,  "
                    + "   decode(H.type,  "
                    + "           'MR', 'Media Recovery',  "
                    + "           'RT', 'Redo Thread',  "
                    + "           'UN', 'User Name',  "
                    + "           'TX', 'Transaction',  "
                    + "           'TM', 'DML',  "
                    + "           'UL', 'PL/SQL User Lock',  "
                    + "           'DX', 'Distributed Xaction',  "
                    + "           'CF', 'Control File',  "
                    + "           'IS', 'Instance State',  "
                    + "           'FS', 'File Set',  "
                    + "           'IR', 'Instance Recovery',  "
                    + "           'ST', 'Disk Space Transaction',  "
                    + "           'TS', 'Temp Segment',  "
                    + "           'IV', 'Library Cache Invalidation',  "
                    + "           'LS', 'Log Start or Switch',  "
                    + "           'RW', 'Row Wait',  "
                    + "           'SQ', 'Sequence Number',  "
                    + "           'TE', 'Extend Table',  "
                    + "           'TT', 'Temp Table',  "
                    + "           'TC', 'Thread Checkpoint',  "
                    + "            'SS', 'Sort Segment',  "
                    + "            'JQ', 'Job Queue',  "
                    + "            'PI', 'Parallel operation',  "
                    + "            'PS', 'Parallel operation',  "
                    + "            'DL', 'Direct Index Creation',  "
                    + "           H.type) type,  "
                    + "   decode(H.lmode,  "
                    + "           0, 'None',         1, 'Null',  "
                    + "           2, 'Row-S (SS)',   3, 'Row-X (SX)',  "
                    + "           4, 'Share',        5, 'S/Row-X (SSX)',  "
                    + "           6, 'Exclusive',    to_char(H.lmode)) hold,  "
                    + "    decode(r.request,         0, 'None',  "
                    + "           1, 'Null',         2, 'Row-S (SS)',  "
                    + "           3, 'Row-X (SX)',   4, 'Share',  "
                    + "           5, 'S/Row-X (SSX)',6, 'Exclusive',  "
                    + "           to_char(R.request)) request,  "
                    + "   R.ID1,R.ID2,R.CTIME  "
                    + " FROM V$LOCK H,V$LOCK R  "
                    + " WHERE H.BLOCK = 1 AND R.REQUEST > 0 AND H.SID <> R.SID  "
                    + "   and H.TYPE <> 'MR' AND R.TYPE <> 'MR'  "
                    + "   AND H.ID1 = R.ID1 AND H.ID2 = R.ID2 AND H.TYPE=R.TYPE  "
                    + "   AND H.LMODE > 0 AND R.REQUEST > 0 ORDER BY 1,2", "select /* AnySQL */  "
            + "   ts.name TABLESPACE,count(*) TOTAL,    "
            + "   SUM(decode(b.status,'xcur',1,0)) XCUR,  "
            + "   SUM(decode(b.status,'scur',1,0)) SCUR,  "
            + "   SUM(decode(b.status,'cr',1,0))   CR,  "
            + "   SUM(decode(b.status,'read',1,0)) READ,  "
            + "   SUM(decode(b.status,'mrec',1,0)) MREC,  "
            + "   SUM(decode(b.status,'irec',1,0)) IREC,  "
            + "   round(100.0*ratio_to_report(count(*)) over (),2) bufpct   "
            + " from v$bh b, v$tablespace ts    "
            + " where b.ts# = ts.ts# and b.file# <> 0 group by ts.name", "select /* AnySQL */ * FROM (SELECT    "
            + "   o.owner,o.object_type type,o.object_name,count(*) TOTAL,    "
            + "   SUM(decode(b.status,'xcur',1,0)) XCUR,    "
            + "   SUM(decode(b.status,'scur',1,0)) SCUR,    "
            + "   SUM(decode(b.status,'cr',1,0))   CR,    "
            + "   SUM(decode(b.status,'read',1,0)) READ,    "
            + "   SUM(decode(b.status,'mrec',1,0)) MREC,    "
            + "   SUM(decode(b.status,'irec',1,0)) IREC,    "
            + "   round(100.0*ratio_to_report(count(*)) over (),4) bufpct    "
            + " from v$bh b, dba_objects o   "
            + " where b.objd = o.data_object_id and b.objd > 0 and o.owner not in ('SYS','SYSTEM')   "
            + " group by o.owner,o.object_type,o.object_name   "
            + " order by 4 desc ) WHERE BUFPCT >= TO_NUMBER(NVL(:V1,'0.5'))",
            "SELECT /* AnySQL */   "
                    + "   o.owner,count(*) TOTAL,    "
                    + "   SUM(decode(b.status,'xcur',1,0)) XCUR,    "
                    + "   SUM(decode(b.status,'scur',1,0)) SCUR,    "
                    + "   SUM(decode(b.status,'cr',1,0))   CR,    "
                    + "   SUM(decode(b.status,'read',1,0)) READ,    "
                    + "   SUM(decode(b.status,'mrec',1,0)) MREC,    "
                    + "   SUM(decode(b.status,'irec',1,0)) IREC,    "
                    + "   round(100.0*ratio_to_report(count(*)) over (),4) bufpct    "
                    + " from v$bh b, (select distinct data_object_id,owner  "
                    + "       from dba_objects WHERE data_object_id is not null) o   "
                    + " where b.objd = o.data_object_id and b.objd > 0    "
                    + " group by o.owner  "
                    + " order by 9 desc",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "    R.FILE#,F.FILE_NAME NAME,F.TABLESPACE_NAME TS,   "
                    + "    R.\"ONLINE\" STATUS,R.ERROR,R.CHANGE#,R.TIME   "
                    + "  FROM V$RECOVER_FILE R,DBA_DATA_FILES F   "
                    + "  WHERE R.FILE#=F.FILE_ID",
            "SELECT /* AnySQL */ /* RULE */    "
                    + "    S.SID,S.SERIAL#,P.SPID,S.USERNAME, R.NAME RBS,  "
                    + "    T.START_TIME TRAN_START_TIME,   "
                    + "    to_char(T.USED_UBLK)||','||to_char(T.USED_UREC) BLKS_RECS , "
                    + "    T.LOG_IO LOGIO,T.PHY_IO PHYIO,T.CR_GET CRGET,T.CR_CHANGE CRMOD  "
                    + "  FROM V$TRANSACTION T, V$SESSION S,V$ROLLNAME R,  "
                    + "       V$ROLLSTAT RS,V$PROCESS P   "
                    + "  WHERE T.SES_ADDR(+) = S.SADDR    "
                    + "    AND T.XIDUSN = R.USN AND S.USERNAME IS NOT NULL   "
                    + "    AND R.USN = RS.USN  AND S.PADDR = P.ADDR",
            "SELECT /* AnySQL */  "
                    + "    S.SID,S.SERIAL#,P.SPID,S.SQL_HASH_VALUE HASH_VALUE,  "
                    + "    W.EVENT, W.P1 FILE_ID, W.P2 BLOCK#,  "
                    + "    W.SECONDS_IN_WAIT SECONDS,W.STATE   "
                    + "  FROM    "
                    + "    V$SESSION_WAIT W,V$SESSION S,V$PROCESS P  "
                    + "  WHERE S.PADDR = P.ADDR AND W.EVENT IN    "
                    + "       (SELECT NAME FROM V$EVENT_NAME  "
                    + "        WHERE PARAMETER1='file#' AND PARAMETER2='block#'  "
                    + "              AND NAME not like 'control%')   "
                    + "    AND W.SID=NVL(TO_NUMBER(:V1),W.SID) AND W.SID=S.SID ",
            "SELECT /* AnySQL */ /* RULE ORDERED */    "
                    + "    S.SID,S.SERIAL#,P.SPID,S.USERNAME,S.PROGRAM,S.MACHINE,  "
                    + "    W.SECONDS_IN_WAIT SECONDS,W.STATE,S.SQL_HASH_VALUE,Q.SQL_TEXT    "
                    + "  FROM     "
                    + "    V$SESSION_WAIT W,V$LATCH L,V$SESSION S,V$SQL Q,V$PROCESS P  "
                    + "  WHERE W.EVENT = 'latch free'  AND L.ADDR=HEXTORAW(W.P1) AND   "
                    + "    L.NAME IN ('cache buffers chains','cache buffers lru chain')  "
                    + "    AND S.SQL_ADDRESS = Q.ADDRESS(+) AND W.SID=S.SID AND S.PADDR = P.ADDR  "
                    + "    AND S.SID=NVL(TO_NUMBER(:V1),S.SID)",
            "SELECT /* AnySQL */ * FROM V$PGASTAT",
            "SELECT /* AnySQL */ /* RULE */  "
                    + "  NAMESPACE,GETS,GETHITS,ROUND(100.0*GETHITRATIO,2) GETRATIO,  "
                    + "  PINS,PINHITS,ROUND(100.0*PINHITRATIO,2) PINRATIO,  "
                    + "  RELOADS,INVALIDATIONS  "
                    + "FROM V$librarycache",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "  PARAMETER,GETS,GETMISSES,  "
                    + "  ROUND(100.0-100.0*GETMISSES/GETS,2) HITRATIO,  "
                    + "  SCANS,SCANMISSES,MODIFICATIONS,FLUSHES  "
                    + "FROM V$ROWCACHE  "
                    + " WHERE SUBORDINATE# IS NULL AND GETS > 0",
            "SELECT /* AnySQL */ STATISTIC#,NAME,VALUE FROM V$SYSSTAT  "
                    + "  WHERE VALUE > 0 AND NAME LIKE '%' || :V1 || '%'  "
                    + "",
            "SELECT /* AnySQL */ * FROM V$WAITSTAT",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "  EVENT,TOTAL_WAITS WAITS,TOTAL_TIMEOUTS TIMEOUTS,   "
                    + "  TIME_WAITED,ROUND(AVERAGE_WAIT,2) AVERAGE_WAIT   "
                    + "FROM V$SYSTEM_EVENT    "
                    + "WHERE EVENT NOT LIKE 'rdbms%' AND EVENT NOT LIKE '%time%'   "
                    + "  AND EVENT NOT LIKE '%message%' AND EVENT NOT LIKE '%slave%'   "
                    + "ORDER BY TIME_WAITED DESC,TOTAL_WAITS DESC",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    TS.NAME,COUNT(*) FCOUNT,SUM(FS.PHYRDS) PHYRDS,SUM(FS.PHYWRTS) PHYWRTS,  "
                    + "    SUM(FS.PHYBLKRD) PHYBLKRD,SUM(FS.PHYBLKWRT) PHYBLKWRT,  "
                    + "    TRUNC(AVG(FS.READTIM),4) READTIM,TRUNC(AVG(FS.WRITETIM),4) WRITETIM  "
                    + "  FROM V$FILESTAT FS,V$DATAFILE DF,V$TABLESPACE TS   "
                    + "  WHERE FS.FILE# = DF.FILE# AND DF.TS# = TS.TS#  "
                    + "  GROUP BY TS.NAME  "
                    + "UNION ALL  "
                    + "SELECT /*+ RULE */   "
                    + "    TS.NAME,COUNT(*) FCOUNT,SUM(FS.PHYRDS) PHYRDS,SUM(FS.PHYWRTS) PHYWRTS,  "
                    + "    SUM(FS.PHYBLKRD) PHYBLKRD,SUM(FS.PHYBLKWRT) PHYBLKWRT,  "
                    + "    TRUNC(AVG(FS.READTIM),4) READTIM,TRUNC(AVG(FS.WRITETIM),4) WRITETIM  "
                    + " FROM V$TEMPSTAT FS,V$TEMPFILE DF,V$TABLESPACE TS   "
                    + " WHERE FS.FILE# = DF.FILE# AND DF.TS# = TS.TS#  "
                    + " GROUP BY TS.NAME",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    DF.FILE_NAME,FS.PHYRDS,FS.PHYWRTS,FS.PHYBLKRD,  "
                    + "    FS.PHYBLKWRT,FS.READTIM,FS.WRITETIM,FS.AVGIOTIM  "
                    + "  FROM V$FILESTAT FS,DBA_DATA_FILES DF  "
                    + "  WHERE FS.FILE# = DF.FILE_ID AND DF.TABLESPACE_NAME = UPPER(:V1)  "
                    + "UNION ALL  "
                    + "SELECT /*+ RULE */  "
                    + "    DF.FILE_NAME,FS.PHYRDS,FS.PHYWRTS,FS.PHYBLKRD,  "
                    + "    FS.PHYBLKWRT,FS.READTIM,FS.WRITETIM,FS.AVGIOTIM  "
                    + " FROM V$TEMPSTAT FS,DBA_TEMP_FILES DF  "
                    + " WHERE FS.FILE# = DF.FILE_ID AND DF.TABLESPACE_NAME = UPPER(:V1) ",
            "SELECT /* AnySQL */   "
                    + "  ST.STATISTIC#,SN.NAME,ST.VALUE    "
                    + "FROM  V$SESSTAT ST,V$STATNAME SN    "
                    + "WHERE ST.STATISTIC# = SN.STATISTIC# AND ST.VALUE > 0  "
                    + "  AND ST.SID = TO_NUMBER(:V1)    "
                    + "  AND SN.NAME LIKE '%' || :V2 || '%'  "
                    + "ORDER BY 1",
            "SELECT /* AnySQL */ /*+ RULE */ O.OWNER,O.OBJECT_TYPE,O.OBJECT_NAME,S.*   "
                    + "FROM DBA_OBJECTS O, (SELECT OBJ#,   "
                    + "  SUM(DECODE(STATISTIC#,0,VALUE,0)) READ,   "
                    + "  SUM(DECODE(STATISTIC#,1,VALUE,0)) BUSY,   "
                    + "  SUM(DECODE(STATISTIC#,2,VALUE,0)) CHANGE,   "
                    + "  SUM(DECODE(STATISTIC#,3,VALUE,0)) PHYRD,   "
                    + "  SUM(DECODE(STATISTIC#,4,VALUE,0)) PHYWR,   "
                    + "  SUM(DECODE(STATISTIC#,5,VALUE,0)) PHYDRD,   "
                    + "  SUM(DECODE(STATISTIC#,6,VALUE,0)) PHYDWR,   "
                    + "  SUM(DECODE(STATISTIC#,8,VALUE,0)) CRBLKS,   "
                    + "  SUM(DECODE(STATISTIC#,9,VALUE,0)) CUBLKS,   "
                    + "  SUM(DECODE(STATISTIC#,10,VALUE,0)) ITLWAIT,   "
                    + "  SUM(DECODE(STATISTIC#,11,VALUE,0)) LCKWAIT   "
                    + "  FROM V$SEGSTAT WHERE VALUE > 0 GROUP BY OBJ# ) S    "
                    + "WHERE O.OBJECT_ID = S.OBJ# AND O.OWNER NOT IN ('SYS','SYSTEM')  "
                    + "  AND O.OWNER=:V1 ORDER BY 9 DESC",
            "SELECT /* AnySQL */ /*+ RULE */ OWNER,SEGMENT_NAME NAME,  "
                    + "   PARTITION_NAME PARTITION,SEGMENT_TYPE TYPE,TABLESPACE_NAME TABLESPACE  "
                    + "  FROM DBA_EXTENTS  "
                    + "  WHERE FILE_ID = TO_NUMBER(:V1)  "
                    + "    AND TO_NUMBER(:V2) BETWEEN BLOCK_ID AND BLOCK_ID + BLOCKS - 1", "select /* AnySQL */ /*+ rule */   "
            + " w1.sid||','||W1.serial#  WAITSESS,  "
            + " h1.sid||','||h1.serial#  HOLDSESS,  "
            + " h1.machine,h1.sql_hash_value HOLDSQL,  "
            + " w.kgllktype STATUS,  "
            + " w.kgllkhdl  address,  "
            + " decode(h.kgllkmod,  0, 'None', 1, 'Null', 2, 'Share',   "
            + "    3, 'Exclusive','Unknown') HOLDMODE,  "
            + " decode(w.kgllkreq,  0, 'None', 1, 'Null', 2, 'Share',  "
            + "    3, 'Exclusive','Unknown') WAITMODE  "
            + "  from dba_kgllock w,dba_kgllock h,   "
            + "       v$session w1,v$session h1  "
            + " where  "
            + "  (((h.kgllkmod != 0) and (h.kgllkmod != 1)  "
            + "     and ((h.kgllkreq = 0) or (h.kgllkreq = 1)))  "
            + "   and  "
            + "     (((w.kgllkmod = 0) or (w.kgllkmod= 1))  "
            + "     and ((w.kgllkreq != 0) and (w.kgllkreq != 1))))  "
            + "  and  w.kgllktype  =  h.kgllktype  "
            + "  and  w.kgllkhdl  =  h.kgllkhdl  "
            + "  and  w.kgllkuse     =   w1.saddr  "
            + "  and  h.kgllkuse     =   h1.saddr",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "    DF.TABLESPACE_NAME TABLESPACE,   "
                    + "    DF.FILE_NAME,B.CHANGE#,B.TIME   "
                    + " FROM DBA_DATA_FILES DF, V$BACKUP B   "
                    + " WHERE DF.FILE_ID = B.FILE#   "
                    + "   AND B.STATUS = 'ACTIVE'",
            "SELECT /* AnySQL */  "
                    + "  TO_CHAR(FIRST_TIME,'YYYY-MM-DD') DAY,  "
                    + "  COUNT(*) LOG,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'00',1,0)) H00,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'01',1,0)) H01,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'02',1,0)) H02,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'03',1,0)) H03,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'04',1,0)) H04,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'05',1,0)) H05,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'06',1,0)) H06,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'07',1,0)) H07,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'08',1,0)) H08,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'09',1,0)) H09,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'10',1,0)) H10,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'11',1,0)) H11,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'12',1,0)) H12,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'13',1,0)) H13,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'14',1,0)) H14,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'15',1,0)) H15,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'16',1,0)) H16,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'17',1,0)) H17,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'18',1,0)) H18,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'19',1,0)) H19,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'20',1,0)) H20,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'21',1,0)) H21,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'22',1,0)) H22,  "
                    + "  SUM(DECODE(TO_CHAR(FIRST_TIME,'HH24'),'23',1,0)) H23  "
                    + "FROM V$LOG_HISTORY WHERE FIRST_TIME > TRUNC(SYSDATE-30)  "
                    + "GROUP BY ROLLUP(TO_CHAR(FIRST_TIME,'YYYY-MM-DD'))",
            "SELECT /* AnySQL */ * FROM  "
                    + "(  "
                    + "  SELECT EXECUTIONS,BUFFER_GETS,DISK_READS,  "
                    + "     TRUNC(BUFFER_GETS/EXECUTIONS) GETS1EXEC,ADDRESS,HASH_VALUE  "
                    + "   FROM V$SQLAREA WHERE EXECUTIONS > 0  "
                    + "   ORDER BY EXECUTIONS DESC  "
                    + ") A  "
                    + "WHERE ROWNUM < NVL(TO_NUMBER(:V1),20)+1",
            "SELECT /* AnySQL */ * FROM  "
                    + "(  "
                    + "  SELECT EXECUTIONS,BUFFER_GETS,DISK_READS,  "
                    + "     TRUNC(BUFFER_GETS/EXECUTIONS) GETS1EXEC,ADDRESS,HASH_VALUE  "
                    + "   FROM V$SQLAREA WHERE EXECUTIONS > 0  "
                    + "   ORDER BY BUFFER_GETS DESC  "
                    + ") A  "
                    + "WHERE ROWNUM < NVL(TO_NUMBER(:V1),20)+1",
            "SELECT /* AnySQL */ * FROM  "
                    + "(  "
                    + "  SELECT EXECUTIONS,BUFFER_GETS,DISK_READS,  "
                    + "     TRUNC(BUFFER_GETS/EXECUTIONS) GETS1EXEC,ADDRESS,HASH_VALUE  "
                    + "   FROM V$SQLAREA WHERE EXECUTIONS > 0  "
                    + "   ORDER BY DISK_READS DESC  "
                    + ") A  "
                    + "WHERE ROWNUM < NVL(TO_NUMBER(:V1),20)+1",
            "SELECT /* AnySQL */ * FROM  "
                    + "(  "
                    + "  SELECT EXECUTIONS,BUFFER_GETS,DISK_READS,  "
                    + "     TRUNC(BUFFER_GETS/EXECUTIONS) GETS1EXEC,ADDRESS,HASH_VALUE  "
                    + "   FROM V$SQLAREA WHERE EXECUTIONS > 0  "
                    + "   ORDER BY 4 DESC  "
                    + ") A  "
                    + "WHERE ROWNUM < NVL(TO_NUMBER(:V1),20)+1",
            "SELECT /* AnySQL */ SID,EVENT,WAIT_TIME,SECONDS_IN_WAIT SECONDS,STATE FROM V$SESSION_WAIT  "
                    + "WHERE SID=NVL(TO_NUMBER(:V1),SID) AND EVENT NOT LIKE 'SQL*Net%'  "
                    + "      AND EVENT NOT LIKE 'rdbms%' AND EVENT NOT LIKE '%time%'  "
                    + "      AND EVENT NOT LIKE '%message%' AND EVENT NOT LIKE '%slave%'",
            "SELECT /* AnySQL */ * FROM V$SGASTAT",
            "SELECT /* AnySQL */ /*+ RULE */ W.SID,S.SERIAL#,L.NAME LATCH_NAME,S.USERNAME,  "
                    + "  S.MACHINE,W.WAIT_TIME,W.SECONDS_IN_WAIT SECONDS,S.SQL_HASH_VALUE HASH_VALUE,  "
                    + "  DECODE(S.ROW_WAIT_OBJ#,-1,null,S.ROW_WAIT_OBJ#||'.'||S.ROW_WAIT_FILE#||'.'||  "
                    + "  S.ROW_WAIT_BLOCK#||'.'||S.ROW_WAIT_ROW#) ROW_ID  "
                    + "FROM V$SESSION_WAIT W,V$SESSION S,V$LATCHNAME L  "
                    + "WHERE W.SID=NVL(TO_NUMBER(:V1),W.SID) AND S.SID = W.SID  "
                    + "  AND W.EVENT = 'latch free' AND W.P2 = L.LATCH#  "
                    + "", "select /* AnySQL */ /*+ RULE */  "
            + "   sid, "
            + "   decode(type,  "
            + "           'MR', 'Media Recovery',  "
            + "           'RT', 'Redo Thread',  "
            + "           'UN', 'User Name',  "
            + "           'TX', 'Transaction',  "
            + "           'TM', 'DML',  "
            + "           'UL', 'PL/SQL User Lock',  "
            + "           'DX', 'Distributed Xaction',  "
            + "           'CF', 'Control File',  "
            + "           'IS', 'Instance State',  "
            + "           'FS', 'File Set',  "
            + "           'IR', 'Instance Recovery',  "
            + "           'ST', 'Disk Space Transaction',  "
            + "           'TS', 'Temp Segment',  "
            + "           'IV', 'Library Cache Invalidation',  "
            + "           'LS', 'Log Start or Switch',  "
            + "           'RW', 'Row Wait',  "
            + "           'SQ', 'Sequence Number',  "
            + "           'TE', 'Extend Table',  "
            + "           'TT', 'Temp Table',  "
            + "           'TC', 'Thread Checkpoint',  "
            + "            'SS', 'Sort Segment',  "
            + "            'JQ', 'Job Queue',  "
            + "            'PI', 'Parallel operation',  "
            + "            'PS', 'Parallel operation',  "
            + "            'DL', 'Direct Index Creation',  "
            + "           type) type,  "
            + "   decode(lmode,  "
            + "           0, 'None',             "
            + "           1, 'Null',             "
            + "           2, 'Row-S (SS)',       "
            + "           3, 'Row-X (SX)',       "
            + "           4, 'Share',            "
            + "           5, 'S/Row-X (SSX)',    "
            + "           6, 'Exclusive',        "
            + "           to_char(lmode)) hold,  "
            + "    decode(request,  "
            + "           0, 'None',             "
            + "           1, 'Null',             "
            + "           2, 'Row-S (SS)',       "
            + "           3, 'Row-X (SX)',       "
            + "           4, 'Share',            "
            + "           5, 'S/Row-X (SSX)',    "
            + "           6, 'Exclusive',        "
            + "           to_char(request)) request,  "
            + "    ID1,ID2,CTIME,  "
            + "    decode(block,  "
            + "           0, 'Not Blocking',   "
            + "           1, 'Blocking',       "
            + "           2, 'Global',         "
            + "           to_char(block)) block_others  "
            + "    from v$lock  "
            + "   where type <> 'MR' and to_char(sid) = nvl(:V1,to_char(sid)) ",
            "SELECT /* AnySQL */ OWNER,OBJECT_NAME,SUBOBJECT_NAME,OBJECT_TYPE, "
                    + "   TO_CHAR(CREATED,'YYYY/MM/DD') CREATED,  "
                    + "   TO_CHAR(LAST_DDL_TIME,'YYYY/MM/DD') LASTDDL,STATUS  "
                    + " FROM ALL_OBJECTS WHERE OBJECT_ID = TO_NUMBER(:V1)",
            "SELECT /* AnySQL */  "
                    + "   sid,message, "
                    + "   round(elapsed_seconds/60) Elapsed, "
                    + "   round(time_remaining/60) Remain, SQL_ADDRESS  "
                    + "from v$session_longops "
                    + "where time_remaining>0", "select /* AnySQL */ "
            + "   TABLESPACE_NAME TS_NAME,INITIAL_EXTENT INI_EXT,NEXT_EXTENT NXT_EXT, "
            + "   MIN_EXTENTS MINEXTS,MAX_EXTENTS MAXEXTS,PCT_INCREASE PCT,STATUS,CONTENTS, "
            + "   EXTENT_MANAGEMENT EXT_MGR,ALLOCATION_TYPE ALLOC_TYPE  "
            + "FROM DBA_TABLESPACES "
            + "", "select /* AnySQL */ "
            + "   F.NAME,TO_CHAR(CREATION_TIME,'YYYY/MM/DD') CREA_DATE, "
            + "   CREATION_CHANGE# CREA_SCN,CHECKPOINT_CHANGE# CKPT_SCN, "
            + "   UNRECOVERABLE_CHANGE# RECO_SCN,STATUS, "
            + "   ENABLED,TRUNC(BYTES/1048576,2) SIZE_MB  "
            + "FROM V$DATAFILE F,V$TABLESPACE T "
            + "WHERE F.TS#=T.TS# AND T.NAME = NVL(UPPER(:V1),'SYSTEM')",
            "SELECT /* AnySQL */ "
                    + "   NAME,SEQUENCE#,THREAD#,ARCHIVED ARC, "
                    + "   FIRST_CHANGE# SCN1,NEXT_CHANGE# SCN2 "
                    + "FROM V$ARCHIVED_LOG  "
                    + "WHERE TO_NUMBER(:V1) < FIRST_CHANGE# OR TO_NUMBER(:V1) < NEXT_CHANGE# "
                    + "ORDER BY 2",
            "SELECT /* AnySQL */ "
                    + "  L.SID HOLD_SID,S.SID WAIT_SID,S.SERIAL#,P.SPID, "
                    + "  L.NAME LATCH_NAME  "
                    + " FROM V$LATCHHOLDER L,V$PROCESS P,V$SESSION S  "
                    + " WHERE L.PID = P.PID AND (L.LADDR = HEXTORAW(P.LATCHWAIT)  "
                    + "   OR L.LADDR = HEXTORAW(P.LATCHSPIN))  "
                    + "   AND P.ADDR = S.PADDR AND P.LATCHWAIT IS NOT NULL",
            "SELECT /* AnySQL */ ADDRESS,SQL_TEXT FROM V$OPEN_CURSOR  "
                    + "  WHERE SID = TO_NUMBER(:V1)",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "   T.OWNER,T.TABLE_NAME,   "
                    + "   DECODE(I.OWNER,T.OWNER,NULL,I.OWNER||'.')||I.INDEX_NAME INDEX_NAME,   "
                    + "   I.INDEX_TYPE,I.UNIQUENESS,I.TABLESPACE_NAME TS_INDEX   "
                    + "  FROM ALL_TABLES T, ALL_INDEXES I   "
                    + "  WHERE (:V1 IS NULL OR T.OWNER=UPPER(:V1)) AND T.PARTITIONED='YES'   "
                    + "    AND I.TABLE_OWNER = T.OWNER    "
                    + "    AND I.TABLE_NAME = T.TABLE_NAME   "
                    + "    AND I.PARTITIONED = 'NO' ORDER BY 1,2",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "  'ALTER '||   "
                    + "  DECODE(I.INDEX_TYPE,  "
                    + "    'IOT - TOP','TABLE ',  "
                    + "    'INDEX ') ||  "
                    + "  DECODE(I.INDEX_TYPE,  "
                    + "    'IOT - TOP',I.TABLE_OWNER||'.'||I.TABLE_NAME,  "
                    + "    S.OWNER||'.'||S.SEGMENT_NAME) ||   "
                    + "  DECODE(I.INDEX_TYPE,  "
                    + "    'IOT - TOP',CHR(13)||CHR(10)||'   MOVE',  "
                    + "    CHR(13)||CHR(10)||'   REBUILD') ||  "
                    + "  DECODE(S.SEGMENT_TYPE,  "
                    + "    'INDEX',' TABLESPACE '||NVL(UPPER(:V2),'<TSNAME>'),  "
                    + "    'INDEX PARTITION',' PARTITION '||S.PARTITION_NAME||' TABLESPACE '  "
                    + "    ||NVL(UPPER(:V2),'<TSNAME>'),NULL)||  "
                    + "    DECODE(I.INDEX_TYPE,'NORMAL', "
                    + "   DECODE(I.COMPRESSION,'ENABLED',NULL,' ONLINE'),'IOT - TOP',  "
                    + "   DECODE(I.COMPRESSION,'ENABLED',NULL,' ONLINE'),NULL)||  "
                    + "  (CASE  "
                    + "      WHEN S.BYTES < 104857600  THEN ';'  "
                    + "      WHEN S.BYTES < 524288000  THEN ' PARALLEL 2;'  "
                    + "      WHEN S.BYTES < 2147483648 THEN ' PARALLEL 4;'  "
                    + "      ELSE ' PARALLEL 8;'  "
                    + "   END)  "
                    + "  AS MOVE_DDL  "
                    + "FROM DBA_INDEXES I,DBA_SEGMENTS S  "
                    + "WHERE S.OWNER = I.OWNER AND S.SEGMENT_NAME = I.INDEX_NAME  "
                    + "  AND I.OWNER=UPPER(UPPER(:V1_OWNER)) AND I.TABLE_NAME LIKE UPPER(:V1_NAME)  "
                    + "  AND (:V2 IS NULL OR S.TABLESPACE_NAME <> UPPER(:V2))  "
                    + "  AND S.SEGMENT_TYPE IN ('INDEX','INDEX PARTITION')  "
                    + "ORDER BY S.SEGMENT_TYPE DESC,S.SEGMENT_NAME,S.PARTITION_NAME",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "  'ALTER TABLE '||  "
                    + "  S.OWNER||'.'||S.SEGMENT_NAME ||  "
                    + "  ' MOVE '||DECODE(S.PARTITION_NAME,NULL,NULL,'PARTITION '||S.PARTITION_NAME) ||  "
                    + "  ' TABLESPACE '||NVL(UPPER(:V2),'<TSNAME>') ||  "
                    + "  (CASE  "
                    + "      WHEN S.BYTES < 104857600  THEN ';'  "
                    + "      WHEN S.BYTES < 524288000  THEN ' PARALLEL 2;'  "
                    + "      WHEN S.BYTES < 2147483648 THEN ' PARALLEL 4;'  "
                    + "      ELSE ' PARALLEL 8;'  "
                    + "   END)  "
                    + "  AS MOVE_DDL  "
                    + "FROM DBA_SEGMENTS S  "
                    + "WHERE S.OWNER=UPPER(:V1_OWNER) AND S.SEGMENT_NAME LIKE UPPER(UPPER(:V1_NAME))  "
                    + "  AND (:V2 IS NULL OR S.TABLESPACE_NAME <> UPPER(:V2))  "
                    + "  AND S.SEGMENT_TYPE IN ('TABLE','TABLE PARTITION')  "
                    + "ORDER BY S.SEGMENT_TYPE DESC,S.SEGMENT_NAME,S.PARTITION_NAME",
            "SELECT /* AnySQL */ * FROM (SELECT   "
                    + "  ADDRESS,COUNT(*) CHILDS ,SUM(SHARABLE_MEM) BYTES  "
                    + "  FROM V$SQL GROUP BY ADDRESS --HAVING COUNT(*) > 1  "
                    + "  ORDER BY 3 DESC ) WHERE ROWNUM < NVL(TO_NUMBER(:V1),20)",
            "SELECT /* AnySQL */  "
                    + "    U.US#,U.NAME RBS,U.BLOCK#,U.STATUS$,  "
                    + "     decode(u.status$, 1, 'NONEXIST',  "
                    + "         2, 'OFFLINE', 3, 'ONLINE',  "
                    + "         4, 'INVALID', 5, 'NEEDS RECOVERY',   "
                    + "         6, 'PARTLY AVAILABLE', 'UNDEFINED') USTATUS,  "
                    + "    R.STATUS RSTATUS,F.STATUS FSTATUS,F.NAME FILENAME  "
                    + "  FROM SYS.UNDO$ U,V$DATAFILE F,V$ROLLSTAT R  "
                    + "  WHERE U.FILE#=F.RFILE# AND U.TS#=F.TS#  "
                    + "    AND U.US#=R.USN(+) AND (:V1 IS NULL OR U.NAME LIKE UPPER(:V1)||'%')", "select /* AnySQL */  "
            + "  t.KTUXEUSN,t.KTUXESLT,t.KTUXESQN,t.KTUXESTA,t.KTUXECFL  "
            + "from sys.x$ktuxe t   "
            + "where t.ktuxesta <> 'INACTIVE'",
            "SELECT /* AnySQL */  "
                    + "  P.KSPPINM NAME, V.KSPPSTVL VALUE   "
                    + "FROM SYS.X$KSPPI P, SYS.X$KSPPSV V  "
                    + "WHERE P.INDX = V.INDX   "
                    + "  AND V.INST_ID = USERENV('Instance')  "
                    + "  AND SUBSTR(P.KSPPINM,1,1)='_'   "
                    + "  AND (:V1 IS NULL OR P.KSPPINM LIKE '%'||LOWER(:V1)||'%')", " select /* AnySQL */  "
            + "    nls_charset_id(value) ID, TO_CHAR(nls_charset_id(value),'0XXX') HEX_ID,  "
            + "    VALUE CHARSET_NAME  "
            + " from  v$nls_valid_values   "
            + " where parameter = 'CHARACTERSET'  "
            + "   AND nls_charset_id(value) IS NOT NULL  "
            + "   AND (:V1 IS NULL OR VALUE LIKE '%'||UPPER(:V1)||'%')",
            "SELECT /* AnySQL */ * FROM (SELECT   "
                    + "  SUBSTR(SQL_TEXT,1,60) SQL_TEXT,COUNT(*) CHILDS , "
                    + "  SUM(SHARABLE_MEM) BYTES  "
                    + "  FROM V$SQL GROUP BY SUBSTR(SQL_TEXT,1,60)  "
                    + "  ORDER BY 3 DESC ) WHERE ROWNUM < NVL(TO_NUMBER(:V1),20)",
            "SELECT /* AnySQL */ SQL_TEXT \"SQL Executing\" FROM V$SQLTEXT_WITH_NEWLINES  "
                    + " WHERE HASH_VALUE = TO_NUMBER(:V1)  "
                    + " ORDER BY PIECE",
            "SELECT /* AnySQL */ /*+ RULE */  "
                    + "    S.SID,S.SERIAL#,P.SPID,S.USERNAME,  "
                    + "    S.MACHINE,S.STATUS,S.PROGRAM PROGRAM  "
                    + "  FROM V$PROCESS P, V$SESSION S, V$LOCKED_OBJECT O    "
                    + "  WHERE P.ADDR = S.PADDR  AND O.SESSION_ID=S.SID     "
                    + "    AND S.USERNAME IS NOT NULL  "
                    + "    AND O.OBJECT_ID=TO_NUMBER(:V1)",
            "SELECT /*+ NOMERGE USE_NL(S,SQL) */  "
                    + "   DECODE(SQL.PIECE,0,S.SQL_ADDRESS,NULL) SQL_ADDRESS,  "
                    + "   DECODE(SQL.PIECE,0,S.HASH_VALUE,NULL) HASH_VALUE,  "
                    + "   DECODE(SQL.PIECE,0,S.SESS_COUNT,NULL) SESS_COUNT,  "
                    + "   SQL.SQL_TEXT  "
                    + "FROM   "
                    + "(SELECT /*+ AnySQL */   "
                    + "   SQL_ADDRESS,SQL_HASH_VALUE HASH_VALUE,COUNT(*) SESS_COUNT   "
                    + "FROM V$SESSION   "
                    + "WHERE ((STATUS='ACTIVE' OR STATUS='KILLED') AND SQL_HASH_VALUE > 0)  "
                    + "GROUP BY SQL_ADDRESS,SQL_HASH_VALUE) S,V$SQLTEXT SQL  "
                    + "WHERE S.HASH_VALUE = SQL.HASH_VALUE AND S.SQL_ADDRESS=SQL.ADDRESS  "
                    + " AND SQL.PIECE < 5 ORDER BY S.SQL_ADDRESS,S.HASH_VALUE,SQL.PIECE", "select /* AnySQL */ /*+ ORDERED FIRST_ROWS */  "
            + "   l.sid,s.serial#,s.machine,s.username, "
            + "   decode(l.type,  "
            + "           'MR', 'Media Recovery',  "
            + "           'RT', 'Redo Thread',  "
            + "           'UN', 'User Name',  "
            + "           'TX', 'Transaction',  "
            + "           'TM', 'DML',  "
            + "           'UL', 'PL/SQL User Lock',  "
            + "           'DX', 'Distributed Xaction',  "
            + "           'CF', 'Control File',  "
            + "           'IS', 'Instance State',  "
            + "           'FS', 'File Set',  "
            + "           'IR', 'Instance Recovery',  "
            + "           'ST', 'Disk Space Transaction',  "
            + "           'TS', 'Temp Segment',  "
            + "           'IV', 'Library Cache Invalidation',  "
            + "           'LS', 'Log Start or Switch',  "
            + "           'RW', 'Row Wait',  "
            + "           'SQ', 'Sequence Number',  "
            + "           'TE', 'Extend Table',  "
            + "           'TT', 'Temp Table',  "
            + "           'TC', 'Thread Checkpoint',  "
            + "            'SS', 'Sort Segment',  "
            + "            'JQ', 'Job Queue',  "
            + "            'PI', 'Parallel operation',  "
            + "            'PS', 'Parallel operation',  "
            + "            'DL', 'Direct Index Creation',  "
            + "           l.type) type,  "
            + "   decode(l.lmode,  "
            + "           0, 'None',             "
            + "           1, 'Null',             "
            + "           2, 'Row-S (SS)',       "
            + "           3, 'Row-X (SX)',       "
            + "           4, 'Share',            "
            + "           5, 'S/Row-X (SSX)',    "
            + "           6, 'Exclusive',        "
            + "           to_char(lmode)) hold,  "
            + "    decode(l.request,  "
            + "           0, 'None',             "
            + "           1, 'Null',             "
            + "           2, 'Row-S (SS)',       "
            + "           3, 'Row-X (SX)',       "
            + "           4, 'Share',            "
            + "           5, 'S/Row-X (SSX)',    "
            + "           6, 'Exclusive',        "
            + "           to_char(request)) request,  "
            + "    l.ID1,l.ID2,l.CTIME  "
            + "    from v$lock l, v$session s  "
            + "   where l.sid=s.sid and l.type <> 'MR' and l.block=1",
            "SELECT * FROM V$PX_PROCESS_SYSSTAT",
            "SELECT * FROM V$PX_PROCESS",
            "SELECT * FROM V$PX_SESSION",
            "SELECT * FROM V$PX_PROCESS",
            "SELECT  "
                    + "  SLAVE_NAME,STATUS,SESSIONS,IDLE_TIME_CUR IDLECUR,BUSY_TIME_CUR BUSYCUR,  "
                    + "  MSGS_SENT_CUR MSGSCUR,MSGS_RCVD_CUR MSGRCUR,IDLE_TIME_TOTAL IDLEALL, "
                    + "  BUSY_TIME_TOTAL BUSYALL, MSGS_SENT_TOTAL MSGSALL,MSGS_RCVD_TOTAL MSGRALL  "
                    + "FROM V$PQ_SLAVE",
            "SELECT /* AnySQL */ SQL_TEXT \"SQL Executing\" FROM V$SQLTEXT_WITH_NEWLINES  "
                    + " WHERE HASH_VALUE = TO_NUMBER(:V1)  "
                    + " ORDER BY PIECE",
            "SELECT /*+ NOMERGE USE_NL(S,SQL) */  "
                    + "   DECODE(SQL.PIECE,0,S.ADDRESS,NULL) SQL_ADDRESS,  "
                    + "   DECODE(SQL.PIECE,0,S.HASH_VALUE,NULL) HASH_VALUE,  "
                    + "   SQL.SQL_TEXT  "
                    + "FROM   "
                    + "(SELECT ADDRESS,HASH_VALUE FROM V$SQLAREA WHERE SQL_TEXT LIKE  "
                    + "    '%'||NVL(:V1,'XXXXXX')||'%') S,V$SQLTEXT SQL "
                    + "WHERE S.HASH_VALUE = SQL.HASH_VALUE AND S.ADDRESS=SQL.ADDRESS  "
                    + " AND SQL.PIECE < 5 ORDER BY S.ADDRESS,S.HASH_VALUE,SQL.PIECE",
            "SELECT /*+ NOMERGE */  "
                    + "   S.USERNAME,W.EVENT,S.SQL_HASH_VALUE HASH_VALUE,COUNT(*) WSESS  "
                    + "FROM V$SESSION S,  "
                    + "  (SELECT /*+ RULE */ SID,EVENT FROM V$SESSION_WAIT   "
                    + "    WHERE EVENT IN ('buffer busy waits','enqueue','library cache pin','library cache lock')) W  "
                    + "WHERE S.SID = W.SID  "
                    + "GROUP BY S.USERNAME,W.EVENT,S.SQL_HASH_VALUE  "
                    + "UNION ALL  "
                    + "SELECT /*+ NOMERGE */  "
                    + "   S.USERNAME,L.NAME EVENT,S.SQL_HASH_VALUE HASH_VALUE,COUNT(*) WSESS  "
                    + "FROM V$SESSION S,V$LATCHNAME L,  "
                    + "  (SELECT /*+ RULE */ SID,P2 FROM V$SESSION_WAIT   "
                    + "    WHERE EVENT = 'latch free') W  "
                    + "WHERE S.SID = W.SID AND W.P2 = L.LATCH#  "
                    + "GROUP BY S.USERNAME,L.NAME,S.SQL_HASH_VALUE",
            "SELECT /* AnySQL PLAN */ * FROM (SELECT /*+ RULE */  "
                    + "    LPAD(TO_CHAR(ID),3,' ')||LPAD(NVL(TO_CHAR(PARENT_ID),' '),4,' ')||  "
                    + "    ' '||LPAD(' ',2*(LEVEL-1),' ')||  "
                    + "    OPERATION||' '||DECODE(OPTIONS,NULL,'','('||OPTIONS||') ')   "
                    + "    ||DECODE(ID,0,'Optimizer='||OPTIMIZER||' ','')   "
                    + "    ||DECODE(OBJECT_OWNER,NULL,'','OF '||   "
                    + "    OBJECT_NAME) SQLPLAN,  "
                    + "    COST, CARDINALITY CARD,ROUND(Bytes/1024) KByte,PARTITION_START PS,PARTITION_STOP PE "
                    + " FROM (SELECT * FROM V$SQL_PLAN WHERE HASH_VALUE = TO_NUMBER(:V1))  "
                    + "  CONNECT BY PARENT_ID = PRIOR ID AND CHILD_NUMBER=PRIOR CHILD_NUMBER START WITH ID = 0 ORDER BY CHILD_NUMBER,ID)  "
                    + " UNION ALL  "
                    + " SELECT DECODE(ROWNUM,1,CHR(10),'')||SQLPLAN,COST,CARD,BYTES,P1,P2  "
                    + "    FROM (SELECT  LPAD(TO_CHAR(ID),3,' ')||'   '||OTHER_TAG SQLPLAN,  "
                    + "    TO_NUMBER(NULL) COST, TO_NUMBER(NULL) CARD,TO_NUMBER(NULL) Bytes,NULL P1,NULL P2  "
                    + "    FROM V$SQL_PLAN WHERE OTHER_TAG IS NOT NULL AND HASH_VALUE=TO_NUMBER(:V1)  "
                    + "    ORDER BY CHILD_NUMBER,ID)", "Select /* ANYSQL */ tablespace_name ,round(bytes/1048576) size_mb, count(1) extent_count, count(distinct file_id) files  "
            + "    from dba_free_space where :v1 is null or tablespace_name = upper(:v1)  "
            + "    group by tablespace_name, round(bytes/1048576)   "
            + "    having round(bytes/1048576) > 50 or count(1) > 100  "
            + "order by 1,3", "Select /* ANYSQL */ MACHINE,COUNT(*) SESSION_COUNT FROM V$SESSION GROUP BY MACHINE ORDER BY 2", "Select /* ANYSQL */ 'ALTER SYSTEM DISCONNECT SESSION /* '  "
            + "   ||MACHINE||' */ '''||SID||','||SERIAL#||''' IMMEDIATE;' KILL_SQL  "
            + "  FROM V$SESSION WHERE MACHINE = :V1", "Select /* ANYSQL */ 'ALTER SYSTEM DISCONNECT SESSION /* '  "
            + "   ||MACHINE||' */ '''||SID||','||SERIAL#||''' IMMEDIATE;' KILL_SQL  "
            + "  FROM V$SESSION WHERE USERNAME = :V1", "select * from  "
            + "(SELECT hladdr, dbarfil, dbablk, tch FROM x$bh   "
            + "WHERE hladdr in   "
            + "  (select /*+ hash_sj */ addr from  "
            + "   (SELECT addr, latch#, sleeps   "
            + "       FROM v$latch_children   "
            + "       WHERE name = 'cache buffers chains'   "
            + "         AND sleeps > 0 ORDER BY sleeps DESC)  "
            + "   where rownum < 101) and tch > 0  "
            + "ORDER BY tch DESC  "
            + ") where rownum < nvl(to_number(:v1),51)", "Select /* ANYSQL */ 'ALTER SYSTEM DISCONNECT SESSION /* '  "
            + "   ||MACHINE||' */ '''||SID||','||SERIAL#||''' IMMEDIATE;' KILL_SQL  "
            + "  FROM V$SESSION WHERE SQL_HASH_VALUE = to_number(:V1)",
            "SELECT /* AnySQL */ /*+ RULE */   "
                    + "    'ALTER SYSTEM DISCONNECT SESSION /* '   "
                    + "    ||MACHINE||' */ '''||S.SID||','||S.SERIAL#||''' IMMEDIATE;' KILL_SQL   "
                    + "  FROM  V$SESSION S, V$LOCKED_OBJECT O  "
                    + "  WHERE O.SESSION_ID=S.SID  "
                    + "    AND S.USERNAME IS NOT NULL  "
                    + "    AND O.OBJECT_ID=TO_NUMBER(:V1)",
            "SELECT /* AnySQL */  "
                    + "   OWNER,NULL PARTNAME, INITIAL_EXTENT/1024 INIEXT, NEXT_EXTENT/1024 NXTEXT,  "
                    + "   NUM_ROWS NROWS, BLOCKS, AVG_SPACE AVGSPC,CHAIN_CNT CCNT, AVG_ROW_LEN ROWLEN,  "
                    + "   SAMPLE_SIZE SSIZE,LAST_ANALYZED ANADATE  "
                    + "FROM ALL_TABLES  "
                    + "WHERE UPPER(OWNER)=NVL(UPPER(:V1_OWNER),OWNER)  AND TABLE_NAME=UPPER(:V1_NAME)  "
                    + "UNION ALL  "
                    + "SELECT /* AnySQL */  "
                    + "   TABLE_OWNER OWNER,PARTITION_NAME PARTNAME, INITIAL_EXTENT/1024 INIEXT, NEXT_EXTENT/1024 NXTEXT,  "
                    + "   NUM_ROWS NROWS, BLOCKS, AVG_SPACE AVGSPC,CHAIN_CNT CCNT, AVG_ROW_LEN ROWLEN,  "
                    + "   SAMPLE_SIZE SSIZE,LAST_ANALYZED ANADATE  "
                    + "FROM ALL_TAB_PARTITIONS  "
                    + "WHERE UPPER(TABLE_OWNER)=NVL(UPPER(:V1_OWNER),TABLE_OWNER)  AND TABLE_NAME=UPPER(:V1_NAME)",
            "SELECT /* AnySQL */   "
                    + "   TABLE_OWNER OWNER, INDEX_NAME, INITIAL_EXTENT/1024 INIEXT, NEXT_EXTENT/1024 NXTEXT,  "
                    + "   BLEVEL, LEAF_BLOCKS LBLKS,TRUNC(NUM_ROWS) NROWS,DISTINCT_KEYS DROWS,  "
                    + "   AVG_LEAF_BLOCKS_PER_KEY LKEY, AVG_DATA_BLOCKS_PER_KEY DKEY,  "
                    + "   CLUSTERING_FACTOR CLSFCT,SAMPLE_SIZE SSIZE, LAST_ANALYZED ANADAY,  "
                    + "   PARTITIONED PAR  "
                    + "FROM ALL_INDEXES  "
                    + "WHERE UPPER(TABLE_OWNER)=NVL(UPPER(:V1_OWNER),TABLE_OWNER)  AND TABLE_NAME=UPPER(:V1_NAME)",
            "SELECT /*+ AnySQL */ OWNER,SEGMENT_NAME, SEGMENT_TYPE, SUM(BYTES)/1048576 SIZE_MB,  "
                    + "           MAX(INITIAL_EXTENT) INIEXT, MAX(NEXT_EXTENT) MAXEXT FROM DBA_SEGMENTS  "
                    + "    WHERE SEGMENT_NAME = upper(:V1_NAME)  "
                    + "      AND (:V1_OWNER IS NULL OR UPPER(OWNER) = UPPER(:V1_OWNER))  "
                    + "      AND SEGMENT_TYPE LIKE 'TABLE%'  "
                    + "    GROUP BY OWNER, SEGMENT_NAME, SEGMENT_TYPE  "
                    + "  UNION ALL  "
                    + "    SELECT OWNER, SEGMENT_NAME, SEGMENT_TYPE, SUM(BYTES)/1048576 SIZE_MB,  "
                    + "           MAX(INITIAL_EXTENT) INIEXT, MAX(NEXT_EXTENT) MAXEXT FROM DBA_SEGMENTS  "
                    + "    WHERE (OWNER,SEGMENT_NAME) IN (  "
                    + "       SELECT OWNER,INDEX_NAME FROM DBA_INDEXES WHERE TABLE_NAME=upper(:V1_NAME) AND  "
                    + "       (:V1_OWNER IS NULL OR UPPER(TABLE_OWNER) = UPPER(:V1_OWNER))  "
                    + "       UNION  "
                    + "       SELECT OWNER,SEGMENT_NAME FROM DBA_LOBS WHERE TABLE_NAME=upper(:V1_NAME) AND  "
                    + "       (:V1_OWNER IS NULL OR UPPER(OWNER) = UPPER(:V1_OWNER)))  "
                    + "    GROUP BY OWNER, SEGMENT_NAME, SEGMENT_TYPE",
            "SELECT /* AnySQL */  "
                    + "  'ALTER INDEX '||OWNER||'.'||INDEX_NAME||' REBUILD ONLINE;' UNUSABLE_INDEXES  "
                    + "FROM ALL_INDEXES  "
                    + "WHERE TABLE_OWNER=UPPER(:V1) AND STATUS='UNUSABLE'  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER INDEX '||IP.INDEX_OWNER||'.'||IP.INDEX_NAME||' REBUILD PARTITION '  "
                    + "       ||IP.PARTITION_NAME||' ONLINE;'  "
                    + "FROM ALL_IND_PARTITIONS IP, ALL_INDEXES I  "
                    + "WHERE IP.INDEX_OWNER=I.OWNER AND IP.INDEX_NAME=I.INDEX_NAME  "
                    + "  AND I.TABLE_OWNER=UPPER(:V1) AND IP.STATUS='UNUSABLE'  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER INDEX '||IP.INDEX_OWNER||'.'||IP.INDEX_NAME||' REBUILD SUBPARTITION '  "
                    + "       ||IP.PARTITION_NAME||' ONLINE;'  "
                    + "FROM ALL_IND_SUBPARTITIONS IP, ALL_INDEXES I  "
                    + "WHERE IP.INDEX_OWNER=I.OWNER AND IP.INDEX_NAME=I.INDEX_NAME  "
                    + "  AND I.TABLE_OWNER=UPPER(:V1) AND IP.STATUS='UNUSABLE'",
            "SELECT /* AnySQL */  "
                    + "    OBJECT_ID, OWNER,OBJECT_NAME,OBJECT_TYPE,STATUS,  "
                    + "  to_char(created,'yy-mm-dd hh24:mi:ss') created,  "
                    + "  to_char(LAST_DDL_TIME,'yy-mm-dd hh24:mi:ss') last_ddl_time  "
                    + "FROM ALL_OBJECTS  "
                    + "WHERE STATUS='INVALID' AND (:V1 IS NULL OR OWNER=UPPER(:V1))", "WITH FOBJ AS  "
            + "(SELECT DISTINCT FILE_ID,OWNER,SEGMENT_NAME,PARTITION_NAME,SEGMENT_TYPE  "
            + "   FROM DBA_EXTENTS  "
            + "   WHERE TABLESPACE_NAME = UPPER(:V1))  "
            + "SELECT   "
            + "  DISTINCT  "
            + "         DECODE(FOBJ.SEGMENT_TYPE,'INDEX PARTITION',  "
            + "               'ALTER INDEX '||FOBJ.SEGMENT_NAME||' REBUILD PARTITION '||FOBJ.PARTITION_NAME||' ONLINE;',  "
            + "               'INDEX', 'ALTER INDEX '||FOBJ.SEGMENT_NAME||' REBUILD ONLINE;',  "
            + "               OWNER||'.'||SEGMENT_NAME||DECODE(PARTITION_NAME,NULL,'',' PARTITION '||PARTITION_NAME)) REBUILD_SQL  "
            + "FROM  "
            + "  FOBJ,  "
            + "  (SELECT FILE_ID,COUNT(*) OCNT FROM FOBJ  "
            + "   GROUP BY FILE_ID  "
            + "   HAVING COUNT(*) < TO_NUMBER(NVL(:V2,'6'))  "
            + "   ORDER BY 2 ) FCNT  "
            + "WHERE FCNT.FILE_ID=FOBJ.FILE_ID  "
            + "ORDER BY 1",
            "SELECT /*+ NO_MERGE */ 'ALTER DATABASE DATAFILE '''||FILE_NAME||''' RESIZE 16K;' RESIZE_CMD   "
                    + "FROM DBA_DATA_FILES WHERE TABLESPACE_NAME=UPPER(:V1)   "
                    + "  AND BYTES > (TO_NUMBER(NVL(:V2,'1')) * 1048576) AND FILE_ID NOT IN  "
                    + "    (SELECT /*+ HASH_AJ */ FILE_ID FROM DBA_EXTENTS WHERE TABLESPACE_NAME=UPPER(:V1))",
            "SELECT 'ALTER TABLE '||TABLE_NAME||' LOGGING;' LOGGING_SQL FROM ALL_TABLES WHERE LOGGING='NO' AND OWNER=NVL(UPPER(:V1),USER)  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER TABLE '||TABLE_NAME||' MODIFY PARTITION '||PARTITION_NAME||' LOGGING;' FROM ALL_TAB_PARTITIONS WHERE LOGGING='NO' AND TABLE_OWNER=NVL(UPPER(:V1),USER)  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER TABLE '||TABLE_NAME||' MODIFY SUBPARTITION '||SUBPARTITION_NAME||' LOGGING;' FROM ALL_TAB_SUBPARTITIONS WHERE LOGGING='NO' AND TABLE_OWNER=NVL(UPPER(:V1),USER)  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER INDEX '||INDEX_NAME||' LOGGING;' FROM ALL_INDEXES WHERE LOGGING='NO' AND OWNER=NVL(UPPER(:V1),USER)  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER INDEX '||INDEX_NAME||' MODIFY PARTITION '||PARTITION_NAME||' LOGGING;' FROM ALL_IND_PARTITIONS WHERE LOGGING='NO' AND INDEX_OWNER=NVL(UPPER(:V1),USER)  "
                    + "UNION ALL  "
                    + "SELECT 'ALTER INDEX '||INDEX_NAME||' MODIFY SUBPARTITION '||SUBPARTITION_NAME||' LOGGING;' FROM ALL_IND_SUBPARTITIONS WHERE LOGGING='NO' AND INDEX_OWNER=NVL(UPPER(:V1),USER)",
            "SELECT /* AnySQL */  "
                    + "   ADDRESS, HASH_VALUE, OPEN_VERSIONS VERS, SORTS, EXECUTIONS EXECS,   "
                    + "   DISK_READS READS, BUFFER_GETS GETS, ROWS_PROCESSED ROWCNT, MODULE, ACTION  "
                    + " FROM V$SQL WHERE HASH_VALUE IN  "
                    + "   (SELECT /*+ NL_SJ */ DISTINCT HASH_VALUE FROM V$SQL_PLAN WHERE OBJECT_NAME=UPPER(:V1_NAME)  "
                    + "      AND NVL(OBJECT_OWNER,'A')=UPPER(NVL(:V1_OWNER,'A')))", "select  "
            + "  decode(drank,1,snap_time,' ') TIME_STAMP, drank R#, "
            + "  HASH_VALUE, SORTS, FETCHS, EXECS, GETS, TRUNC(GETS/EXECS) GET_E,   "
            + "  TRUNC(GETS_PCT * 100,2) GETS_PCT ,  "
            + "  DISKS, TRUNC(DISKS/EXECS) DISK_E, TRUNC(DISK_PCT * 100,2) DISK_PCT ,  "
            + "  RECORDS, TRUNC(RECORDS/EXECS) ROW_E, LOADS, VALIDS, VCOUNT  "
            + "from  "
            + "(  "
            + "select sql.*,  "
            + "   row_number() over (partition by snap_time order by gets + disks * 10 desc) drank,  "
            + "   ratio_to_report(gets) over (partition by snap_time) gets_pct,  "
            + "   ratio_to_report(disks) over (partition by snap_time) disk_pct  "
            + "from (select   "
            + "   to_char(snap.snap_time, 'MM/DD HH24:MI') snap_time,  "
            + "   sqla.hash_value,  "
            + "   sqla.sorts - sqlb.sorts sorts,  "
            + "   sqla.fetches - sqlb.fetches fetchs,  "
            + "   sqla.EXECUTIONS - sqlb.EXECUTIONS EXECS,  "
            + "   sqla.loads - sqlb.loads loads,  "
            + "   sqla.INVALIDATIONS - sqlb.INVALIDATIONS VALIDS,  "
            + "   sqla.DISK_READS - sqlb.DISK_READS DISKS,  "
            + "   sqla.BUFFER_GETS - sqlb.BUFFER_GETS GETS,  "
            + "   sqla.ROWS_PROCESSED - sqlb.ROWS_PROCESSED RECORDS,  "
            + "   sqla.VERSION_COUNT  - sqlb.VERSION_COUNT VCOUNT  "
            + "from  "
            + "perfstat.stats$sql_summary sqla, perfstat.stats$sql_summary sqlb,  "
            + "(select   "
            + "    A.SNAP_TIME,  "
            + "    A.SNAP_ID SNAP_ID_A,A.dbid DB_ID_A, A.INSTANCE_NUMBER INST_A,  "
            + "    B.SNAP_ID SNAP_ID_B,B.dbid DB_ID_B, B.INSTANCE_NUMBER INST_B  "
            + "  from perfstat.stats$snapshot a, perfstat.stats$snapshot b   "
            + "  where A.SNAP_TIME >= sysdate - to_number(nvl(:v1,'0.5')) AND   "
            + "        A.SNAP_TIME <= sysdate - to_number(nvl(:v2,'0')) AND   "
            + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) >= to_number(nvl(:v3,'0')) AND   "
            + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) <= to_number(nvl(:v4,'23')) AND   "
            + "        B.SNAP_ID = (SELECT MAX(SNAP_ID) FROM stats$snapshot WHERE SNAP_ID < A.SNAP_ID) AND  "
            + "        B.DBID = A.DBID AND B.INSTANCE_NUMBER = A.INSTANCE_NUMBER AND  "
            + "        B.STARTUP_TIME = A.STARTUP_TIME  "
            + ") SNAP  "
            + "where sqla.snap_id = snap.snap_id_a   "
            + "  and sqla.dbid    = snap.db_id_a  "
            + "  and sqla.instance_number = snap.inst_a  "
            + "  and sqla.COMMAND_TYPE <> 47  "
            + "  and sqlb.snap_id = snap.snap_id_b  "
            + "  and sqlb.dbid    = snap.db_id_b  "
            + "  and sqlb.instance_number = snap.inst_b  "
            + "  and sqla.hash_value = sqlb.hash_value  "
            + "  and sqla.TEXT_SUBSET = sqlb.TEXT_SUBSET  "
            + ") sql where execs > 0 and gets > 0 "
            + ") where drank <= to_number(nvl(:v5,'5'))  "
            + "order by snap_time, drank", "select  "
            + "   to_char(snap.snap_time, 'MM/DD HH24:MI') snap_time,  "
            + "   sqla.sorts - sqlb.sorts sorts,  "
            + "   sqla.fetches - sqlb.fetches fetchs,  "
            + "   sqla.EXECUTIONS - sqlb.EXECUTIONS EXECS,  "
            + "   sqla.loads - sqlb.loads loads,  "
            + "   sqla.INVALIDATIONS - sqlb.INVALIDATIONS VALIDS,  "
            + "   sqla.DISK_READS - sqlb.DISK_READS DISKS,  "
            + "   trunc((sqla.DISK_READS - sqlb.DISK_READS)/(sqla.EXECUTIONS - sqlb.EXECUTIONS)) DISK_E,  "
            + "   sqla.BUFFER_GETS - sqlb.BUFFER_GETS GETS,  "
            + "   trunc((sqla.BUFFER_GETS - sqlb.BUFFER_GETS)/(sqla.EXECUTIONS - sqlb.EXECUTIONS)) GET_E,  "
            + "   sqla.ROWS_PROCESSED - sqlb.ROWS_PROCESSED RECORDS,  "
            + "   trunc((sqla.ROWS_PROCESSED - sqlb.ROWS_PROCESSED)/(sqla.EXECUTIONS - sqlb.EXECUTIONS)) ROW_E,  "
            + "   sqla.VERSION_COUNT  - sqlb.VERSION_COUNT VCOUNT  "
            + "from  "
            + "perfstat.stats$sql_summary sqla, perfstat.stats$sql_summary sqlb,  "
            + "(select  "
            + "    A.SNAP_TIME,  "
            + "    A.SNAP_ID SNAP_ID_A,A.dbid DB_ID_A, A.INSTANCE_NUMBER INST_A,  "
            + "    B.SNAP_ID SNAP_ID_B,B.dbid DB_ID_B, B.INSTANCE_NUMBER INST_B  "
            + "  from perfstat.stats$snapshot a, perfstat.stats$snapshot b  "
            + "  where A.SNAP_TIME > sysdate - to_number(nvl(:v2,'1')) AND  "
            + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) >= to_number(nvl(:v3,'0'))  AND  "
            + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) <= to_number(nvl(:v4,'23'))  AND  "
            + "        B.SNAP_ID = (SELECT MAX(SNAP_ID) FROM stats$snapshot WHERE SNAP_ID < A.SNAP_ID) AND  "
            + "        B.DBID = A.DBID AND B.INSTANCE_NUMBER = A.INSTANCE_NUMBER AND  "
            + "        B.STARTUP_TIME = A.STARTUP_TIME  "
            + ") SNAP  "
            + "where sqla.snap_id = snap.snap_id_a  "
            + "  and sqla.dbid    = snap.db_id_a  "
            + "  and sqla.instance_number = snap.inst_a  "
            + "  and sqla.COMMAND_TYPE <> 47  "
            + "  and sqla.hash_value = to_number(:v1)  "
            + "  and sqlb.snap_id = snap.snap_id_b  "
            + "  and sqlb.dbid    = snap.db_id_b  "
            + "  and sqlb.instance_number = snap.inst_b  "
            + "  and sqla.hash_value = sqlb.hash_value  "
            + "  and sqla.TEXT_SUBSET = sqlb.TEXT_SUBSET  "
            + "  and (sqla.executions - sqlb.executions) > 0",
            "SELECT /* AnySQL */ * FROM DBA_TAB_PRIVS  "
                    + " WHERE (OWNER=NVL(UPPER(:V1_OWNER),OWNER)) AND TABLE_NAME LIKE UPPER(:V1_NAME)",
            "SELECT /* AnySQL */ ID, PREDICATES FROM  "
                    + "(  "
                    + "SELECT CHILD_NUMBER, ID, 'A: '||ACCESS_PREDICATES PREDICATES FROM V$SQL_PLAN   "
                    + "  WHERE HASH_VALUE=TO_NUMBER(:V1) AND ACCESS_PREDICATES IS NOT NULL  "
                    + "UNION ALL  "
                    + "SELECT CHILD_NUMBER, ID, 'F: '||FILTER_PREDICATES FROM V$SQL_PLAN   "
                    + "  WHERE HASH_VALUE=TO_NUMBER(:V1) AND FILTER_PREDICATES IS NOT NULL  "
                    + ")  "
                    + "ORDER BY CHILD_NUMBER, ID",
            "SELECT  "
                    + "   decode(rid,1,day,null) day,  "
                    + "   hour, login, exec, gets,   "
                    + "   read, write, parse, cmmt,   "
                    + "   sort, cputime  "
                    + "FROM  "
                    + "(select  "
                    + "   day, hour, ROW_NUMBER() OVER (PARTITION BY DAY ORDER BY HOUR) rid,   "
                    + "   sum(decode(name,'logons cumulative',value,0)) LOGIN,  "
                    + "   sum(decode(name,'execute count',value,0)) EXEC,  "
                    + "   sum(decode(name,'session logical reads',value,0)) GETS,  "
                    + "   sum(decode(name,'physical reads',value,0)) READ,  "
                    + "   sum(decode(name,'physical writes',value,0)) WRITE,  "
                    + "   sum(decode(name,'parse count (total)',value,0)) PARSE,  "
                    + "   sum(decode(name,'user commits',value,0)) CMMT,  "
                    + "   sum(decode(name,'sorts (memory)',value,0)) SORT,  "
                    + "   sum(decode(name,'CPU used by this session',value,0)) CPUTIME  "
                    + "from  "
                    + "(  "
                    + "select  "
                    + "   to_char(snap.snap_time, 'HH24:MI') hour, to_char(snap.snap_time, 'MM/DD') DAY,  "
                    + "   sqla.name,   "
                    + "   sqla.value - sqlb.value value  "
                    + "from  "
                    + "perfstat.STATS$SYSSTAT sqla, perfstat.STATS$SYSSTAT sqlb,  "
                    + "(select  "
                    + "    A.SNAP_TIME,  "
                    + "    A.SNAP_ID SNAP_ID_A,A.dbid DB_ID_A, A.INSTANCE_NUMBER INST_A,  "
                    + "    B.SNAP_ID SNAP_ID_B,B.dbid DB_ID_B, B.INSTANCE_NUMBER INST_B  "
                    + "  from perfstat.stats$snapshot a, perfstat.stats$snapshot b  "
                    + "  where A.SNAP_TIME >= sysdate - to_number(nvl(:V1,'0.5')) AND  "
                    + "        A.SNAP_TIME <= sysdate - to_number(nvl(:V2,'0')) AND  "
                    + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) >= to_number(nvl(:V3,'0')) AND  "
                    + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) <= to_number(nvl(:V4,'23')) AND  "
                    + "        B.SNAP_ID = (SELECT MAX(SNAP_ID) FROM stats$snapshot WHERE SNAP_ID < A.SNAP_ID) AND  "
                    + "        B.DBID = A.DBID AND B.INSTANCE_NUMBER = A.INSTANCE_NUMBER AND  "
                    + "        B.STARTUP_TIME = A.STARTUP_TIME  "
                    + ") SNAP  "
                    + "where sqla.snap_id = snap.snap_id_a  "
                    + "  and sqla.dbid    = snap.db_id_a  "
                    + "  and sqla.instance_number = snap.inst_a  "
                    + "  and sqla.NAME in ('execute count', 'physical reads', 'session logical reads',   "
                    + "                    'physical writes', 'parse count (total)', 'user commits',  "
                    + "                    'sorts (memory)','logons cumulative','CPU used by this session')  "
                    + "  and sqlb.snap_id = snap.snap_id_b  "
                    + "  and sqlb.dbid    = snap.db_id_b  "
                    + "  and sqlb.instance_number = snap.inst_b  "
                    + "  and sqla.name = sqlb.name  "
                    + ")  "
                    + "GROUP BY day, hour  "
                    + ") A ",
            "SELECT  "
                    + "   decode(rid,1,day,null) day, hour,   "
                    + "   s_read, m_read, d_read, d_write,  "
                    + "   enq, l_free, lf_sync, l_pin  "
                    + "FROM  "
                    + "(select  "
                    + "   day, hour, ROW_NUMBER() OVER (PARTITION BY DAY ORDER BY HOUR) rid,  "
                    + "   sum(decode(name,'db file sequential read',value,0)) S_READ,  "
                    + "   sum(decode(name,'db file scattered read',value,0)) M_READ,  "
                    + "   sum(decode(name,'latch free',value,0)) L_FREE,  "
                    + "   sum(decode(name,'out file sync',value,0)) LF_SYNC,  "
                    + "   sum(decode(name,'direct path read',value,0)) D_READ,  "
                    + "   sum(decode(name,'direct path write',value,0)) D_WRITE,  "
                    + "   sum(decode(name,'enqueue',value,0)) ENQ,  "
                    + "   sum(decode(name,'library cache pin',value,0)) L_PIN  "
                    + "from  "
                    + "(  "
                    + "select  "
                    + "   to_char(snap.snap_time, 'HH24:MI') hour, to_char(snap.snap_time, 'MM/DD') DAY,  "
                    + "   sqla.event name,  "
                    + "   sqla.total_waits - sqlb.total_waits value  "
                    + "from  "
                    + "perfstat.STATS$system_event sqla, perfstat.STATS$system_event sqlb,  "
                    + "(select  "
                    + "    A.SNAP_TIME,  "
                    + "    A.SNAP_ID SNAP_ID_A,A.dbid DB_ID_A, A.INSTANCE_NUMBER INST_A,  "
                    + "    B.SNAP_ID SNAP_ID_B,B.dbid DB_ID_B, B.INSTANCE_NUMBER INST_B  "
                    + "  from perfstat.stats$snapshot a, perfstat.stats$snapshot b  "
                    + "  where A.SNAP_TIME >= sysdate - to_number(nvl(:v1,'0.5')) AND  "
                    + "        A.SNAP_TIME <= sysdate - to_number(nvl(:v2,'0')) AND  "
                    + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) >= to_number(nvl(:v3,'0')) AND  "
                    + "        TO_NUMBER(TO_CHAR(A.SNAP_TIME,'HH24')) <= to_number(nvl(:v4,'23')) AND  "
                    + "        B.SNAP_ID = (SELECT MAX(SNAP_ID) FROM stats$snapshot WHERE SNAP_ID < A.SNAP_ID) AND  "
                    + "        B.DBID = A.DBID AND B.INSTANCE_NUMBER = A.INSTANCE_NUMBER AND  "
                    + "        B.STARTUP_TIME = A.STARTUP_TIME  "
                    + ") SNAP  "
                    + "where sqla.snap_id = snap.snap_id_a  "
                    + "  and sqla.dbid    = snap.db_id_a  "
                    + "  and sqla.instance_number = snap.inst_a  "
                    + "  and sqla.event in ('db file sequential read', 'db file scattered read',   "
                    + "                     'latch free', 'out file sync','direct path read',  "
                    + "                     'direct path write','enqueue', 'library cache pin')  "
                    + "  and sqlb.snap_id = snap.snap_id_b  "
                    + "  and sqlb.dbid    = snap.db_id_b  "
                    + "  and sqlb.instance_number = snap.inst_b  "
                    + "  and sqla.event = sqlb.event  "
                    + ")  "
                    + "GROUP BY day, hour  "
                    + ") A"};

    public static final void main(String[] paramArrayOfString) {
        System.out.println(getCount());
    }

    public static final int getCount() {
        return COMMANDSQL.length;
    }

    public static final String getORASQL(int paramInt) {
        if ((paramInt >= 0) && (paramInt < COMMANDSQL.length))
            return COMMANDSQL[paramInt];
        return null;
    }
    
    public static void oraUsage(CommandLog log) {
        log.println("Usage: ");
        log.println("  ORA keyword [parameters]  ");
        log.println("  ------------------------------- -------------------------------- ");
        log.println("  PARAMETER   name                INITORA   name ");
        log.println("  SPID        spid                2PC   ");
        log.println("  ACTIVE                          SQL       sid ");
        log.println("  SORT                            SESSION   patterm ");
        log.println("  BGSESS                          DEAD  ");
        log.println("  TSFREE                          FILEFREE  tablespace ");
        log.println("  SEGMENT    size(MB)             EXTENT  ");
        log.println("  NOINDEX    size(MB)             INDEX     size(MB) ");
        log.println("  RBS                             LOCKWAIT");
        log.println("  TSBH                            OBJBH     percent ");
        log.println("  USERBH                          RECOVER ");
        log.println("  TRAN                            BUSY      sid ");
        log.println("  CHAIN      sid                  SGASTAT ");
        log.println("  PGA                             LIBCACHE ");
        log.println("  ROWCACHE                        SYSSTAT   name ");
        log.println("  WAITSTAT                        SYSEVENT   ");
        log.println("  TSSTAT                          FILESTAT  tablespace ");
        log.println("  SESSTAT    sid  name            SEGSTAT   user ");
        log.println("  BLOCK      file block           DDLLOCK ");
        log.println("  BACKUP                          LOGHIS ");
        log.println("  TOP{EXE|GETS|READ|GET} count    WAIT      sid");
        log.println("  LOCK       sid                  OBJECT    objid");
        log.println("  LONGOPS                         TABLESPACE");
        log.println("  DATAFILE   tablespace           ARCLOG    scn");
        log.println("  LATCHWAIT                       CURSOR    sid");
        log.println("  GLOBAL     username             MOVEIND   owner.table tsname");
        log.println("  MOVETAB    owner.table tsname   SHARE     count");
        log.println("  UNDO       name                 UNDOHDR   ");
        log.println("  _PARAMETER name                 CHARSET   name");
        log.println("  SQLMEM     count                HASH      sql_hash_value");
        log.println("  HOLD       object_id            ACTIVESQL");
        log.println("  BLOCKING                        PXSTAT");
        log.println("  PX                              PXSESS");
        log.println("  PXPROCESS                       PQSLAVE");
        log.println("  XPLAN      sql_hash_value       SQLLIKE   pattern");
        log.println("  WSQL                            PLAN      sql_hash_value");
        log.println("  MACHINE                         KILLMACHINE machine");
        log.println("  KILLUSER   username             HOT");
        log.println("  KILLSQL    username             KILLHOLD  object_id");
        log.println("  TSTAT      owner.table          ISTAT     owner.table");
        log.println("  SIZE       owner.table          UNUSABLE  username");
        log.println("  INVALID    username             FREESPACE tsname");
        log.println("  FILEOBJ    tsname count         RESIZE    tsname");
        log.println("  NOLOGGING  username             OBJSQL    owner.object_name");
        log.println("  SPTOPSQL   days h1 h2 rows      SPSQL     hash days h1 h2");
        log.println("  SPSTAT     day1 day2  h1 h2     OBJGRANT  owner.object_name");
    }

    public static String loadSqlScript(String name, CommandLog log) {
        if (name == null || name.trim().isEmpty()) {
            log.println("Invalid keyword specified.");
            return null;
        }
        String sql;
        try {
            String path = "scripts" + JavaVM.FILE_SEPERATOR + name.toLowerCase() + ".sql";
            FileReader localFileReader = new FileReader(path);
            char[] arrayOfChar = new char[65536];
            int m = localFileReader.read(arrayOfChar);
            sql = String.valueOf(arrayOfChar, 0, m);
            localFileReader.close();
        } catch (IOException localIOException) {
            log.println("Invalid keyword " + name + " specified.");
            return null;
        }

        return sql;
    }
}

