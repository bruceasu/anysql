AnySQL Tools
==========================


## 声明

这个别人开发的一个数据库（oracle）客户端，版权属于

    Lou Fangxin (http://www.anysql.net 【失效】)
    mail(dcba@itpub.net)
    qq(37223884)
   【来自MANIFEST.MF的信息】

作者是个DBA大牛（曾经开发了好多数据库工具，还获得oracle的奖项。曾供职于BAT，还创业，提供服务。）
本人没有他的目前有效联系方式，未能获得修改授权，鉴于他的这个软件是本来就是免费给大家使用（他写了好多好用的免费工具），
不以商业目的的反编译，修改，再次分发应该没有违反原作者的意愿。
原作者如果认为这样不合适，请联系本人停止再次分发。

这个软件都是10年前（2004）开发的，运行环境是jre6-32, 使用了不少sun的代码，已经不适用于jre7,jre8。
本案的目的是修改相关sun私有方法，以便能够在新的jre下运行，


## 手册


AnySQL为基于Java和JDBC开发的Oracle交互工具, 不需要安装数据库客户端。
如有企业需要定制的只读版本也可以联系我。
	
	
### AnySQL中的特色功能介绍(一)
[ASQL](http://www.anysql.net/anysql/anysql_new_feature01.html2006-09-13 "已失效")

    AnySQL可以不安装Oracle客户端, 也不需要进行客户端的配置,
而直接连接数据库, 如下所示:

     ASQL> conn system/oracle@localhost:1521:xe

      Database connected.

在AnySQL中集成了我平时收集的常用的SQL, 并做成方便的ORA命令,
方便使用, 功能强大. 如查询表空间使用情况的命令:

     ASQL> ora tsfree
     TABLESPACE FILES SIZE_MB FREE_MB MAXFREE PCT_USED PCT_FREE
     ———- —– ——- ——- ——- ——– ——–
     SYSAUX         1     430       3    2.44     99.3      0.7
     USERS          1     100   98.06   98.06     1.94    98.06
     SYSTEM         1     340    6.75    5.94    98.01     1.99
     UNDO           1      90   14.19       3    84.24    15.76
     4 rows returned.

在AnySQL中也不需要指定列的宽度, 输出中会计算最大的列的长度,
而不是根据列的定义中的长度来显示, 省去了一堆”COL … FORMAT …”的定制命令,
如查询数据库的数据文件时:

     ASQL> SELECT NAME,BYTES FROM V$DATAFILE;
     NAME                                  BYTES
     ——————————— ———
     C:\ORACLEXE\ORADATA\XE\SYSTEM.DBF 356515840
     C:\ORACLEXE\ORADATA\XE\UNDO.DBF    94371840
     C:\ORACLEXE\ORADATA\XE\SYSAUX.DBF 450887680
     C:\ORACLEXE\ORADATA\XE\USERS.DBF  104857600

还有其他的常用功能, 留在以后介绍.
	
### AnySQL中的特色功能介绍(二)
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature02.html2006-09-13 "已失效")

AnySQL的DESC命令不仅可以显示表结构, 还显示了表是否分区及其索引信息,
这是DBA在平常的数据库管理中是很实用的功能, 请看下面的例子:

    ASQL> desc scott.emp
    NO# NAME   NULLABLE   TYPE
    — ————————- ——– ————
    1 EMPNO                     NOT NULL NUMBER(4)
    2 ENAME                              VARCHAR2(10)
    3 JOB                                VARCHAR2(9)
    4 MGR                                NUMBER(4)
    5 HIREDATE                           DATE
    6 SAL                                NUMBER(7,2)
    7 COMM                               NUMBER(7,2)
    8 DEPTNO                             NUMBER(2)
    TYPE  ISUNQ  INDEX_NAME       NO# COLUMN_NAME DESCEND
    —— —— ————— — ———– ——-
    NORMAL UNIQUE PK_EMP            1 EMPNO       ASC
    PARTITIONED AVG_ROW_LEN NUM_ROWS BLOCKS EMPTY_BLOCKS NO
    ———– ———– ——– —— ————


在对数据库对象进行某些操作(如Rename, Drop等)之前,
先看一下对象的依赖关系是一个很好的习惯,
它会告诉你这个对象依赖于那些对象, 及哪些对象依赖于将要操作的对象。
如果过程或视图创建失败, 用这个功能也可以帮你快速定位失败原因。

    ASQL> DEPEND SYS.DBMS_SUMMARY
    Reference:
    TYPE         D_OWNER D_NAME           D_TYPE
    DEPEND
    ———— ——- —————- ———— ——
    PACKAGE BODY SYS     STANDARD         PACKAGE      HARD
    PACKAGE      SYS     STANDARD
    PACKAGE      HARD
    PACKAGE BODY SYS     DBMS_SUMMARY     PACKAGE      HARD
    PACKAGE BODY PUBLIC  DBMS_OLAP        SYNONYM      HARD
    PACKAGE BODY SYS     DBMS_SUMREF_UTIL PACKAGE      HARD
    PACKAGE BODY SYS     DBMS_SUMADVISOR  PACKAGE      HARD
    PACKAGE BODY SYS     DBMS_OLAP        NON-EXISTENT HARD
    Referenced By:
    TYPE    R_OWNER R_NAME       R_TYPE       DEPEND
    ——- ——- ———— ———— ——
    PACKAGE PUBLIC  DBMS_SUMMARY SYNONYM      HARD
    PACKAGE PUBLIC  DBMS_OLAP    SYNONYM      HARD
    PACKAGE SYS     DBMS_SUMMARY PACKAGE BODY HARD

在我的其他工具中, 经常会输出一个SQL的哈希值, 可以用下面这个自定义命令看SQL的文本:

    ASQL> ora hash 3109775760
    SELECT /* AnySQL */ SQL_TEXT “SQL Executing”
    FROM V$SQLTEXT_WITH_NEWLINES
    WHERE HASH_VALUE = TO_NUMBER(:1)
    ORDER BY PIECE

更多的功能, 请等下一篇介绍.
	
### AnySQL中的特色功能介绍(三)
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature03.html2006-09-15)

