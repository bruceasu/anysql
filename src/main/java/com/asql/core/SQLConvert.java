package com.asql.core;

import java.util.Vector;

public final class SQLConvert {
    private static final boolean isSpaceChar(char paramChar) {
        return (paramChar == ' ') || (paramChar == '\t') || (paramChar == '\r') || (paramChar == '\n');
    }

    private static final boolean isVariableFirst(char paramChar) {
        if (paramChar == '_')
            return true;
        if ((paramChar >= '0') && (paramChar <= '9'))
            return true;
        if ((paramChar >= 'a') && (paramChar <= 'z'))
            return true;
        return (paramChar >= 'A') && (paramChar <= 'Z');
    }

    private static final boolean isVariableChar(char paramChar) {
        if (paramChar == '_')
            return true;
        if ((paramChar >= '0') && (paramChar <= '9'))
            return true;
        if ((paramChar >= 'a') && (paramChar <= 'z'))
            return true;
        return (paramChar >= 'A') && (paramChar <= 'Z');
    }

    private static final boolean strncmp(char[] paramArrayOfChar,
                                         int paramInt,
                                         String paramString) {
        char[] arrayOfChar = paramString.toCharArray();
        for (int i = 0; i < arrayOfChar.length; i++)
            if ((paramInt + i >= paramArrayOfChar.length)
                    || (paramArrayOfChar[(paramInt + i)] != arrayOfChar[i]))
                return false;
        return (paramInt + arrayOfChar.length >= paramArrayOfChar.length)
                || (isSpaceChar(paramArrayOfChar[(paramInt + arrayOfChar.length)]));
    }

