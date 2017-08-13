package com.asql.core;

import com.asql.core.util.DateOperator;
import com.asql.core.util.JavaVM;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Hashtable;

public final class VariableTable {
    protected int varCount = 0;
    protected String[] varName = new String[50];
    protected int[] varType = new int[50];
    protected Object[] varValue = new Object[50];
    private Hashtable varIndex = new Hashtable();
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private int findVariable(String paramString) {
        if (paramString == null)
            return -1;
        if (this.varIndex.containsKey(paramString.toUpperCase())) {
            Integer localInteger = (Integer) this.varIndex.get(paramString.toUpperCase());
            return localInteger.intValue();
        }
        int i = 0;
        for (;
             (i < this.varCount)
                     && ((this.varName[i] == null)
                     || (!this.varName[i].equalsIgnoreCase(paramString)));
             i++) {
            ;
        }
        return i == this.varCount ? -1 : i;
    }

    public final int find(String paramString) {
        return findVariable(paramString);
    }

    public final int size() {
        return this.varCount;
    }

    public final String[] getNames() {
        String[] arrayOfString = new String[0];
        if (this.varCount == 0)
            return arrayOfString;
        arrayOfString = new String[this.varCount];
        System.arraycopy(this.varName, 0, arrayOfString, 0, this.varCount);
        return arrayOfString;
    }

    public final String getName(int paramInt) {
        if ((paramInt > 0) && (paramInt <= this.varCount))
            return this.varName[(paramInt - 1)];
        return null;
    }

    public final boolean exists(String paramString) {
        if (paramString != null) {
            if (paramString.equalsIgnoreCase("SYS_DATE"))
                return true;
            if (paramString.equalsIgnoreCase("JAVA_VERSION"))
                return true;
            if (paramString.equalsIgnoreCase("FILE_ENCODING"))
                return true;
            if (paramString.equalsIgnoreCase("SYS_TIME"))
                return true;
            if (paramString.equalsIgnoreCase("SYS_DATETIME"))
                return true;
            if (paramString.equalsIgnoreCase("NLS_DATE_FORMAT"))
                return true;
            if (paramString.equalsIgnoreCase("CURRENT_DATE"))
                return true;
        }
        return findVariable(paramString) != -1;
    }

    public final Object getValue(String paramString) {
        if (paramString != null) {
            if (paramString.equalsIgnoreCase("SYS_DATE"))
                return new Date(System.currentTimeMillis());
            if (paramString.equalsIgnoreCase("SYS_TIME"))
                return new Time(System.currentTimeMillis());
            if (paramString.equalsIgnoreCase("JAVA_VERSION"))
                return JavaVM.VERSION;
            if (paramString.equalsIgnoreCase("FILE_ENCODING"))
                return JavaVM.ENCODING;
            if (paramString.equalsIgnoreCase("SYS_DATETIME"))
                return new Timestamp(System.currentTimeMillis());
            if (paramString.equalsIgnoreCase("NLS_DATE_FORMAT"))
                return this.dateFormat;
            if (paramString.equalsIgnoreCase("CURRENT_DATE"))
                return DateOperator.getDay(this.dateFormat);
        }
        int i = findVariable(paramString);
        if (i == -1)
            return null;
        Object localObject = this.varValue[i];
        return localObject;
    }

    public final String getString(String paramString) {
        Object localObject = getValue(paramString);
        if (localObject != null)
            return localObject.toString();
        return null;
    }

    public final String getString(String paramString1, String paramString2) {
        Object localObject = getValue(paramString1);
        if (localObject != null)
            return localObject.toString();
        return paramString2;
    }

    public final int getInt(String paramString, int paramInt) {
        String str = getString(paramString);
        if (str == null)
            return paramInt;
        try {
            return Integer.valueOf(str).intValue();
        } catch (NumberFormatException localNumberFormatException) {
        }
        return paramInt;
    }

    public final long getLong(String paramString, long paramLong) {
        String str = getString(paramString);
        if (str == null)
            return paramLong;
        try {
            return Long.valueOf(str).longValue();
        } catch (NumberFormatException localNumberFormatException) {
        }
        return paramLong;
    }

    public final float getFloat(String paramString, float paramFloat) {
        String str = getString(paramString);
        if (str == null)
            return paramFloat;
        try {
            return Float.valueOf(str).floatValue();
        } catch (NumberFormatException localNumberFormatException) {
        }
        return paramFloat;
    }

    public final double getDouble(String paramString, double paramDouble) {
        String str = getString(paramString);
        if (str == null)
            return paramDouble;
        try {
            return Double.valueOf(str).doubleValue();
        } catch (NumberFormatException localNumberFormatException) {
        }
        return paramDouble;
    }

