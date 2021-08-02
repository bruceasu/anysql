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
public class SQLInvoker implements ModuleInvoker {
    PgSqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;
    public SQLInvoker(PgSqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd){
        DBRowCache rowCacheOfSessionWait;
        DBRowCache rowCacheOfSessionStats;
        long l1;
        long l2;
        executor.getObjectFromCommand(cmd.command.substring(0, Math.min(100, cmd.command.length() - 1)));
        out.println();
        rowCacheOfSessionWait = executor.getDbRowCacheOfSessionWait();
        rowCacheOfSessionStats = executor.getDbRowCacheOfSessionStats();
        l1 = System.currentTimeMillis();
        executor.executeSQL(executor.database, cmd, executor.sysVariable, out);
        l2 = System.currentTimeMillis();
        executor.printCost(l2, l1);
        executor.printSessionStats(rowCacheOfSessionStats);
        executor.printSessionWait(rowCacheOfSessionWait);
        executor.printExplain(cmd);
        out.println();

        return true;
    }
}
