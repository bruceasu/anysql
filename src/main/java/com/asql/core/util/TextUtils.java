package com.asql.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

public final class TextUtils {
    public static final Vector getFields(String paramString) {
        return getFields(paramString, ",", "\"");
    }

    public static final Vector getFields(String paramString1, String paramString2) {
        return getFields(paramString1, paramString2, "\"");
    }

    public static final Vector getFields(String paramString1, String paramString2, String paramString3) {
        Vector localVector = new Vector();
        if (paramString1 == null)
            return localVector;
        int i = 0;
        FieldTokenizer localFieldTokenizer = new FieldTokenizer(paramString1, paramString2, true);
        Object localObject = "";
        int j = 0;
        while (localFieldTokenizer.hasMoreTokens()) {
            String str = localFieldTokenizer.nextToken();
            if (i != 0) {
                localObject = (String) localObject + str;
            } else {
                if (str.equals(paramString2)) {
                    if (j == 0)
                        localVector.addElement(null);
                    j = 0;
                    continue;
                }
                localObject = str;
            }
            if (((String) localObject).startsWith(paramString3))
                i = 1;
            if ((i != 0) && ((!((String) localObject).endsWith(paramString3)) || (((String) localObject).length() == paramString3.length())))
                continue;
            if (((String) localObject).endsWith(paramString3))
                i = 0;
            if ((((String) localObject).length() > paramString3.length()) && (((String) localObject).startsWith(paramString3)) && (((String) localObject).endsWith(paramString3))) {
                localObject = ((String) localObject).substring(paramString3.length());
                if (((String) localObject).length() >= paramString3.length())
                    localObject = ((String) localObject).substring(0, ((String) localObject).length() - paramString3.length());
            }
            if (i == 0) {
                localVector.addElement(localObject);
                j = 1;
            }
            localObject = "";
        }
        if (j == 0)
            localVector.addElement(null);
        return (Vector) localVector;
    }

    /**
     * 分词.
     * @param cmdLine String
     * @return Vector
     */
    public static final Vector getWords(String cmdLine) {
        String[] arrayOfString = {" ", "\t", "\r", "\n"};
        return getWords(cmdLine, arrayOfString, "\"");
    }

    /**
     * 分词.
     *
     * @param cmdLine String
     * @param splitters String[]
     * @return Vector
     */
    public static final Vector getWords(String cmdLine, String[] splitters) {
        return getWords(cmdLine, splitters, "\"");
    }

    /**
     * 分词.
     *
     * @param cmdLine String
     * @param splitters String[]
     * @param escape String
     * @return Vector
     */
    public static final Vector getWords(String cmdLine, String[] splitters, String escape) {
        Vector localVector = new Vector();
        int i = 0;
        CommandTokenizer localCommandTokenizer = new CommandTokenizer(cmdLine, splitters, true);
        Object localObject = "";
        while (localCommandTokenizer.hasMoreTokens()) {
            String str = localCommandTokenizer.nextToken();
            if (i != 0) {
                localObject = (String) localObject + str;
            } else {
                if (indexOf(splitters, str) >= 0)
                    continue;
                localObject = str;
            }
            if (((String) localObject).startsWith(escape))
                i = 1;
            if ((i != 0) && (((!((String) localObject).endsWith(escape)) && (!((String) localObject).endsWith(escape + "\n"))) || (((String) localObject).length() == escape.length())))
                continue;
            if ((((String) localObject).endsWith(escape)) || (((String) localObject).endsWith(escape + "\n")))
                i = 0;
            if ((((String) localObject).length() > escape.length()) && (((String) localObject).startsWith(escape)) && ((((String) localObject).endsWith(escape)) || (((String) localObject).endsWith(escape + "\n")))) {
                localObject = ((String) localObject).substring(escape.length());
                if ((((String) localObject).length() >= escape.length()) && (((String) localObject).endsWith(escape)))
                    localObject = ((String) localObject).substring(0, ((String) localObject).length() - escape.length());
                else if ((((String) localObject).length() >= escape.length()) && (((String) localObject).endsWith(escape + "\n")))
                    localObject = ((String) localObject).substring(0, ((String) localObject).length() - escape.length() - 1);
            }
            if (i == 0) {
                if (((String) localObject).endsWith("\n"))
                    localObject = ((String) localObject).substring(0, ((String) localObject).length() - 1);
                localVector.addElement(((String) localObject).trim());
            }
            localObject = "";
        }
        if (((String) localObject).length() > 0) {
            if (((String) localObject).endsWith("\n"))
                localObject = ((String) localObject).substring(0, ((String) localObject).length() - 1);
            localVector.addElement(((String) localObject).trim());
        }
        return (Vector) localVector;
    }

    public static final Vector getLines(String content) {
        String str = null;
        Vector localVector = new Vector();
        StringReader localStringReader = new StringReader(content);
        BufferedReader localBufferedReader = new BufferedReader(localStringReader);
        try {
            while ((str = localBufferedReader.readLine()) != null)
                localVector.addElement(str);
            localBufferedReader.close();
        } catch (IOException localIOException) {
        }
        return localVector;
    }

    public static final String[] toStringArray(Vector paramVector) {
        String[] arrayOfString = new String[0];
        if ((paramVector == null) || (paramVector.size() == 0))
            return arrayOfString;
        arrayOfString = new String[paramVector.size()];
        for (int i = 0; i < paramVector.size(); i++)
            arrayOfString[i] = paramVector.elementAt(i).toString();
        return arrayOfString;
    }

    public static final int indexOf(Vector patterns, String content) {
        return indexOf(patterns, content, false);
    }

    public static final int indexOf(Vector patterns, String content, boolean sensitive) {
        if (patterns == null)
            return -1;
        if (patterns.size() == 0)
            return -1;
        for (int i = 0; i < patterns.size(); i++)
            if (sensitive) {
                if (patterns.elementAt(i).toString().equals(content))
                    return i;
            } else if (patterns.elementAt(i).toString().equalsIgnoreCase(content))
                return i;
        return -1;
    }

    public static final int indexOf(String[] patterns, String content) {
        return indexOf(patterns, content, false);
    }

    public static final int indexOf(String[] patterns, String content, boolean sensitive) {
        if (patterns == null)
            return -1;
        if (patterns.length == 0)
            return -1;
        for (int i = 0; i < patterns.length; i++)
            if (sensitive) {
                if (patterns[i].equals(content))
                    return i;
            } else if (patterns[i].equalsIgnoreCase(content))
                return i;
        return -1;
    }
}