    public final boolean getBoolean(String paramString, boolean paramBoolean) {
        String str = getString(paramString);
        if (str == null)
            return paramBoolean;
        return Boolean.valueOf(str).booleanValue();
    }

    public final void setType(String paramString, int paramInt) {
        if (paramString != null) {
            if (paramString.equalsIgnoreCase("SYS_DATE"))
                return;
            if (paramString.equalsIgnoreCase("SYS_TIME"))
                return;
            if (paramString.equalsIgnoreCase("JAVA_VERSION"))
                return;
            if (paramString.equalsIgnoreCase("FILE_ENCODING"))
                return;
            if (paramString.equalsIgnoreCase("SYS_DATETIME"))
                return;
            if (paramString.equalsIgnoreCase("NLS_DATE_FORMAT"))
                return;
            if (paramString.equalsIgnoreCase("CURRENT_DATE"))
                return;
        }
        int i = findVariable(paramString);
        if (i == -1)
            return;
        this.varType[i] = paramInt;
    }

    public final int getType(String paramString) {
        if (paramString != null) {
            if (paramString.equalsIgnoreCase("SYS_DATE"))
                return 91;
            if (paramString.equalsIgnoreCase("SYS_TIME"))
                return 92;
            if (paramString.equalsIgnoreCase("JAVA_VERSION"))
                return 12;
            if (paramString.equalsIgnoreCase("FILE_ENCODING"))
                return 12;
            if (paramString.equalsIgnoreCase("SYS_DATETIME"))
                return 93;
            if (paramString.equalsIgnoreCase("NLS_DATE_FORMAT"))
                return 12;
            if (paramString.equalsIgnoreCase("CURRENT_DATE"))
                return 12;
        }
        int i = findVariable(paramString);
        if (i == -1)
            return 12;
        return this.varType[i];
    }

    public final void setValue(String paramString, Object paramObject)
            throws NumberFormatException {
        if (paramString != null) {
            if (paramString.equalsIgnoreCase("SYS_DATE"))
                return;
            if (paramString.equalsIgnoreCase("SYS_TIME"))
                return;
            if (paramString.equalsIgnoreCase("JAVA_VERSION"))
                return;
            if (paramString.equalsIgnoreCase("FILE_ENCODING"))
                return;
            if (paramString.equalsIgnoreCase("SYS_DATETIME"))
                return;
            if (paramString.equalsIgnoreCase("CURRENT_DATE"))
                return;
            if (paramString.equalsIgnoreCase("NLS_DATE_FORMAT")) {
                if (paramObject != null)
                    this.dateFormat = paramObject.toString();
                else
                    this.dateFormat = "yyyy-MM-dd HH:mm:ss";
                return;
            }
        }
        int i = findVariable(paramString);
        if (i == -1)
            return;
        if (paramObject == null) {
            this.varValue[i] = null;
        } else {
            Object localObject = paramObject;
            if ((paramObject instanceof String))
                if (paramObject.toString().equals("${today}"))
                    localObject = DateOperator.getDay("yyyyMMdd");
                else if (paramObject.toString().startsWith("${today}+"))
                    localObject = DateOperator.addDays(DateOperator.getDay("yyyyMMdd"), Integer.valueOf(paramObject.toString().substring(9)).intValue());
                else if (paramObject.toString().startsWith("${today}-"))
                    localObject = DateOperator.addDays(DateOperator.getDay("yyyyMMdd"), Integer.valueOf(paramObject.toString().substring(8)).intValue());
                else if (paramObject.toString().equals("${month}"))
                    localObject = DateOperator.getDay("yyyyMM");
                else if (paramObject.toString().startsWith("${month}+"))
                    localObject = DateOperator.addMonths(DateOperator.getDay("yyyyMMdd"), Integer.valueOf(paramObject.toString().substring(9)).intValue()).substring(0, 6);
                else if (paramObject.toString().startsWith("${month}-"))
                    localObject = DateOperator.addMonths(DateOperator.getDay("yyyyMMdd"), Integer.valueOf(paramObject.toString().substring(8)).intValue()).substring(0, 6);
                else if (paramObject.toString().equals("${year}"))
                    localObject = DateOperator.getDay("yyyy");
                else if (paramObject.toString().startsWith("${year}+"))
                    localObject = DateOperator.addMonths(DateOperator.getDay("yyyyMMdd"), Integer.valueOf(paramObject.toString().substring(8)).intValue() * 12).substring(0, 4);
                else if (paramObject.toString().startsWith("${year}-"))
                    localObject = DateOperator.addMonths(DateOperator.getDay("yyyyMMdd"), Integer.valueOf(paramObject.toString().substring(7)).intValue() * 12).substring(0, 4);
                else
                    localObject = (String) paramObject;
            if (this.varType[i] != -999999)
                this.varValue[i] = SQLTypes.getValue(this.varType[i], localObject);
            else
                this.varValue[i] = localObject;
        }
    }

