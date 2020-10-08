package com.asql.core;

import com.asql.core.io.CommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.TextUtils;
import java.io.IOException;
import java.util.Vector;

public abstract class CMDType
{

    public static final int SQL_QUERY           = 0;
    public static final int SQL_DML             = 1;
    public static final int SQL_DDL             = 2;
    public static final int SQL_SCRIPT          = 3;
    public static final int SQL_CALL            = 4;
    public static final int ASQL_SINGLE         = 5;
    public static final int ASQL_MULTIPLE       = 6;
    public static final int ASQL_END            = 7;
    public static final int ASQL_EXIT           = 8;
    public static final int ASQL_CANCEL         = 9;
    public static final int UNKNOWN_COMMAND     = 10;
    public static final int ASQL_COMMENT        = 11;
    public static final int NULL_COMMAND        = 12;
    public static final int SQL_BLOCK           = 13;
    public static final int MULTI_COMMENT_START = 14;
    public static final int MULTI_COMMENT_END   = 15;
    public static final int ASQL_SQL_FILE       = 16;
    public static final int ASQL_DB_COMMAND     = 17;
    public static final int DISABLED_COMMAND    = 18;
    
    private boolean multi_comment = false;
    private boolean query_only    = true;

    public final void setQueryOnly(boolean paramBoolean)
    {
        this.query_only = paramBoolean;
    }

    public final boolean getQueryOnly()
    {
        return this.query_only;
    }

    public static final String getName(int paramInt)
    {
        switch (paramInt) {
        case SQL_QUERY:
            return "SQL_QUERY";
        case SQL_DML:
            return "SQL_DML";
        case SQL_DDL:
            return "SQL_DDL";
        case SQL_SCRIPT:
            return "SQL_SCRIPT";
        case SQL_CALL:
            return "SQL_CALL";
        case ASQL_SINGLE:
            return "ASQL_SINGLE";
        case ASQL_MULTIPLE:
            return "ASQL_MULTIPLE";
        case ASQL_END:
            return "ASQL_END";
        case ASQL_EXIT:
            return "ASQL_EXIT";
        case ASQL_CANCEL:
            return "ASQL_CANCEL";
        case ASQL_COMMENT:
            return "ASQL_COMMENT";
        case UNKNOWN_COMMAND:
            return "UNKNOWN_COMMAND";
        case NULL_COMMAND:
            return "NULL_COMMAND";
        case SQL_BLOCK:
            return "SQL_BLOCK";
        case ASQL_SQL_FILE:
            return "ASQL_SQLFILE";
        case MULTI_COMMENT_START:
            return "/*";
        case MULTI_COMMENT_END:
            return "*/";
        }
        return "UNKNOWN_COMMAND";
    }

    public final String getPrompt(int paramInt)
    {
        if (paramInt <= 1) {
            if (this.multi_comment) {
                return "ADOC> ";
            }
            return "ASQL> ";
        }
        StringBuffer localStringBuffer = new StringBuffer();
        String str = String.valueOf(paramInt);
        for (int i = 0; i < 5 - str.length(); i++) {
            localStringBuffer.append(' ');
        }
        localStringBuffer.append(str);
        localStringBuffer.append(' ');
        return localStringBuffer.toString();
    }

    public abstract String[] getSQLQuery();

    public abstract String[] getSQLDML();

    public abstract String[] getSQLDDL();

    public abstract String[] getSQLScript();

    public abstract String[] getSQLBlock();

    public abstract String[] getSQLCall();

    public abstract String[] getASQLSingle();

    public abstract String[] getASQLMultiple();

    public abstract String[] getEnd();

    public abstract String[] getCancel();

    public abstract String[] getDBCommand();

    public abstract String[] getComment();

    public abstract String[] getSQLFile();

    public abstract char getCompleteChar();

    public abstract char getContinueChar();

    public abstract String[] getMultipleStart();

    public abstract String[] getMultipleEnd();

    public abstract String[] getCommandHint();

    public String[] getExit()
    {
        return new String[]{"EXIT", "QUIT"};
    }

