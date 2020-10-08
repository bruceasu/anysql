package com.asql.core;

/**
 *
 * @author suk
 * @date 2017/8/13
 */
public interface ModuleInvoker {

    /**
     * @param cmd {@link Command}
     * @return true if success, otherwise return false.
     */
    boolean invoke(Command cmd);
}