    public final synchronized void add(String paramString, int paramInt) {
        if (paramString == null)
            return;
        if (paramString != null) {
            if (paramString.equalsIgnoreCase("SYS_DATE"))
                return;
            if (paramString.equalsIgnoreCase("SYS_TIME"))
                return;
            if (paramString.equalsIgnoreCase("JAVA_VERSION"))
                return;
            if (paramString.equalsIgnoreCase("FILE_ENCODING"))
                return;
            if (paramString.equalsIgnoreCase("SYS_DATETIME"))
                return;
            if (paramString.equalsIgnoreCase("NLS_DATE_FORMAT"))
                return;
            if (paramString.equalsIgnoreCase("CURRENT_DATE"))
                return;
        }
        int i = findVariable(paramString);
        if (i >= 0)
            return;
        if (this.varCount == this.varName.length) {
            String[] arrayOfString = this.varName;
            int[] arrayOfInt = this.varType;
            Object[] arrayOfObject = this.varValue;
            this.varName = new String[this.varCount + 50];
            this.varType = new int[this.varCount + 50];
            this.varValue = new Object[this.varCount + 50];
            System.arraycopy(arrayOfString, 0, this.varName, 0, this.varCount);
            System.arraycopy(arrayOfInt, 0, this.varType, 0, this.varCount);
            System.arraycopy(arrayOfObject, 0, this.varValue, 0, this.varCount);
        }
        this.varName[this.varCount] = paramString.toUpperCase();
        this.varType[this.varCount] = paramInt;
        this.varIndex.put(this.varName[this.varCount], new Integer(this.varCount));
        this.varCount += 1;
    }

    public final synchronized void remove(String paramString) {
        int i = findVariable(paramString);
        if (i == -1)
            return;
        this.varIndex.remove(this.varName[i]);
        if (this.varCount - i - 1 > 0) {
            System.arraycopy(this.varName, i + 1, this.varName, i, this.varCount - i - 1);
            System.arraycopy(this.varType, i + 1, this.varType, i, this.varCount - i - 1);
            System.arraycopy(this.varValue, i + 1, this.varValue, i, this.varCount - i - 1);
        }
        this.varCount -= 1;
        for (int j = i; i < this.varCount; j++) {
            this.varIndex.remove(this.varName[i]);
            this.varIndex.put(this.varName[i], new Integer(j));
        }
        this.varName[this.varCount] = null;
        this.varValue[this.varCount] = null;
    }

    public final synchronized void removeAll() {
        this.varIndex.clear();
        this.varName = new String[50];
        this.varType = new int[50];
        this.varValue = new Object[50];
        this.varCount = 0;
    }

    public final void loadURL(String paramString) {
        try {
            URL localURL = getClass().getResource(paramString);
            if (localURL == null)
                return;
            load(new BufferedReader(new InputStreamReader(localURL.openStream())));
        } catch (IOException localIOException) {
        }
    }