    public final int endsWith(String[] paramArrayOfString, String paramString)
    {
        if ((paramString == null) && (paramString.length() == 0)) {
            return -1;
        }
        int i = paramString.length() - 1;
        for (; (i >= 0) && isWhiteSpace(paramString.charAt(i)); i--) {
            ;
        }
        if (i == -1) {
            return -1;
        }
        for (int k = 0; k < paramArrayOfString.length; k++) {
            if (paramString.lastIndexOf(paramArrayOfString[k])
                    == i - paramArrayOfString[k].length() + 1) {
                return k;
            }
        }
        return -1;
    }

    private boolean isWhiteSpace(int j)
    {
        return (j == ' ') || (j == 9) || (j == 13) || (j == 10);
    }

    public final int startsWith(String[] paramArrayOfString, String paramString)
    {
        if ((paramString == null) && (paramString.length() == 0)) {
            return -1;
        }
        int j;
        int i = 0;
        for (; (i < paramString.length()) && (((j = paramString.charAt(i)) == ' ') || (j == 9));
                i++) {
            ;
        }
        for (int k = 0; k < paramArrayOfString.length; k++) {
            if (paramString.indexOf(paramArrayOfString[k]) == i) {
                return k;
            }
        }
        return -1;
    }

    private final boolean commandCheck(String[] paramArrayOfString1, String[] paramArrayOfString2)
    {
        if (paramArrayOfString2 == null) {
            return false;
        }
        if (paramArrayOfString2.length == 0) {
            return false;
        }
        if (paramArrayOfString1.length == 0) {
            return false;
        }
        int i = 0;
        int j = 0;
        int k = 0;
        if ((paramArrayOfString1.length == 1) && (paramArrayOfString1[0].equalsIgnoreCase("*"))) {
            return true;
        }
        for (i = 0; i < paramArrayOfString1.length; i++) {
            j = 0;
            String[] arrayOfString = TextUtils.toStringArray(
                    TextUtils.getWords(paramArrayOfString1[i]));
            if (arrayOfString.length > paramArrayOfString2.length) {
                continue;
            }
            k = 1;
            for (j = 0; j < arrayOfString.length; j++) {
                if (arrayOfString[j].equalsIgnoreCase(paramArrayOfString2[j])) {
                    continue;
                }
                k = 0;
                break;
            }
            if (k != 0) {
                return true;
            }
        }
        return false;
    }

    private final boolean commandEqual(String[] paramArrayOfString1, String[] paramArrayOfString2)
    {
        if (paramArrayOfString2 == null) {
            return false;
        }
        if (paramArrayOfString2.length == 0) {
            return false;
        }
        if (paramArrayOfString1.length == 0) {
            return false;
        }
        int i = 0;
        int j = 0;
        int k = 0;
        for (i = 0; i < paramArrayOfString1.length; i++) {
            j = 0;
            String[] arrayOfString = TextUtils.toStringArray(
                    TextUtils.getWords(paramArrayOfString1[i]));
            if (arrayOfString.length != paramArrayOfString2.length) {
                continue;
            }
            k = 1;
            for (j = 0; j < arrayOfString.length; j++) {
                if (arrayOfString[j].equalsIgnoreCase(paramArrayOfString2[j])) {
                    continue;
                }
                k = 0;
                break;
            }
            if (k != 0) {
                return true;
            }
        }
        return false;
    }

