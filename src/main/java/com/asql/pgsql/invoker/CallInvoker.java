package com.asql.pgsql.invoker;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.DBRowCache;
import com.asql.core.ModuleInvoker;
import com.asql.core.log.CommandLog;
import com.asql.pgsql.PgSqlSQLExecutor;

/**
 * Created by suk on 2017/8/13.
 */
public class CallInvoker implements ModuleInvoker {
    PgSqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;

    public CallInvoker(PgSqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        DBRowCache rowCacheOfSessionWait;
        DBRowCache rowCacheOfSessionStats;
        out.println();
        rowCacheOfSessionWait = executor.getDbRowCacheOfSessionWait();
        rowCacheOfSessionStats = executor.getDbRowCacheOfSessionStats();
        long start = System.currentTimeMillis();
        executor.executeCall(executor.database,
                new Command(cmd.type1, cmd.type2, executor.skipWord(cmd.command, 1)),
                executor.sysVariable,
                out);
        long end = System.currentTimeMillis();
        executor.printCost(end, start);
        executor.printSessionStats(rowCacheOfSessionStats);
        executor.printSessionWait(rowCacheOfSessionWait);
        out.println();
        return true;
    }
}