    public final void loadFile(String paramString) {
        try {
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(paramString)));
            load(localBufferedReader);
        } catch (IOException localIOException) {
        }
    }

    public final void loadInputStream(InputStream paramInputStream) {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(paramInputStream));
        load(localBufferedReader);
    }

    private final void load(BufferedReader paramBufferedReader) {
        int i = 0;
        int j = 0;
        String str1 = "";
        String str2 = "";
        try {
            if (paramBufferedReader == null)
                return;
            while ((str1 = paramBufferedReader.readLine()) != null) {
                if ((str1.trim().length() == 0) || (str1.trim().substring(0, 1).equals("#")))
                    continue;
                char[] arrayOfChar = str1.toCharArray();
                for (j = arrayOfChar.length; (j > 0) && ((arrayOfChar[(j - 1)] == ' ') || (arrayOfChar[(j - 1)] == '\t')); j--)
                    ;
                str1 = String.valueOf(arrayOfChar, 0, j);
                j = 0;
                if (str1.endsWith("\\")) {
                    str2 = str2 + str1.substring(0, str1.length() - 1) + "\n";
                    continue;
                }
                str2 = str2 + str1;
                j = str2.indexOf("=");
                if (j > 0) {
                    addFromString(str2, j);
                }
                str2 = "";
            }
            if (str2.length() > 0) {
                j = str2.indexOf("=");
                if (j > 0) {
                    addFromString(str2, j);
                }
            }
        } catch (IOException localIOException1) {
        }
        try {
            if (paramBufferedReader != null)
                paramBufferedReader.close();
        } catch (IOException localIOException2) {
        }
    }

    public void addFromString(String str2, int length) {
        add(str2.substring(0, length).trim().toUpperCase(), 12);
        if (length == str2.length() - 1)
            setValue(str2.substring(0, length).trim().toUpperCase(), "");
        else
            setValue(str2.substring(0, length).trim().toUpperCase(), str2.substring(length + 1));
    }

    private final boolean isVariableFirst(char paramChar) {
        if (paramChar == '_')
            return true;
        if ((paramChar >= 'a') && (paramChar <= 'z'))
            return true;
        return (paramChar >= 'A') && (paramChar <= 'Z');
    }

    private final boolean isVariableChar(char paramChar) {
        if (paramChar == '_')
            return true;
        if ((paramChar >= '0') && (paramChar <= '9'))
            return true;
        if ((paramChar >= 'a') && (paramChar <= 'z'))
            return true;
        return (paramChar >= 'A') && (paramChar <= 'Z');
    }

    public final String parseString(String paramString, char paramChar1, char paramChar2) {
        if (paramString == null)
            return null;
        String str = "";
        StringBuffer localStringBuffer = new StringBuffer();
        char[] arrayOfChar = paramString.toCharArray();
        int i = 0;
        while (i < arrayOfChar.length) {
            if ((arrayOfChar[i] != paramChar1) && (arrayOfChar[i] != paramChar2)) {
                localStringBuffer.append(arrayOfChar[i]);
                i++;
                continue;
            }
            if (arrayOfChar[i] == paramChar2) {
                if (i + 1 < arrayOfChar.length) {
                    if (arrayOfChar[(i + 1)] == paramChar1) {
                        localStringBuffer.append(arrayOfChar[(i + 1)]);
                        i += 2;
                        continue;
                    }
                    localStringBuffer.append(arrayOfChar[i]);
                    i++;
                    continue;
                }
                localStringBuffer.append(arrayOfChar[i]);
                i++;
                continue;
            }
            if (i + 1 < arrayOfChar.length) {
                int j;
                int k;
                if (arrayOfChar[(i + 1)] == '{') {
                    j = i + 2;
                    for (k = i + 2; (k < arrayOfChar.length) && (arrayOfChar[k] != '}'); k++) ;
                    if (j < arrayOfChar.length)
                        if (k < arrayOfChar.length) {
                            str = String.valueOf(arrayOfChar, j, k - j);
                            if (exists(str)) {
                                localStringBuffer.append(getString(str, ""));
                            } else {
                                localStringBuffer.append("&{");
                                localStringBuffer.append(str);
                                localStringBuffer.append("}");
                            }
                        } else {
                            str = String.valueOf(arrayOfChar, j, arrayOfChar.length - j);
                            if (exists(str)) {
                                localStringBuffer.append(getString(str, ""));
                            } else {
                                localStringBuffer.append("&{");
                                localStringBuffer.append(str);
                                localStringBuffer.append("}");
                            }
                        }
                    i = k + 1;
                    continue;
                }
                if (isVariableFirst(arrayOfChar[(i + 1)])) {
                    j = i + 1;
                    for (k = i + 1; (k < arrayOfChar.length) && (isVariableChar(arrayOfChar[k])); k++)
                        ;
                    if (j < arrayOfChar.length)
                        if (k < arrayOfChar.length) {
                            str = String.valueOf(arrayOfChar, j, k - j);
                            if (exists(str)) {
                                localStringBuffer.append(getString(str, ""));
                            } else {
                                localStringBuffer.append("&");
                                localStringBuffer.append(str);
                            }
                        } else {
                            str = String.valueOf(arrayOfChar, j, arrayOfChar.length - j);
                            if (exists(str)) {
                                localStringBuffer.append(getString(str, ""));
                            } else {
                                localStringBuffer.append("&");
                                localStringBuffer.append(str);
                            }
                        }
                    if ((k < arrayOfChar.length) && (arrayOfChar[k] == '.')) {
                        i = k + 1;
                        continue;
                    }
                    i = k;
                    continue;
                }
                localStringBuffer.append(arrayOfChar[i]);
                i++;
                continue;
            }
            i++;
        }
        return localStringBuffer.toString();
    }

    public final String parseString(String paramString, char paramChar) {
        return parseString(paramString, paramChar, '\\');
    }

    public final String parseString(String paramString) {
        return parseString(paramString, '$', '\\');
    }
}
