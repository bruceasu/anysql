package com.asql.core;

import com.asql.core.util.JavaVm;

public class Command {

    public int    type1      = -1;
    public int    type2      = -1;
    public String command    = null;
    public String workingDir = null;

    public Command(int type1, int type2, String cmd) {
        this.type1      = type1;
        this.type2      = type2;
        this.command    = cmd;
        this.workingDir = JavaVm.USER_DIRECTORY;
    }

    public Command(int type1, int type2, String cmd, String workDir) {
        this.type1      = type1;
        this.type2      = type2;
        this.command    = cmd;
        this.workingDir = workDir;
    }
}