    private static final SQLQuery parseSQLString(String paramString,
                                                 VariableTable paramVariableTable) {
        if (paramString == null)
            return null;
        String str1 = "";
        int i = 1;
        int j = 0;
        Vector localVector1 = new Vector();
        Vector localVector2 = new Vector();
        StringBuffer localStringBuffer = new StringBuffer();
        char[] arrayOfChar = paramString.toCharArray();
        int k = 0;
        while (k < arrayOfChar.length) {
            if (isSpaceChar(arrayOfChar[k])) {
                localStringBuffer.append(arrayOfChar[k]);
                k++;
                continue;
            }
            if (arrayOfChar[k] == '\\') {
                i = 0;
                if (k + 1 < arrayOfChar.length) {
                    if (arrayOfChar[(k + 1)] == '&') {
                        localStringBuffer.append(arrayOfChar[(k + 1)]);
                        k += 2;
                        continue;
                    }
                    localStringBuffer.append(arrayOfChar[k]);
                    k++;
                    continue;
                }
                localStringBuffer.append(arrayOfChar[k]);
                k++;
                continue;
            }
            if (arrayOfChar[k] == '\'') {
                i = 0;
                if ((k + 1 < arrayOfChar.length) && (arrayOfChar[(k + 1)] == '\'')) {
                    localStringBuffer.append(arrayOfChar[k]);
                    localStringBuffer.append(arrayOfChar[(k + 1)]);
                    k += 2;
                    continue;
                }
                j = j == 0 ? 1 : 0;
                localStringBuffer.append(arrayOfChar[k]);
                k++;
                continue;
            }
            int m;
            int n;
            if ((arrayOfChar[k] == ':') && (j == 0)) {
                if (k + 1 < arrayOfChar.length) {
                    if (arrayOfChar[(k + 1)] == '=') {
                        i = 0;
                        localStringBuffer.append(arrayOfChar[k]);
                        k++;
                        continue;
                    }
                    String str2;
                    if (arrayOfChar[(k + 1)] == '{') {
                        m = k + 2;
                        for (n = k + 2; (n < arrayOfChar.length) && (arrayOfChar[n] != '}'); n++) ;
                        if (m >= arrayOfChar.length)
                            continue;
                        if (n < arrayOfChar.length) {
                            localVector1.addElement(String.valueOf(arrayOfChar, m, n - m));
                            localStringBuffer.append("?");
                        } else {
                            localVector1.addElement(String.valueOf(arrayOfChar, m, arrayOfChar.length - m));
                            localStringBuffer.append("?");
                        }
                        for (k = n + 1; (k < arrayOfChar.length) && (isSpaceChar(arrayOfChar[k])); k++)
                            localStringBuffer.append(arrayOfChar[k]);
                        if ((k < arrayOfChar.length) && (isVariableChar(arrayOfChar[k]))) {
                            m = k;
                            for (n = k; (n < arrayOfChar.length) && (isVariableChar(arrayOfChar[n])); n++)
                                ;
                            str2 = "IN";
                            if (n < arrayOfChar.length)
                                str2 = String.valueOf(arrayOfChar, m, n - m);
                            else
                                str2 = String.valueOf(arrayOfChar, m, arrayOfChar.length - m);
                            if ("IN".equalsIgnoreCase(str2)) {
                                localVector2.addElement("IN");
                                k = n + 1;
                                continue;
                            }
                            if ("OUT".equalsIgnoreCase(str2)) {
                                localVector2.addElement("OUT");
                                k = n + 1;
                                continue;
                            }
                            if ("INOUT".equalsIgnoreCase(str2)) {
                                localVector2.addElement("INOUT");
                                k = n + 1;
                                continue;
                            }
                            localVector2.addElement("IN");
                            continue;
                        }
                        if (i != 0) {
                            localVector2.addElement("OUT");
                            continue;
                        }
                        localVector2.addElement("IN");
                        continue;
                    }
                    if (isVariableFirst(arrayOfChar[(k + 1)])) {
                        m = k + 1;
                        for (n = k + 1; (n < arrayOfChar.length) && (isVariableChar(arrayOfChar[n])); n++)
                            ;
                        if (m >= arrayOfChar.length)
                            continue;
                        if (n < arrayOfChar.length) {
                            localVector1.addElement(String.valueOf(arrayOfChar, m, n - m));
                            localStringBuffer.append("?");
                        } else {
                            localVector1.addElement(String.valueOf(arrayOfChar, m, arrayOfChar.length - m));
                            localStringBuffer.append("?");
                        }
                        for (k = n; (k < arrayOfChar.length) && (isSpaceChar(arrayOfChar[k])); k++)
                            localStringBuffer.append(arrayOfChar[k]);
                        if ((k < arrayOfChar.length) && (isVariableChar(arrayOfChar[k]))) {
                            m = k;
                            for (n = k; (n < arrayOfChar.length) && (isVariableChar(arrayOfChar[n])); n++)
                                ;
                            str2 = "IN";
                            if (n < arrayOfChar.length)
                                str2 = String.valueOf(arrayOfChar, m, n - m);
                            else
                                str2 = String.valueOf(arrayOfChar, m, arrayOfChar.length - m);
                            if ("IN".equalsIgnoreCase(str2)) {
                                localVector2.addElement("IN");
                                k = n;
                                continue;
                            }
                            if ("OUT".equalsIgnoreCase(str2)) {
                                localVector2.addElement("OUT");
                                k = n;
                                continue;
                            }
                            if ("INOUT".equalsIgnoreCase(str2)) {
                                localVector2.addElement("INOUT");
                                k = n;
                                continue;
                            }
                            localVector2.addElement("IN");
                            continue;
                        }
                        if (i != 0) {
                            localVector2.addElement("OUT");
                            continue;
                        }
                        localVector2.addElement("IN");
                        continue;
                    }
                    i = 0;
                    localStringBuffer.append(arrayOfChar[k]);
                    k++;
                    continue;
                }
                k++;
                continue;
            }
            if (arrayOfChar[k] == '&') {
                i = 0;
                if (k + 1 < arrayOfChar.length) {
                    if (arrayOfChar[(k + 1)] == '{') {
                        m = k + 2;
                        for (n = k + 2; (n < arrayOfChar.length) && (arrayOfChar[n] != '}'); n++) ;
                        if (m < arrayOfChar.length)
                            if (n < arrayOfChar.length) {
                                str1 = String.valueOf(arrayOfChar, m, n - m);
                                if (paramVariableTable.exists(str1)) {
                                    localStringBuffer.append(paramVariableTable.getString(str1, ""));
                                } else {
                                    localStringBuffer.append("&{");
                                    localStringBuffer.append(str1);
                                    localStringBuffer.append("}");
                                }
                            } else {
                                str1 = String.valueOf(arrayOfChar, m, n - m);
                                if (paramVariableTable.exists(str1)) {
                                    localStringBuffer.append(paramVariableTable.getString(str1, ""));
                                } else {
                                    localStringBuffer.append("&{");
                                    localStringBuffer.append(str1);
                                    localStringBuffer.append("}");
                                }
                            }
                        k = n + 1;
                        continue;
                    }
                    m = k + 1;
                    for (n = k + 1; (n < arrayOfChar.length) && (isVariableChar(arrayOfChar[n])); n++)
                        ;
                    if (m < arrayOfChar.length)
                        if (n < arrayOfChar.length) {
                            str1 = String.valueOf(arrayOfChar, m, n - m);
                            if (paramVariableTable.exists(str1)) {
                                localStringBuffer.append(paramVariableTable.getString(str1, ""));
                            } else {
                                localStringBuffer.append('&');
                                localStringBuffer.append(str1);
                            }
                        } else {
                            str1 = String.valueOf(arrayOfChar, m, arrayOfChar.length - m);
                            if (paramVariableTable.exists(str1)) {
                                localStringBuffer.append(paramVariableTable.getString(str1, ""));
                            } else {
                                localStringBuffer.append('&');
                                localStringBuffer.append(str1);
                            }
                        }
                    if ((n < arrayOfChar.length) && (arrayOfChar[n] == '.')) {
                        k = n + 1;
                        continue;
                    }
                    k = n;
                    continue;
                }
                k++;
                continue;
            }
            localStringBuffer.append(arrayOfChar[k]);
            if ((i != 0) && (arrayOfChar[k] == '='))
                localStringBuffer.append(" call ");
            i = 0;
            k++;
        }
        return new SQLQuery(paramString, localStringBuffer.toString(), toArray(localVector1), toArray(localVector2));
    }