    public final int getCommandID(String paramString)
    {
        int i = matchLastChar(paramString, getContinueChar());
        if (i == -1) {
            i = matchLastChar(paramString, getCompleteChar());
        }
        String str = paramString;
        if (i > -1) {
            str = paramString.substring(0, i);
        }
        String[] arrayOfString = TextUtils.toStringArray(TextUtils.getWords(str));
        if (this.multi_comment) {
            return MULTI_COMMENT_START;
        }
        if (arrayOfString.length == 0) {
            return NULL_COMMAND;
        }
        if (commandCheck(getASQLSingle(), arrayOfString)) {
            return ASQL_SINGLE;
        }
        if (commandCheck(getSQLQuery(), arrayOfString)) {
            return SQL_QUERY;
        }
        if (commandCheck(getSQLScript(), arrayOfString)) {
            return this.query_only ? DISABLED_COMMAND : SQL_SCRIPT;
        }
        if (commandCheck(getSQLDDL(), arrayOfString)) {
            return this.query_only ? DISABLED_COMMAND : SQL_DDL;
        }
        if (commandCheck(getSQLDML(), arrayOfString)) {
            return this.query_only ? DISABLED_COMMAND : SQL_DML;
        }
        if (commandCheck(getSQLCall(), arrayOfString)) {
            return this.query_only ? DISABLED_COMMAND : SQL_CALL;
        }
        if (commandCheck(getSQLBlock(), arrayOfString)) {
            return this.query_only ? DISABLED_COMMAND : SQL_BLOCK;
        }
        if (commandCheck(getDBCommand(), arrayOfString)) {
            return ASQL_DB_COMMAND;
        }
        if (startsWith(getComment(), paramString) != -1) {
            return ASQL_COMMENT;
        }
        if (startsWith(getSQLFile(), paramString) != -1) {
            return ASQL_SQL_FILE;
        }
        if (startsWith(getMultipleStart(), paramString) != -1) {
            return MULTI_COMMENT_START;
        }
        if (commandCheck(getASQLMultiple(), arrayOfString)) {
            return ASQL_MULTIPLE;
        }
        if (commandEqual(getEnd(), arrayOfString)) {
            return ASQL_END;
        }
        if (commandEqual(getExit(), arrayOfString)) {
            return ASQL_EXIT;
        }
        if (commandEqual(getCancel(), arrayOfString)) {
            return ASQL_CANCEL;
        }
        return UNKNOWN_COMMAND;
    }

    private int matchLastChar(String paramString, char paramChar)
    {
        if (paramString == null) {
            return -1;
        }
        if (paramChar == ' ') {
            return -1;
        }
        char[] arrayOfChar = paramString.toCharArray();
        if (arrayOfChar.length == 0) {
            return -1;
        }
        int i = arrayOfChar.length - 1;
        while ((arrayOfChar[i] == ' ') || (arrayOfChar[i] == '\t') || (arrayOfChar[i] == '\r') || (
                arrayOfChar[i] == '\n')) {
            i--;
            if (i < 0) {
                return -1;
            }
        }
        if (arrayOfChar[i] == paramChar) {
            return i;
        }
        return -1;
    }

    public final boolean isCommandReady(int paramInt1, int paramInt2, String paramString)
    {
        switch (paramInt1) {
        case ASQL_END:
        case ASQL_EXIT:
        case ASQL_CANCEL:
        case UNKNOWN_COMMAND:
        case ASQL_COMMENT:
        case NULL_COMMAND:
        case MULTI_COMMENT_START:
        case DISABLED_COMMAND:
            return true;
        case SQL_CALL:
        case ASQL_SINGLE:
        case ASQL_SQL_FILE:
        case ASQL_DB_COMMAND:
            return matchLastChar(paramString, getContinueChar()) < 0;
        case SQL_SCRIPT:
        case SQL_BLOCK:
            return (paramInt2 == ASQL_END) || (paramInt2 == ASQL_CANCEL);
        case SQL_QUERY:
        case SQL_DML:
        case SQL_DDL:
        case ASQL_MULTIPLE:
            if ((paramInt2 == ASQL_END) || (paramInt2 == ASQL_CANCEL)) {
                return true;
            }
            return matchLastChar(paramString, getCompleteChar()) >= 0;
        case MULTI_COMMENT_END:
        }
        return true;
    }