在AnySQL中实现了一些在SQL*Plus中不太容易实现的功能, 如查看视图或过程的代码,
查看建表的语法(Beta版), 查看某个表及其索引等对象的大小等。
下面再介绍几个比较好的功能:
在我的测试用户下有这样的几个对象:

    ASQL> select * from tab;
    TNAME                   TABTYPE CLUSTERID
    ———————– ——- ———
    A_V                     VIEW
    T_HASH                  TABLE
    T_LOB                   TABLE
    T_LONG                  TABLE
    11 rows returned.

下面我们来看如何查看视图的定义, 查过程的定义就自已去试试了:

    ASQL> source anysql.a_v
    select “TNAME”,”TABTYPE”,”CLUSTERID” from tab

查看建表的语法, 这个功能的输出仅供参考, 请不要用这个功能来拷贝表结构:

    ASQL> source anysql.t_lob
    CREATE TABLE ANYSQL.T_LOB
    (
       FNAME VARCHAR2(20) ,
       FTEXT CLOB
    )
    TABLESPACE USERS INITRANS 1 PCTFREE 10
    STORAGE ( FREELIST GROUPS 1 FREELISTS 1)
    MNOCACHE LOGGING
    /

查看表的大小:

    ASQL> ora size t_hash
    OWNER  SEGMENT_NAME SEGMENT_TYPE      SIZE_MB INIEXT MAXEXT
    —— ———— ————— ——— —— ——
    ANYSQL T_HASH       TABLE PARTITION       0.5  16384
    ANYSQL IDX_T_HASH   INDEX PARTITION 0.4296875  16384  40960
    2 roMws returned.

查看LONG/LONG RAW字段的内容, COL1为LONG字段, 输出的第一个字段为字节数, 第二个字段为KB:

    ASQL> LOBLEN SELECT COL1 FROM T_LONG;
    63521,62

更多功能在以后介绍。
	
### AnySQL中的特色功能介绍(四)
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature04.html2006-09-20)

在Oracle的调优中, 查看SQL的执行计划是非常重要和常用的方法,
在AnySQL中提供了几个方便地查看SQL执行计划的方法, 让我们来体验一下。

1.  第一种情况是获得一个SQL语句的执行计划, 可以用EXPLAIN PLAN命令,
    这里的命令稍不同于SQL*Plus中的, 没有”FOR”关键字.

        ASQL> EXPLAIN PLAN SELECT * FROM T_HASH;
        SQLPLAN                                    COST  CARD KBYTE PS PE
        —————————————— —- —– —– — –
        0     SELECT STATEMENT Optimizer=RULE      20 10000    59
        1   0   PARTITION HASH (ALL)                              1  8
        2   1     TABLE ACCESS (FULL) OF T_HASH    20 10000    59 1  8

2.  第二种情况是知道一个SQL的哈希值(Hash Value),
    然后从V$SQL_PLAN中的执行计划(正在使用的), 可以使用定制命令
    ”ORA PLAN 哈希值”来完成.

        ASQL> ORA PLAN 821132411
        SQLPLAN                                   COST  CARD KBYTE PS PE
        —————————————– —- —– —– — –
        0     SELECT STATEMENT Optimizer=RULE   20
        1   0   PARTITION HASH (ALL)                             1  8
        2   1     TABLE ACCESS (FULL) OF T_HASH   20 10000    59 1  8

        3 rows returned.

