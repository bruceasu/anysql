package com.asql.core.log;

import com.asql.core.CommandExecutor;

public class DefaultCommandLog extends OutputCommandLog {
  public DefaultCommandLog(CommandExecutor paramCommandExecutor) {
    super(paramCommandExecutor, System.out);
  }

  public void close() {
  }
}
