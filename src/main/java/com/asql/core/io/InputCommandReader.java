package com.asql.core.io;

import com.asql.core.util.JavaVM;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputCommandReader
        implements CommandReader, AutoCloseable {
    private InputStream in = null;
    private InputStreamReader reader = null;
    private String workingDir = JavaVM.USER_DIRECTORY;

    public InputCommandReader(InputStream paramInputStream) {
        this.in = paramInputStream;
        this.reader = new InputStreamReader(this.in);
    }

    public void close() {
        try {
            this.in.close();
        } catch (IOException ignore) {
        }
    }

    public String readline()
            throws IOException {
        StringBuffer localStringBuffer = new StringBuffer();
        char c = '\000';
        int i = 0;
        while ((i = this.reader.read()) != -1) {
            c = (char) i;
            if (c == '\n')
                break;
            if (c == '\r')
                continue;
            localStringBuffer.append(c);
        }
        if ((i == -1) && (localStringBuffer.length() == 0))
            return null;
        return localStringBuffer.toString();
    }

    public String readPassword()
            throws Exception {
        return PasswordReader.readPassword("Password: ");
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public void setWorkingDir(String paramString) {
        this.workingDir = paramString;
    }
}
