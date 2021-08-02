package com.asql.core.io;

import java.io.IOException;

/**
 * @author suk
 */
public interface CommandReader
{

    String readline() throws IOException;

    default String readPassword() throws Exception
    {
        return PasswordReader.readPassword("Password: ");
    }


    String getWorkingDir();

    void setWorkingDir(String paramString);
}
