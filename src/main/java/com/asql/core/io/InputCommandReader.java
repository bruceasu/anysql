package com.asql.core.io;

import com.asql.core.util.JavaVm;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author suk
 */
public class InputCommandReader implements CommandReader, AutoCloseable
{

    private InputStream       in         = null;
    private InputStreamReader reader     = null;
    private String            workingDir = JavaVm.USER_DIRECTORY;

    public InputCommandReader(InputStream paramInputStream)
    {
        this.in = paramInputStream;
        this.reader = new InputStreamReader(this.in);
    }

    @Override
    public void close()
    {
        try {
            this.in.close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public String readline() throws IOException
    {
        StringBuffer localStringBuffer = new StringBuffer();
        char c = '\000';
        int i = 0;
        while ((i = this.reader.read()) != -1) {
            c = (char) i;
            if (c == '\n') {
                break;
            }
            if (c == '\r') {
                continue;
            }
            localStringBuffer.append(c);
        }
        if ((i == -1) && (localStringBuffer.length() == 0)) {
            return null;
        }
        return localStringBuffer.toString();
    }

    @Override
    public String getWorkingDir()
    {
        return this.workingDir;
    }

    @Override
    public void setWorkingDir(String paramString)
    {
        this.workingDir = paramString;
    }
}
