package com.asql.pgsql.invoker;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.DBRowCache;
import com.asql.core.ModuleInvoker;
import com.asql.core.log.CommandLog;
import com.asql.pgsql.PgSqlSQLExecutor;

/**
 *
 * @author suk
 * @date 2017/8/13
 */
public class ScriptInvoker implements ModuleInvoker {
    PgSqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;
    public ScriptInvoker(PgSqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        executor.getObjectFromCommand(cmd.command.substring(0, Math.min(100, cmd.command.length() - 1)));
        out.println();
        DBRowCache rowCacheOfSessionWait = executor.getDbRowCacheOfSessionWait();
        DBRowCache rowCacheOfSessionStats = executor.getDbRowCacheOfSessionStats();
        long start = System.currentTimeMillis();
        executor.executeScript(executor.database, cmd, executor.sysVariable, out);
        long end = System.currentTimeMillis();
        executor.printCost(end, start);
        executor.printSessionStats(rowCacheOfSessionStats);
        executor.printSessionWait(rowCacheOfSessionWait);
        out.println();

        return true;
    }
}
