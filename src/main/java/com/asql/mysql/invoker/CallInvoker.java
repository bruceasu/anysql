package com.asql.mysql.invoker;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.mysql.MySqlSQLExecutor;

/**
 * Created by suk on 2017/8/13.
 */
public class CallInvoker implements ModuleInvoker {

    MySqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;

    public CallInvoker(MySqlSQLExecutor executor) {
        this.executor = executor;
        out           = executor.getCommandLog();
        cmdType       = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        executor.executeCall(executor.database, new Command(cmd.type1, cmd.type2, executor.skipWord(cmd.command, 1)),
                executor.sysVariable, out);
        endTime = System.currentTimeMillis();
        if (!executor.timing) {
            return true;
        }
        out.println("Execute time: " + DBOperation.getElapsed(endTime - startTime));
        return true;
    }
}
