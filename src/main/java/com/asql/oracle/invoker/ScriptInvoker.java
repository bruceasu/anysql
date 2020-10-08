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
 *
 * @author suk
 * @date 2017/8/13
 */
public class ScriptInvoker implements ModuleInvoker {
    OracleSQLExecutor executor;
    CommandLog out;
    CMDType cmdType;
    public ScriptInvoker(OracleSQLExecutor executor) {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCommandType();
    }

    @Override
    public boolean invoke(Command cmd) {
        executor.getObjectFromCommand(cmd.COMMAND.substring(0, Math.min(100, cmd.COMMAND.length() - 1)));
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
