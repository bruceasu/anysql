package com.asql.core;

import com.asql.core.util.JavaVM;

public class Command {
  public int TYPE1 = -1;
  public int TYPE2 = -1;
  public String COMMAND = null;
  public String WORKINGDIR = null;

  public Command(int type1, int type2, String cmd) {
    this.TYPE1 = type1;
    this.TYPE2 = type2;
    this.COMMAND = cmd;
    this.WORKINGDIR = JavaVM.USER_DIRECTORY;
  }

  public Command(int paramInt1, int paramInt2, String paramString1, String paramString2) {
    this.TYPE1 = paramInt1;
    this.TYPE2 = paramInt2;
    this.COMMAND = paramString1;
    this.WORKINGDIR = paramString2;
  }
}
