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

import com.asql.core.CMDType;
import com.asql.core.Command;
import com.asql.core.DBRowCache;
import com.asql.core.ModuleInvoker;
import com.asql.core.log.CommandLog;
import com.asql.oracle.OracleSQLExecutor;

/**
 * Created by suk on 2017/8/13.
 */
public class CallInvoker implements ModuleInvoker {
    OracleSQLExecutor executor;
    CommandLog out;
    CMDType cmdType;

    public CallInvoker(OracleSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
    }

    @Override
    public boolean invoke(Command cmd) {
        DBRowCache rowCacheOfSessionWait;
        DBRowCache rowCacheOfSessionStats;
        long l1;
        long l2;
        out.println();
        rowCacheOfSessionWait = executor.getDbRowCacheOfSessionWait();
        rowCacheOfSessionStats = executor.getDbRowCacheOfSessionStats();
        l1 = System.currentTimeMillis();
        executor.executeCall(executor.database, new Command(cmd.type1, cmd.type2, executor.skipWord(cmd.command, 1)),
                executor.sysVariable, out);
        l2 = System.currentTimeMillis();
        executor.printCost(l2, l1);
        executor.printSessionStats(rowCacheOfSessionStats);
        executor.printSessionWait(rowCacheOfSessionWait);
        out.println();
        return true;
    }
}