3.   第二种情况是知道一个SQL的哈希值(Hash Value),
然后希望运行EXPLAIN PLAN命令来在当前环境下重新生成执行计划,
可以使用定制命令”ORA XPLAN 哈希值”来完成, 这个命令会首先打印现SQL语句,
然后显示它的执行计划.

        ASQL> ORA XPLAN 821132411
        SELECT * FROM T_HASH
        SQLPLAN                                    COST  CARD KBYTE PS PE
        —————————————— —- —– —– — –
        0     SELECT STATEMENT Optimizer=RULE      20 10000    59
        1   0   PARTITION HASH (ALL)                              1  8
        2   1     TABLE ACCESS (FULL) OF T_HASH    20 10000    59 1  8

在STATSPACK或AWR中一般不会显示完整的SQL语句, 但肯定会告诉你一个哈希值,
这时你就可以方便地使用这些命令来进行调优了, 当然这些命令只是提供一些方便而已。

### AnySQL中的特色功能介绍(五) — 更新插入LONG/LOB
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature05.html2006-09-21)

在AnySQL中可以比较方便地操作LONG/LONG RAW类型, 将客户端文件插入或更新到这些字段中,
这是SQL*Plus没有办法做到的。
首建来建一个包括一个LONG字段的测试表, 如下所示:

    ASQL> DESC T_LONG
    NO# NAME
    NULLABLE TYPE
    — —————- ——– —-
    1 COL1                      LONG

接下来我们来插入一个Shell文件到一条新的记录中, 你也可以用Update语句来
更新一个LONG/LONG RAW字段, 只需要声明一个CLOB(更新LONG)或BLOB(更新LONG RAW)类型的
AnySQL宿主变量(Host Vairable), 并赋给一个文件名作为值, 在Insert/Update语句中直接引用就可以了,
如下所示:

    ASQL> VAR P_LONG CLOB
    ASQL> define p_long=otop
    ASQL> insert into t_long values (:p_long);
    1 rows affected.
    ASQL> commit;

然后在AnySQL直接SELECT查看内容就可以了, 可以最多显示64KB的内容:

    ASQL> select col1 from t_long;
    COL1
    ————————————————————
    #!/bin/sh
    if [ "A${ORACLE_HOME}A" = "AA" ]; then
       echo “ORACLE_HOME environment variable not setted.”
       exit
    fi
    if [ "A${LD_LIBRARY_PATH}A" = "AA" ];then
       LD_LIBRARY_PATH=/lib:/usr/lib
    fi
    if [ -d ${ORACLE_HOME}/lib32 ]; then
       LD_LIBRARY_PATH=${ORACLE_HOME}/lib32:${ORACLE_HOME}/lib:${LD_LIBRARY_PATH}
    else
       LD_LIBRARY_PATH=${ORACLE_HOME}/lib:${ORACLE_HOME}/lib64:${LD_LIBRARY_PATH}
    fi
    export LD_LIBRARY_PATH
    dcba.bin -1 “$1″ “$2″ “$3″ “$4″ “$5″ “$6″ “$7″ “$8″ “$9″
    1 rows returned.

AnySQL对于某些字符集的不文支持不好, 如US7ASCII, E8ISO8859P1等单字节字符集, 请在自已的库上验证后使用.
	

### AnySQL中的特色功能介绍(六)
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature06.html2006-09-30)

很多时侯我们需要进行行列转换进行显示, 如下面的例子所示,
要查所有用户下的表和索引的个数时, 下面的输出是不是更容易看懂呢?

    ASQL> CROSS SELECT OWNER,OBJECT_TYPE,COUNT(*)
        2 FROM DBA_OBJECTS
        3 WHERE OBJECT_TYPE IN (‘TABLE’,'INDEX’)
        4 GROUP BY OWNER,OBJECT_TYPE;
    OWNER        INDEX TABLE
    ———— —– —–
    TSMSYS           1     1
    ANYSQL                 1
    SYS            695   678
    OUTLN            3     3
    SYSTEM         179   141
    XDB             51    36
    FLOWS_FILES      4     1
    DBSNMP          10    21
    HR              19     8
    FLOWS_020100   364   164
    MDSYS           39    37
    CTXSYS          46    37
    12 rows returned.

当数据库中遇到块坏时, 首先要确定这个坏块是属于那个对象的,
在AnySQL中可以用定制命令”ORA BLOCK 文件号 块号”来进行查询。

    ASQL> ora block 1 9
    OWNER NAME   PARTITION TYPE     TABLESPACE
    —– —— ——— ——– ———-
    SYS   SYSTEM           ROLLBACK SYSTEM
    1 rows returned.

数据库中的主键和外键表明了一种表之间的父子联系, 如何在命令行查这层关系呢?
在AnySQL中的”SHOW PARENT/CHILD”命令可以告诉你结果。

    ASQL> show parent system.mview$_adv_level
    FKNAME              TNAME                 PKNAME
    ——————- ——————— —————–
    MVIEW$_ADV_LEVEL_FK SYSTEM.MVIEW$_ADV_LOG MVIEW$_ADV_LOG_PK
    ASQL> show child system.mview$_adv_level
    PKNAME              TNAME                    FKNAME
    ——————- ———————— ———————
    MVIEW$_ADV_LEVEL_PK SYSTEM.MVIEW$_ADV_ROLLUP MVIEW$_ADV_ROLLUP_CFK
    MVIEW$_ADV_LEVEL_PK SYSTEM.MVIEW$_ADV_ROLLUP MVIEW$_ADV_ROLLUP_PFK

更多功能还在发现中, 如果你需要一些特定的功能, 告诉我, 我也可以集成进去。
	
### AnySQL中的特色功能介绍(七) — 查询LONG/LOB列
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature07.html2006-11-08)

