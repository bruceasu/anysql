package com.asql.mysql.invoker;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.mysql.MySqlSQLExecutor;

/**
 * Created by suk on 2017/8/13.
 */
public class SQLInvoker implements ModuleInvoker {
    MySqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;
    public SQLInvoker(MySqlSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd){
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        executor.executeSQL(executor.getDatabase(), cmd, executor.getSysVariable(), out);
        endTime = System.currentTimeMillis();
        if (executor.isTiming()) {
            out.println("Execute time: " + DBOperation.getElapsed(endTime - startTime));
        }

        return true;
    }
}