    public final Command readCommand(CommandReader paramCommandReader) throws IOException
    {
        Vector localVector = new Vector(10, 10);
        String str = null;
        int i = -1;
        int j = -1;
        int k = -1;
        while ((str = paramCommandReader.readline()) != null) {
            if (i == -1) {
                i = getCommandID(str);
            }
            if (i == MULTI_COMMENT_START) {
                this.multi_comment = true;
            }
            if (this.multi_comment) {
                if (endsWith(getMultipleEnd(), str) != -1) {
                    j = MULTI_COMMENT_END;
                    this.multi_comment = false;
                } else {
                    j = getCommandID(str);
                }
            } else {
                j = getCommandID(str);
            }
            if (isCommandReady(i, j, str)) {
                switch (i) {
                case SQL_SCRIPT:
                case SQL_BLOCK:
                    break;
                case SQL_QUERY:
                case SQL_DML:
                case SQL_DDL:
                case ASQL_MULTIPLE:
                    if ((j == ASQL_END) || (j == ASQL_CANCEL)) {
                        break;
                    }
                    k = matchLastChar(str, getCompleteChar());
                    if (k >= 0) {
                        localVector.addElement(str.substring(0, k));
                    } else {
                        localVector.addElement(str);
                    }
                    break;
                case SQL_CALL:
                case ASQL_SINGLE:
                case ASQL_SQL_FILE:
                case ASQL_DB_COMMAND:
                    k = matchLastChar(str, getContinueChar());
                    if (k == -1) {
                        k = matchLastChar(str, getCompleteChar());
                    }
                    if (k > -1) {
                        localVector.addElement(str.substring(0, k));
                    } else {
                        localVector.addElement(str);
                    }
                    break;
                case ASQL_END:
                case ASQL_EXIT:
                case ASQL_CANCEL:
                case UNKNOWN_COMMAND:
                    k = matchLastChar(str, getContinueChar());
                    if (k == -1) {
                        k = matchLastChar(str, getCompleteChar());
                    }
                    if (k > -1) {
                        localVector.addElement(str.substring(0, k));
                    } else {
                        localVector.addElement(str);
                    }
                    break;
                case ASQL_COMMENT:
                    localVector.addElement(str);
                    if (j != MULTI_COMMENT_END) {
                        break;
                    }
                    this.multi_comment = false;
                    break;
                case MULTI_COMMENT_START:
                    localVector.addElement(str);
                    if (j != MULTI_COMMENT_END) {
                        break;
                    }
                    this.multi_comment = false;
                case NULL_COMMAND:
                case MULTI_COMMENT_END:
                }
                break;
            }
            switch (i) {
            case ASQL_SINGLE:
            case ASQL_END:
            case ASQL_EXIT:
            case ASQL_CANCEL:
            case UNKNOWN_COMMAND:
            case ASQL_COMMENT:
            case NULL_COMMAND:
            case ASQL_SQL_FILE:
            case ASQL_DB_COMMAND:
                k = matchLastChar(str, getContinueChar());
                if (k == -1) {
                    k = matchLastChar(str, getCompleteChar());
                }
                if (k > -1) {
                    localVector.addElement(str.substring(0, k));
                    continue;
                }
                localVector.addElement(str);
                break;
            case ASQL_MULTIPLE:
            case SQL_BLOCK:
            case MULTI_COMMENT_START:
            case MULTI_COMMENT_END:
            default:
                localVector.addElement(str);
            }
        }
        if ((str == null) && (localVector.size() == 0)) {
            return new Command(ASQL_EXIT, ASQL_EXIT, null, paramCommandReader.getWorkingDir());
        }
        StringBuffer localStringBuffer = new StringBuffer();
        for (int m = 0; m < localVector.size(); m++) {
            localStringBuffer.append((String) localVector.elementAt(m));
            if (m >= localVector.size() - 1) {
                continue;
            }
            localStringBuffer.append("\n");
        }
        return new Command(i, j, localStringBuffer.toString(), paramCommandReader.getWorkingDir());
    }

    public final Command readCommand(CommandReader paramCommandReader, CommandLog paramCommandLog)
    throws IOException
    {
        return readCommand(paramCommandReader, paramCommandLog, false);
    }

