package com.asql.core.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PasswordReader {
    public static String readPassword(String paramString)
            throws Exception {
        if (System.console() != null) {
            char[] chars = System.console().readPassword(paramString);
            return new String(chars);
        } else {
            ConsoleEraser localConsoleEraser = new ConsoleEraser();
            System.out.print(paramString);
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(System.in));
            localConsoleEraser.start();
            String str = localBufferedReader.readLine();
            localConsoleEraser.halt();
            System.out.print("\b");
            return str;
        }
    }
}