    public static final SQLQuery parseScript(String paramString) {
        return parseScript(paramString, new VariableTable());
    }

    public static final SQLQuery parseScript(String paramString, VariableTable paramVariableTable) {
        String[] arrayOfString1 = new String[0];
        String[] arrayOfString2 = new String[0];
        String str = "";
        if (paramVariableTable != null)
            str = paramVariableTable.parseString(paramString, '&', '\\');
        else
            str = paramString;
        return new SQLQuery(paramString, str, arrayOfString1, arrayOfString2);
    }

    public static final SQLQuery parseCall(String paramString) {
        return parseCall(paramString, new VariableTable());
    }

    public static final SQLQuery parseCall(String paramString, VariableTable paramVariableTable) {
        SQLQuery localSQLQuery = parseSQLString(paramString, paramVariableTable);
        if (!localSQLQuery.getDestSQL().trim().startsWith("?"))
            return new SQLQuery(localSQLQuery.getSourceSQL(), "call " + localSQLQuery.getDestSQL(), localSQLQuery.getParamNames(), localSQLQuery.getParamTypes());
        return localSQLQuery;
    }

    public static final SQLQuery parseSQL(String paramString) {
        return parseSQLString(paramString, new VariableTable());
    }

    public static final SQLQuery parseSQL(String paramString, VariableTable paramVariableTable) {
        return parseSQLString(paramString, paramVariableTable);
    }

    private static final String[] toArray(Vector paramVector) {
        String[] arrayOfString = new String[0];
        if (paramVector == null)
            return null;
        if (paramVector.size() == 0)
            return arrayOfString;
        arrayOfString = new String[paramVector.size()];
        for (int i = 0; i < paramVector.size(); i++)
            arrayOfString[i] = paramVector.elementAt(i).toString();
        return arrayOfString;
    }
}

