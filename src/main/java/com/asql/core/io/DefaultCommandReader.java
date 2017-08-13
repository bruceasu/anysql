package com.asql.core.io;

public class DefaultCommandReader extends InputCommandReader {
    public DefaultCommandReader() {
        super(System.in);
    }

    public void close() {
    }

    public String readPassword()
            throws Exception {
        return PasswordReader.readPassword("Password: ");
    }

    public void setWorkingDir(String paramString) {
    }
}