常看到有人问如何查看LOB或LONG类型的字段中的内容, 在SQL*Plus中是不容易的,
在AnySQL小工具中, 我设计了LOB和LOBEXP命令来对这些数据类型进行查询操作,
这些命令都将LONG/LOB字段中的内容检索到客户端的机器(非服务器端).

    LOB命令语法, 这里面query应当只返回一行及一列(LONG或LOB类型).
    Usage:
      LOB query >> file
    Note :
      >> mean export long/long raw/blob/clob to a file
        LOBEXP命令语法, 这里面query应当只返回两列, 第一列为文件名, 第二列为LONG或LOB字段.
    Usage:
      LOBIMP query
    Note :
      Query should return tow column as following:
      col1 : CHAR or VARCHAR specify the filename.
      col2 : blob/clob field.

下面来看一下例子中用到的表:

    ASQL> SELECT FNAME FROM T_LOB;
    FNAME
    —–
    a.txt
    otop
    2 rows returned.
    ASQL> desc t_LOB;
    NO# NAME              NULLABLE TYPE
    — —————– ——– ————
      1 FNAME                      VARCHAR2(20)
      2 FTEXT                      CLOB

用LOB命令的例子:
    ASQL> LOB SELECT FTEXT FROM T_LOB WHERE FNAME=’otop’ >> otop.txt;
    Command succeed.

用LOBEXP命令的例子:

    ASQL> LOBEXP SELECT FNAME, FTEXT FROM T_LOB;
    Write to file: a.txt , bytes=37
    Write to file: otop , bytes=483
    Command succeed.

现在你可以在运行AnySQL的机器上去看一下有没有新文件被生成? 查询LOB就是这么简单!
	

### AnySQL中的特色功能介绍(八) — 更新LONG/LOB列
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature08.html2006-11-15)

常看到有人问如何更新LOB或LONG类型的字段中的内容, 在SQL*Plus中是不容易的,
在AnySQL小工具中, 除了可以用LOB类型的主机变量, 我设计了LOB和LOBIMP命令
来对这些数据类型进行查询操作, 这些命令可以将运行AnySQL的机器(非服务器端)上的
文件上传更新到LONG/LOB字段中.

    LOB命令语法, 这里面query应当只返回一行及一列(LONG或LOB类型),
    在SELECT中应当加上”FOR UPDATE”子句表示锁定LOB进行更新.
    Usage:
      LOB query << file
    Note :
      << mean import a file to blob/clob field, the query
        should include the for update clause
    LOBIMP命令语法, 这里面query应当只返回两列, 第一列为文件名, 第二列为LONG或LOB字段,
    在SELECT中应当加上”FOR UPDATE”子句表示锁定LOB进行更新.
    Usage:
      LOBIMP query
    Note :
      Query should return tow column as following:
      col1 : CHAR or VARCHAR specify the filename.
      col2 : blob/clob field.

下面来看一下例子中用到的表:

    ASQL> SELECT FNAME FROM T_LOB;
    FNAME
    —–
    a.txt
    otop
    2 rows returned.
    ASQL> desc t_LOB;
    NO# NAME              NULLABLE TYPE
    — —————– ——– ————
      1 FNAME                      VARCHAR2(20)
      2 FTEXT                      CLOB

我们将”FTEXT”字段更新成空的CLOB值, 在使用这两个命令时,
要确保LOB字段中的值不为NULL, 如下所示:

    ASQL> update t_lob set ftext=empty_clob();
    2 rows affected.
    ASQL> commit;
    Commit Succeed.

下面用LOB命令来更新一个值:

    ASQL> LOB SELECT FTEXT FROM T_LOB WHERE FNAME=’a.txt’ FOR UPDATE << a.txt;
    Command succeed.
    ASQL> commit;
    Commit Succeed.

