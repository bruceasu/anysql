package com.asql.pgsql.invoker;

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.ModuleInvoker;
import com.asql.core.log.CommandLog;
import com.asql.pgsql.PgSqlSQLExecutor;
import java.util.function.Function;

/**
 * Created by suk on 2017/8/13.
 */
public class DBCommandInvoker implements ModuleInvoker {

    PgSqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;

    public DBCommandInvoker(PgSqlSQLExecutor executor) {
        this.executor = executor;
        out           = executor.getCommandLog();
        cmdType       = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        int  k = executor.getDBCommandID(cmd.command);
        // TODO: process the kth command
        return true;
    }


    boolean time(Command cmd, Function<String, Boolean> function) {
        out.println();
        long    start = System.currentTimeMillis();
        boolean r  = function.apply(cmd.command);
        long    end = System.currentTimeMillis();
        executor.printCost(end, start);
        out.println();
        return r;
    }

    void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) { closeable.close(); }
        } catch (Throwable ignore) {
        }
    }

}
