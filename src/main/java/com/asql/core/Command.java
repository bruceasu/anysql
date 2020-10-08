package com.asql.core;

import com.asql.core.util.JavaVM;

public class Command {
  public int TYPE1 = -1;
  public int TYPE2 = -1;
  public String COMMAND     = null;
  public String WORKING_DIR = null;

  public Command(int type1, int type2, String cmd) {
    this.TYPE1 = type1;
    this.TYPE2 = type2;
    this.COMMAND = cmd;
    this.WORKING_DIR = JavaVM.USER_DIRECTORY;
  }

  public Command(int type1, int type2, String cmd, String workDir) {
    this.TYPE1 = type1;
    this.TYPE2 = type2;
    this.COMMAND = cmd;
    this.WORKING_DIR = workDir;
  }
}
