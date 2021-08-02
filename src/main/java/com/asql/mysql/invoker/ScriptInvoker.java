package com.asql.mysql.invoker;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.mysql.MySqlSQLExecutor;

/**
 *
 * @author suk
 * @date 2017/8/13
 */
public class ScriptInvoker implements ModuleInvoker {
    MySqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;
    public ScriptInvoker(MySqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        executor.executeScript(executor.database, cmd, executor.sysVariable, out);
        endTime = System.currentTimeMillis();
        if (executor.timing) {
            out.println(" Execute time: " + DBOperation.getElapsed(endTime - startTime));
        }

        return true;
    }
}