    public final Command readCommand(CommandReader reader,
                                     CommandLog log,
                                     boolean paramBoolean) throws IOException
    {
        Vector localVector = new Vector(10, 10);
        String str = null;
        int i = -1;
        int j = -1;
        int k = -1;
        log.prompt(getPrompt(localVector.size() + 1));
        while ((str = reader.readline()) != null) {
            if (paramBoolean) {
                log.println(str);
            } else if (log.getLogFile() != null) {
                log.getLogFile().println(str);
            }
            if (i == -1) {
                i = getCommandID(str);
            }
            if (i == 14) {
                this.multi_comment = true;
            }
            if (this.multi_comment) {
                if (endsWith(getMultipleEnd(), str) != -1) {
                    j = 15;
                    this.multi_comment = false;
                } else {
                    j = getCommandID(str);
                }
            } else {
                j = getCommandID(str);
            }
            if (isCommandReady(i, j, str)) {
                switch (i) {
                case SQL_SCRIPT:
                case SQL_BLOCK:
                    break;
                case SQL_QUERY:
                case SQL_DML:
                case SQL_DDL:
                case ASQL_MULTIPLE:
                    if ((j == ASQL_END) || (j == ASQL_CANCEL)) {
                        break;
                    }
                    k = matchLastChar(str, getCompleteChar());
                    if (k >= 0) {
                        localVector.addElement(str.substring(0, k));
                    } else {
                        localVector.addElement(str);
                    }
                    break;
                case SQL_CALL:
                case ASQL_SINGLE:
                case ASQL_SQL_FILE:
                case ASQL_DB_COMMAND:
                    k = matchLastChar(str, getContinueChar());
                    if (k == -1) {
                        k = matchLastChar(str, getCompleteChar());
                    }
                    if (k > -1) {
                        localVector.addElement(str.substring(0, k));
                    } else {
                        localVector.addElement(str);
                    }
                    break;
                case ASQL_END:
                case ASQL_EXIT:
                case ASQL_CANCEL:
                case UNKNOWN_COMMAND:
                    k = matchLastChar(str, getContinueChar());
                    if (k == -1) {
                        k = matchLastChar(str, getCompleteChar());
                    }
                    if (k > -1) {
                        localVector.addElement(str.substring(0, k));
                    } else {
                        localVector.addElement(str);
                    }
                    break;
                case ASQL_COMMENT:
                    localVector.addElement(str);
                    if (j != MULTI_COMMENT_END) {
                        break;
                    }
                    this.multi_comment = false;
                    break;
                case MULTI_COMMENT_START:
                    localVector.addElement(str);
                    if (j != MULTI_COMMENT_END) {
                        break;
                    }
                    this.multi_comment = false;
                case NULL_COMMAND:
                case MULTI_COMMENT_END:
                }
                break;
            }
            switch (i) {
            case ASQL_SINGLE:
            case ASQL_END:
            case ASQL_EXIT:
            case ASQL_CANCEL:
            case UNKNOWN_COMMAND:
            case ASQL_COMMENT:
            case NULL_COMMAND:
            case ASQL_SQL_FILE:
            case ASQL_DB_COMMAND:
                k = matchLastChar(str, getContinueChar());
                if (k == -1) {
                    k = matchLastChar(str, getCompleteChar());
                }
                if (k > -1) {
                    localVector.addElement(str.substring(0, k));
                } else {
                    localVector.addElement(str);
                }
                break;
            case ASQL_MULTIPLE:
            case SQL_BLOCK:
            case MULTI_COMMENT_START:
            case MULTI_COMMENT_END:
            default:
                localVector.addElement(str);
            }
            log.prompt(getPrompt(localVector.size() + 1));
        }
        if ((str == null) && (localVector.size() == 0)) {
            if (paramBoolean) {
                log.println();
            } else if (log.getLogFile() != null) {
                log.getLogFile().println();
            }
            return new Command(8, 8, null, reader.getWorkingDir());
        }
        StringBuffer localStringBuffer = new StringBuffer();
        for (int m = 0; m < localVector.size(); m++) {
            localStringBuffer.append((String) localVector.elementAt(m));
            if (m >= localVector.size() - 1) {
                continue;
            }
            localStringBuffer.append("\n");
        }
        return new Command(i, j, localStringBuffer.toString(), reader.getWorkingDir());
    }
}