用LOBIMP的例子:
    ASQL> LOBIMP SELECT FNAME,FTEXT FROM T_LOB FOR UPDATE;
    File a.txt loaded.
    File otop loaded.
    Command succeed.
    ASQL> commit;
    Commit Succeed.

现在检查一下数据库中的值是不是已经被更新了?
这个功能请使用Oracle 10g中带的JDBC驱动(ojdbc14.jar).
	
### AnySQL中的特色功能介绍(九) — ORA OBJSQL
[AnySQL](http://www.anysql.net/anysql/anysql_new_feature09.html2007-02-02)

Oracle 9i以后多了个视图V$SQL_PLAN, 这个视图可以用于显示当前正在使用的
SQL的执行计划, 其中有两列OBJECT_OWNER和OBJECT_NAME, 指的是执行计划中会访问到的对象,
包括表及索引等. 这个视图是很有用的, 除了看正在使用的执行计划外,
还可以用来看那些SQL在访问某个表或索引, 在你将要删除一个认为不重要的索引以前,
不防先看一下有没有SQL在用这个索引, 然后去看一下这个SQL的执行情况.
“ORA OBJSQL”这个自定义命令就是为这个用途设计的.

下面是一个使用的例子:

    ASQL> ora objsql anysql.t_HASH
    0 rows returned.
    ASQL> SELECT COUNT(*) FROM ANYSQL.T_HASH;
    COUNT(*)
    ——–
        7485
    1 rows returned.
    ASQL> ora objsql anysql.t_HASH
    0 rows returned.

为什么这个取记录数的SQL没有显示出来呢, 是因为这个表上面有一个唯一索引,
刚好索引的列是定义为非空的, 所以这个SQL去走索引了,
我们将”ORA OBJSQL”中的对象名换为索引的名字试试。

    ASQL> ora objsql anysql.IDX_T_HASH
    HASH_VALUE VERS SORTS EXECS READS GETS ROWCNT
    ———- —- —– —– —– —- ——
    606471602    0     0     1    39  199      1
    1 rows returned.
    ASQL> ORA HASH 606471602
    SELECT COUNT(*) FROM ANYSQL.T_HASH

要写这出个后面的SQL其实很简单, 不需要我在这儿贴出来了。
	
### 最近收到的关于AnySQL软件的两封邮件
[AnySQL](http://www.anysql.net/anysql/anysql_two_mails.html2006-10-08)

在10月4号收到的邮件, 关于在AnySQL中如何操作BLOB/CLOB类型的问题,
看来我得写写关于AnySQL工具的英文文档了.

> To whom it may concern,
>
> I’m really interested in using anysql for generating blob to a file, but I figured out that there is no howto in using it. Would it be possible if you explain it and write it in your blog.
> Thanks in advance,

上面的邮件我是在10月5号看的, 却发现了另一封邮件, 说他已经搞明白如何操作BLOB/CLOB类型,
于是我又不急着写英文文档了.

> Thank you so much but I’ve been able to find my way out to use anysql. I will try using lobs later as anysql is good enough for now. Thanks for making such a great tool.

这些老外的功夫就是好, 没有文档也能搞懂! 中文的我已经写了.
	
	
### 如何将BLOB的内容转到LONG RAW中
[AnySQL](http://www.anysql.net/anysql/blob_to_longraw.html2006-12-12)

PL/SQL基本上对于LONG RAW和BLOB无能为力, 对于这两种需要自已写程序来实现。
在AnySQL中就可以将BLOB的内容转换成LONG RAW, 下面是一个例子. 先来建一张测试表:

    ASQL> SET QUERYONLY FALSE
    ASQL> CREATE TABLE T_BLOB (ID NUMBER NOT NULL, IMAGE BLOB);
    Create Table Succeed.
    ASQL> VAR P_IMAGE BLOB
    ASQL> DEFINE P_IMAGE=ASQL.EXE
    ASQL> INSERT INTO T_BLOB VALUES (1, :P_IMAGE);
    1 rows affected.
    ASQL> DEFINE P_IMAGE=ASQLW.EXE
    ASQL> INSERT INTO T_BLOB VALUES (2, :P_IMAGE);
    1 rows affected.
    ASQL> commit;
    Commit Succeed.

用LOBEXP命令将BLOB中的内容导出成一个个操作系统文件:

    ASQL> LOBEXP SELECT ‘IMAGE_’||ID||’.EXE’, IMAGE FROM T_BLOB;
    Write to file: IMAGE_2.EXE , bytes=101376
    Write to file: IMAGE_1.EXE , bytes=101376
    Command succeed.

将表数据导出, 相应的BLOB字段替换为导出的操作系统文件名:

    ASQL> UNLOAD -h off SELECT ID,’IMAGE_’||ID||’.EXE’ FROM T_BLOB >> T_BLOB.TXT;
    Query executed in 00:00:00.330
    2            rows writed in 00:00:00.000
    ASQL> host cat t_blob.txt
    2|IMAGE_2.EXE
    1|IMAGE_1.EXE

创建一个LONG RAW的表:

    ASQL> CREATE TABLE T_LONGRAW (ID NUMBER NOT NULL, IMAGE LONG RAW);
    Create Table Succeed.

创建一个buffer, 结构和导出的文件相同:

    ASQL> buffer reset
    Command completed.
    ASQL> buffer add P_ID INTEGER
    Command completed.
    ASQL> buffer add P_IMAGE BLOB
    Command completed.

用LOAD命令来将数据装载到LONG RAW表中:

    ASQL> LOAD INSERT INTO T_LONGRAW VALUES (:P_ID,:P_IMAGE) << T_BLOB.TXT;
    Command Completed.
    2 rows loaded!

用LOBLEN命令来检查一下装入的LONG RAW字段的长度:

    ASQL> LOBLEN SELECT IMAGE FROM T_LONGRAW;
    101376,99
    101376,99

说明已经成功地将BLOB内容装入到LONG RAW表中, 此例中, 需要将AnySQL的jlib目录下的
Oracle JDBC驱动换成10g的版本, 否则不允许更新LOB列.
	
### 在AnySQL中如何增加自定义SQL命令
[AnySQL](http://www.anysql.net/anysql/anysql_cust_command.html2007-01-02)

AnySQL的ORA命令中收集了我认为常用的一些SQL, 不过他们都是写死在程序中的,
因此要增加一个SQL时需要重新编译程序, 这样会比较麻烦, 今天做了一些改进,
以便可以增加自定义命令, 这个方法是通过增加了一个”scripts”目录,
然后将需要的SQL保存到.sql文件放入那个目录就可以了。

那么如何调用自定义的SQL呢? 我们来看一下ora命令的格式:

    ORA keyword [V1] [V2] [Vn]
    其中:
        keyword为自定义功能的名称, 如果这个关键字没有被我收录, 则调用scripts目录下找keyword.sql(小写)文件.
        V1, V2, Vn为传入自定义SQL的参数, 总为字符类型
    需要说明的是在SQL中可以引用变量名(:变量名), 如”SELECT * FROM TAB WHERE TNAME LIKE = :V1″。
    在数据库中我们经常会用”owner.tablename”的形式, 因此每一个变量都又分解成两个子变量(:Vn_OWNER和:Vn_NAME),
    分别表示用户名和对象名, 也就是每一个关键字后面的单词都有三个变量可以引用, 例如:
        SELECT :V1, :V1_OWNER, :V1_NAME, :V2, :V2_OWNER,:V2_NAME FROM DUAL
    我们将这个SQL语句存成test.sql并放到scripts目录下, 然后就可以调用了:
    ASQL> ora test a.b c.d
    :1  :2 :3 :4  :5 :6
    — — – — — –
    a.b a  b  c.d c  d
    1 rows returned.

在ora命令中所有的SQL语句都是可以用绑定变量的, 让我们来看一下真实的SQL,
是我用来显示某个表的统计信息的.

    SELECT /* AnySQL */
       OWNER,NULL PARTNAME, INITIAL_EXTENT/1024 INIEXT,
       NEXT_EXTENT/1024 NXTEXT, NUM_ROWS NROWS, BLOCKS,
       AVG_SPACE AVGSPC,CHAIN_CNT CCNT, AVG_ROW_LEN ROWLEN,
       SAMPLE_SIZE SSIZE,LAST_ANALYZED ANADATE
    FROM ALL_TABLES
    WHERE UPPER(OWNER)=NVL(UPPER(:V1_OWNER),OWNER)
      AND TABLE_NAME=UPPER(:V1_NAME)
    UNION ALL
    SELECT /* AnySQL */
       TABLE_OWNER OWNER,PARTITION_NAME PARTNAME,
       INITIAL_EXTENT/1024 INIEXT,   NEXT_EXTENT/1024 NXTEXT,
       NUM_ROWS NROWS, BLOCKS, AVG_SPACE AVGSPC, CHAIN_CNT CCNT,
       AVG_ROW_LEN ROWLEN, SAMPLE_SIZE SSIZE,LAST_ANALYZED ANADATE
    FROM ALL_TAB_PARTITIONS
    WHERE UPPER(TABLE_OWNER)=NVL(UPPER(:V1_OWNER),TABLE_OWNER)
      AND TABLE_NAME=UPPER(:V1_NAME)

运行结果如下:

    OWNER  PARTNAME INIEXT NXTEXT NROWS BLOCKS AVGSPC CCNT ROWLEN SSIZE ANADATE
    —— ——– —— —— —– —— —— —- —— —– ———-
    SYSTEM              64            1      1      0    0     11     1 2006-04-17

### 在AnySQL中如何找出锁的拥有者
[AnySQL](http://www.anysql.net/anysql/anysql_lock_holder.html2007-02-26)

在比较忙的系统中我常用一段处理Resource Busy的角本来对表作DDL操作，
但还是常遇到久久不成成功的情况, 这时就要去看是那个会话一直锁住了我要修改的表，
我常用AnySQL中的两个命令来完成这个功能。 如下所示:

    ASQL> list object t_long
    TYPE      ID OWNER  OBJECT_NAME CREATED    MODIFIED   STATUS
    —– —— —— ———– ———- ———- ——
    TABLE 538638 ANYSQL T_LONG      2006/08/02 2007/01/25 VALID
    1 rows returned.
    ASQL> ora hold 538638
    SID SERIAL# SPID USERNAME MACHINE STATUS   PROGRAM
    — ——- —- ——– ——- ——– —————-
    529   62199 44   ANYSQL   frisket INACTIVE sqlplus@frisket
    1 rows returned.

在找出会话之后, 就可以进一步分析, 是否可以将这个会话杀掉。事实上这一招我们是常用的。
	
### 向MySQL学习, AnySQL可以纵向显示结果记录
[AnySQL](http://www.anysql.net/anysql/anysql_form_display.html2007-07-26)

Huang Yong在接触MySQL后, 一直建议我在AnySQL中增加这个功能,
不过这一两个月来没有理他, 一直没有加上此功能, 今天好象良心发现, 加上去吧!
毕竟他现在坐得离我比较近。

    ASQL> select * from tab where rownum < 5;
    TNAME      TABTYPE CLUSTERID
    ———- ——- ———
    BONUS      TABLE
    CLU_A      CLUSTER
    CR_5043802 TABLE
    DEPT       TABLE
    4 rows returned.

接下来要从从USER_OBJECTS中去查某个表的信息, 可是列太多了, 横向显示不方便啊!
你可以查询语句后面加上”/g”,”/G”,”\G”,”\g”中的任何一个, 将结果变为纵向显示。
如下所示:

    ASQL> select * from user_objects where object_name=’BONUS’/g;
    OBJECT_NAME                   : BONUS
    SUBOBJECT_NAME                : null
    OBJECT_ID                     : 637961
    DATA_OBJECT_ID                : 637961
    OBJECT_TYPE                   : TABLE
    CREATED                       : 2007-05-16 22:03:27.0
    LAST_DDL_TIME                 : 2007-05-16 22:03:27.0
    TIMESTAMP                     : 2007-05-16:22:03:27
    STATUS                        : VALID
    TEMPORARY                     : N
    GENERATED                     : N
    SECONDARY                     : N
    1 rows returned.

想用这个功能的, 下载更新吧!
	
### AnySQL的SQL Server / Sybase版
[AnySQL](http://www.anysql.net/anysql/anysql_for_mssql_sybase.html2008-01-17)

AnySQL的Oracle版本, 自写自用, 感觉很好很强大, 可是还没有其他数据库的版本，
总是愧对Any这个字啊。昨天装了SQL Server Express之后, 发现没有适合我的命令行工具，
习惯了Oracle的SQL*Plus一样的界面后, 用起osql及sqlcmd总感觉得十分不方便，
常常在一个SQL语句后面打上分号, 也常常另起一行敲入反斜杆来执行命令，
可这些在SQL Server的工具里都不灵了。

还是打造一个Oracle风格的命令行工具吧, 推出AnySQL的SQL Server/Sybase版,
这两种数据库是从同一个猴子进化而来的, 因此将他们放在一起好了。
命令行界面如下:

    C:\AnySQL>asql –mssql
    AnySQL for SQL Server/Sybase, version 2.0.0
    (@) Copyright Lou Fangxin, all rights reserved.
    ASQL>

接下来就可以用分号来执行SQL了, 而不需要另起一行go了。

    ASQL> select * from t_test;
    col1
    —-
      10
    1 rows returned.

另起一行用反斜杆也一样, 很附合我的习惯.
    ASQL> select * from t_test
        2 /
    col1
    —-
      10
    1 rows returned.

当然原来的go还是支持的.

    ASQL> select * from t_test
        2 go
    col1
    —-
      10
    1 rows returned.

基本的功能已经有了, LOB操作的功能也有了, 只有SHOW, LIST还没有加上,
因为对SQL Server不懂, 自定功命令也没有加上. 用Micrsoft自已的JDBC有点问题,
最后用了jTDS驱动.

目标是让Oracle用户在休闲时间用AnySQL去连接SQL Server或Sybase.

	AnySQL的老大，为什么不回答一下偶提的技术性问题呢？
	真的不知道SQL Server版本的AnySQL的connect命令的格式是啥呀？给个提示吧？是像Oracle这样吗：
	ASQL> connect ansql/ansql@localhost:1521
	anysql Says:
	2008-01-30 at 18:33
	准备改成那样, 但还没有改, 现在是:
	SQL Server: mssql host:port user passwd
	Sybase: sybase host:port user passwd
	

### 能在SQL Server上做点事了
[AnySQL]{http://www.anysql.net/anysql/anysql_sql_server_features.html2008-01-18}

经过几个小时的努力, AnySQL连上SQL Server后已经能做点事了, 比如, 操作Text和Image字段,
和操作Oracle的LONG/LONG RAW是一码子事, 不知道SQL Server有没有BLOB/CLOB类型?
有的话也能操作, 比如将内容取出来, 或将一个文件塞进去. 不用bcp也可以将数据导出成文本文件,
或导入文本文件中的数据到表里. 当然如果是Sybase数据库, 我也一样能干这些活了。
如果有人问如何将SQL Server或Sybase数据中存放的图片文件移到Oracle或相反, 则可以联系我看看。

不过基本的功能还没有做好, 如:

1.  Oracle中的DESCRIBE命令, sp_columns或sp_help的输出实在是看不习惯。
2.  如何在字符界面下查看一个SQL语句的执行计划, 也有Explain Plan语句吗?
3.  如何列出当前活动的会话, 在执行什么SQL, 在等什么资源? sp_who的输出中没有这些, sp_lock能反映出锁的等待关系吗?
4.   如何找出Top SQL, 如逻辑读最多的? 物理读最多的? 等等.

更多的功能正等着你来提呢, 无论你是用Oracle的还是用SQL Server或Sybase的,
如果还有时间和精力, 也搞搞SUN MySQL(收购后改名为或出一个SunSQL, 也不错)版本。
才过了7年以前积累的SQL Server相关知识就全忘了, 看看天下有些奇人,
脑容量居然比Google还大, 搜索比Google还快。

### 在Linux/Unix下如何使用AnySQL
[AnySQL](http://www.anysql.net/anysql/anysql_in_unix.html2006-09-15)

AnySQL是用Java写的, 因此也可以在Unix/Linux下运行, 在这儿下载的是Windows下的,
如何搬到Unix/Linux下呢? 首先你要先在Unix/Linux下有JRE1.4或以上的版本,
然后按照以下步骤去做就可以了:

1. 确定安装目录, 如/usr/AnySQL.
2. 建一个jlib的子目录, 如/usr/AnySQL/jlib.
3. 将Windows下的oasql.jar和oracle.jar拷到jlib目录, 如拷贝到/usr/AnySQL/jlib
4. 编辑一个名称为asql的Shell文件, 以方便地运行AnySQL.
5. 将安装目录放到PATH路径中, 这样在任何目录运行asql就启动了AnySQL.

启动AnySQL的Shell文件的内容为(我目前使用的):

    #!/bin/sh
    ASQL_CMD=`which $0`
    ASQL_HOME=`dirname $ASQL_CMD`
    $JAVA_HOME/bin/java -server -Xms8m -Xmx16m \
        -cp $ASQL_HOME/jlib/oasql.jar com.asql.tools.ASQL $*

这样不需要安装数据库客户端了, 也不一定要运行在图形模式下。欢迎你们在Unix/Linux下使用。

### 更安全的AnySQL只查询(Query Only)模式
http://www.anysql.net/anysql/anysql_query_only.html 2006-09-13

又在ITPub上看到有人删除表了, 除了进行较好的权限控制之外,
我还得介绍一下AnySQL工具中的只查询(Query Only)模式。
默认起动AnySQL时就进行了只查询模式, 在这种模式下不能做DML, 也不能作DDL,
这样就安全多了, 我现在经常用它来进行查询了.
除了这个安全功能外, 还不需要进定义列显示的长度等等格式设定, 还是不错的.
下面我们来看一下这个功能的例子, 首先来连接到数据库:

    ASQL> conn anysql/anysql@localhost:1521:TEST
    Database connected.
    ASQL> select * from tab;
    TNAME TABTYPE CLUSTERID
    ———————– ——- ———
    A_SEQ_SYN              SYNONYM
    T_LOB                  TABLE
    T_LONG                  TABLE
    T_LONGRAW              TABLE

    12 rows returned.

接下来就可以测试这个功能, 也介绍了如何切换到可更新模式:

    ASQL> drop SYNONYM A_SEQ_SYN;
    Query Only mode, DML/DDL/Script disabled.!
    ASQL> set queryonly false
    ASQL> drop synonym A_SEQ_SYN;
    Drop Synonym Succeed.
    ASQL> set queryonly true
    ASQL> drop table t_lob;
    Query Only mode, DML/DDL/Script disabled.!
    ASQL> delete t_lob;
    Query Only mode, DML/DDL/Script disabled.!
    ASQL> alter table t_lob add col100 clob;
    Query Only mode, DML/DDL/Script disabled.!


