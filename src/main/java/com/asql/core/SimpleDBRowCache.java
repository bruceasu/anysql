package com.asql.core;

import com.asql.core.util.TextUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import java.util.Vector;

public final class SimpleDBRowCache
        implements DBRowCache, Comparator<Object> {
    private int column_count = 0;
    private String[] column_label = new String[10];
    private String[] column_name = new String[10];
    private int[] column_type = new int[10];
    private int[] column_size = new int[10];
    private Vector cache_data = new Vector(100, 100);
    private SimpleDateFormat sdftDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdftTime = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat sdftTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private SimpleDateFormat sdftTimestampTz = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
    private int sort_column = 1;
    private boolean sort_asc = true;

    public final int findColumn(String paramString) {
        if (paramString == null)
            return 0;
        int i = 0;
        for (;
             (i < this.column_count)
                && ((this.column_name[i] == null) || (!this.column_name[i].equalsIgnoreCase(paramString)))
                && ((this.column_label[i] == null) || (!this.column_label[i].equalsIgnoreCase(paramString)));
             i++) {}
        return i == this.column_count ? 0 : i + 1;
    }

    public final int getColumnCount() {
        return this.column_count;
    }

    public final synchronized void removeAllColumn() {
        this.cache_data.removeAllElements();
        this.column_name = new String[10];
        this.column_label = new String[10];
        this.column_type = new int[10];
        this.column_size = new int[10];
        this.column_count = 0;
    }

    public final String getColumnName(int paramInt) {
        if ((paramInt < 1) || (paramInt > this.column_count))
            return null;
        return this.column_name[(paramInt - 1)];
    }

    public final String getColumnLabel(int paramInt) {
        if ((paramInt < 1) || (paramInt > this.column_count))
            return null;
        return this.column_label[(paramInt - 1)];
    }

    public final String getColumnLabel(String paramString) {
        int i = findColumn(paramString);
        if ((i < 1) || (i > this.column_count))
            return null;
        return this.column_label[(i - 1)];
    }

    public final void setColumnLabel(int paramInt, String paramString) {
        if ((paramInt < 1) || (paramInt > this.column_count))
            return;
        this.column_label[(paramInt - 1)] = paramString;
    }

    public final void setColumnLabel(String paramString1, String paramString2) {
        int i = findColumn(paramString1);
        if ((i < 1) || (i > this.column_count))
            return;
        this.column_label[(i - 1)] = paramString2;
    }

    public final void setColumnType(int paramInt1, int paramInt2) {
        if ((paramInt1 < 1) || (paramInt1 > this.column_count))
            return;
        this.column_type[(paramInt1 - 1)] = paramInt2;
    }

    public final void setColumnType(String paramString, int paramInt) {
        int i = findColumn(paramString);
        if ((i < 1) || (i > this.column_count))
            return;
        this.column_type[(i - 1)] = paramInt;
    }

    public final void setColumnSize(int paramInt1, int paramInt2) {
        if ((paramInt1 < 1) || (paramInt1 > this.column_count))
            return;
        this.column_size[(paramInt1 - 1)] = paramInt2;
    }

    public final void setColumnSize(String paramString, int paramInt) {
        int i = findColumn(paramString);
        if ((i < 1) || (i > this.column_count))
            return;
        this.column_size[(i - 1)] = paramInt;
    }

    public final int getColumnType(int paramInt) {
        if ((paramInt < 1) || (paramInt > this.column_count))
            return 12;
        return this.column_type[(paramInt - 1)];
    }

    public final int getColumnType(String paramString) {
        return getColumnType(findColumn(paramString));
    }

    public final int getColumnSize(int paramInt) {
        if ((paramInt < 1) || (paramInt > this.column_count))
            return 4;
        return this.column_size[(paramInt - 1)];
    }

    public final int getColumnSize(String paramString) {
        return getColumnSize(findColumn(paramString));
    }

    public final synchronized void addColumn(String paramString, int paramInt) {
        if ((paramString == null) || (paramString.trim().length() == 0))
            return;
        int i = findColumn(paramString);
        if (i > 0)
            return;
        if (this.column_count == this.column_name.length) {
            String[] arrayOfString1 = this.column_label;
            String[] arrayOfString2 = this.column_name;
            int[] arrayOfInt1 = this.column_type;
            int[] arrayOfInt2 = this.column_size;
            this.column_label = new String[this.column_count + 10];
            this.column_name = new String[this.column_count + 10];
            this.column_type = new int[this.column_count + 10];
            this.column_size = new int[this.column_count + 10];
            System.arraycopy(arrayOfString1, 0, this.column_label, 0, this.column_count);
            System.arraycopy(arrayOfString2, 0, this.column_name, 0, this.column_count);
            System.arraycopy(arrayOfInt1, 0, this.column_type, 0, this.column_count);
            System.arraycopy(arrayOfInt2, 0, this.column_size, 0, this.column_count);
            this.column_label[this.column_count] = paramString.trim();
            this.column_name[this.column_count] = paramString.trim();
            this.column_type[this.column_count] = paramInt;
            this.column_size[this.column_count] = this.column_name[this.column_count].length();
            this.column_count += 1;
            for (i = 0; i < this.cache_data.size(); i++) {
                Object[] arrayOfObject2 = (Object[]) this.cache_data.elementAt(i);
                Object[] arrayOfObject1 = new Object[this.column_name.length];
                System.arraycopy(arrayOfObject2, 0, arrayOfObject1, 0, arrayOfObject2.length);
                this.cache_data.setElementAt(arrayOfObject1, i);
            }
        } else {
            this.column_label[this.column_count] = paramString.trim();
            this.column_name[this.column_count] = paramString.trim();
            this.column_type[this.column_count] = paramInt;
            this.column_size[this.column_count] = this.column_name[this.column_count].length();
            this.column_count += 1;
        }
    }

    public int getRowCount() {
        return this.cache_data.size();
    }

    public void setItem(int paramInt1, int paramInt2, Object paramObject) {
        if ((paramInt2 < 1) || (paramInt2 > this.column_count))
            return;
        if ((paramInt1 < 1) || (paramInt1 > getRowCount()))
            return;
        Object[] arrayOfObject = null;
        arrayOfObject = (Object[]) this.cache_data.elementAt(paramInt1 - 1);
        arrayOfObject[(paramInt2 - 1)] = paramObject;
    }

    public void setItem(int paramInt, String paramString, Object paramObject) {
        setItem(paramInt, findColumn(paramString), paramObject);
    }

    public Object getItem(int paramInt1, int paramInt2) {
        if ((paramInt2 < 1) || (paramInt2 > this.column_count))
            return null;
        if ((paramInt1 < 1) || (paramInt1 > getRowCount()))
            return null;
        Object[] arrayOfObject = null;
        arrayOfObject = (Object[]) this.cache_data.elementAt(paramInt1 - 1);
        return arrayOfObject[(paramInt2 - 1)];
    }

    public Object getItem(int paramInt, String paramString) {
        return getItem(paramInt, findColumn(paramString));
    }

    public String getString(int paramInt1, int paramInt2) {
        if ((paramInt2 < 1) || (paramInt2 > this.column_count))
            return null;
        if ((paramInt1 < 1) || (paramInt1 > getRowCount()))
            return null;
        Object[] arrayOfObject = null;
        arrayOfObject = (Object[]) this.cache_data.elementAt(paramInt1 - 1);
        if (arrayOfObject[(paramInt2 - 1)] != null)
            return arrayOfObject[(paramInt2 - 1)].toString();
        return null;
    }

    public String getString(int paramInt, String paramString) {
        return getString(paramInt, findColumn(paramString));
    }

    public Object[] getRow(int paramInt) {
        Object[] arrayOfObject = new Object[0];
        if (this.column_count < 1)
            return arrayOfObject;
        if ((paramInt > this.cache_data.size()) || (paramInt < 1))
            return arrayOfObject;
        arrayOfObject = (Object[]) this.cache_data.elementAt(paramInt - 1);
        return arrayOfObject;
    }

    public synchronized void setRow(int paramInt, Object[] paramArrayOfObject) {
        if (this.column_count == 0)
            return;
        if ((paramInt < 1) || (paramInt > getRowCount()))
            return;
        Object[] arrayOfObject = paramArrayOfObject;
        if (paramArrayOfObject.length > this.column_name.length) {
            arrayOfObject = new Object[this.column_name.length];
            System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, this.column_name.length);
        } else if (paramArrayOfObject.length < this.column_name.length) {
            arrayOfObject = new Object[this.column_name.length];
            System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, paramArrayOfObject.length);
        }
        this.cache_data.setElementAt(arrayOfObject, paramInt - 1);
    }

    public synchronized int insertRow(int paramInt) {
        if (this.column_count == 0)
            return 0;
        Object[] arrayOfObject = new Object[this.column_name.length];
        if (paramInt <= 1) {
            this.cache_data.insertElementAt(arrayOfObject, 0);
            return 1;
        }
        if (paramInt >= this.cache_data.size()) {
            this.cache_data.addElement(arrayOfObject);
            return this.cache_data.size();
        }
        this.cache_data.insertElementAt(arrayOfObject, paramInt);
        return paramInt;
    }

    public synchronized int insertRow(int paramInt, Object[] paramArrayOfObject) {
        if (this.column_count == 0)
            return 0;
        Object[] arrayOfObject = paramArrayOfObject;
        if (paramArrayOfObject.length > this.column_name.length) {
            arrayOfObject = new Object[this.column_name.length];
            System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, this.column_name.length);
        } else if (paramArrayOfObject.length < this.column_name.length) {
            arrayOfObject = new Object[this.column_name.length];
            System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, paramArrayOfObject.length);
        }
        if (paramInt <= 1) {
            this.cache_data.insertElementAt(arrayOfObject, 0);
            return 1;
        }
        if (paramInt >= this.cache_data.size()) {
            this.cache_data.addElement(arrayOfObject);
            return this.cache_data.size();
        }
        this.cache_data.insertElementAt(arrayOfObject, paramInt);
        return paramInt;
    }

    public synchronized int appendRow() {
        if (this.column_count == 0)
            return 0;
        Object[] arrayOfObject = new Object[this.column_name.length];
        this.cache_data.addElement(arrayOfObject);
        return this.cache_data.size();
    }

    public synchronized int appendRow(Object[] paramArrayOfObject) {
        if (this.column_count == 0)
            return 0;
        Object[] arrayOfObject = paramArrayOfObject;
        if (paramArrayOfObject.length > this.column_name.length) {
            arrayOfObject = new Object[this.column_name.length];
            System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, this.column_name.length);
        } else if (paramArrayOfObject.length < this.column_name.length) {
            arrayOfObject = new Object[this.column_name.length];
            System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, paramArrayOfObject.length);
        }
        this.cache_data.addElement(arrayOfObject);
        return this.cache_data.size();
    }

    public int getWidth(boolean paramBoolean) {
        int i = 0;
        Object[] arrayOfObject = null;
        for (int j = 0; j < this.column_count; j++) {
            if (this.column_name[j].getBytes().length <= this.column_size[j])
                continue;
            this.column_size[j] = this.column_name[j].getBytes().length;
        }
        for (int j = 1; j <= getRowCount(); j++) {
            arrayOfObject = (Object[]) this.cache_data.elementAt(j - 1);
            for (int k = 0; k < this.column_count; k++)
                if (this.column_type[k] == 91) {
                    if (this.column_size[k] >= 10)
                        continue;
                    this.column_size[k] = 10;
                } else if (this.column_type[k] == 92) {
                    if (this.column_size[k] >= 8)
                        continue;
                    this.column_size[k] = 8;
                } else if (this.column_type[k] == 93) {
                    if (this.column_size[k] >= 23)
                        continue;
                    this.column_size[k] = 23;
                } else if ((this.column_type[k] == -101) || (this.column_type[k] == -102)) {
                    if (this.column_size[k] >= 29)
                        continue;
                    this.column_size[k] = 29;
                } else {
                    int m = 0;
                    if ((arrayOfObject[k] == null) || ((m = arrayOfObject[k].toString().getBytes().length) <= this.column_size[k]))
                        continue;
                    this.column_size[k] = m;
                }
        }
        for (int j = 0; j < this.column_count; j++)
            i += this.column_size[j];
        if ((paramBoolean) && (i > 0))
            for (int j = 0; j < this.column_count; j++)
                this.column_size[j] = (int) (100.0D * this.column_size[j] / i);
        return i;
    }

    public synchronized void deleteRow(int paramInt) {
        if ((paramInt < 1) || (paramInt > getRowCount()))
            return;
        this.cache_data.removeElementAt(paramInt - 1);
    }

    public synchronized void deleteRow(int paramInt1, int paramInt2) {
        if ((paramInt1 > 0) && (paramInt2 <= getRowCount()) && (paramInt1 <= paramInt2))
            for (int i = paramInt1; i <= paramInt2; i++)
                this.cache_data.removeElementAt(i - 1);
    }

    public synchronized void deleteAllRow() {
        this.cache_data.removeAllElements();
    }

    public double min(String paramString, double paramDouble) {
        double d1 = paramDouble;
        double d2 = paramDouble;
        int j;
        if ((j = findColumn(paramString)) == 0)
            return paramDouble;
        try {
            for (int i = 1; i <= getRowCount(); i++) {
                if (getItem(i, j) == null)
                    continue;
                d2 = Double.valueOf(getItem(i, j).toString()).doubleValue();
                if (d2 >= d1)
                    continue;
                d1 = d2;
            }
        } catch (NumberFormatException localNumberFormatException) {
        }
        return d1;
    }

    public double min(String[] paramArrayOfString, double paramDouble) {
        double d1 = paramDouble;
        double d2 = paramDouble;
        for (int i = 0; i < paramArrayOfString.length; i++) {
            d2 = min(paramArrayOfString[i], d2);
            if (d2 >= d1)
                continue;
            d1 = d2;
        }
        return d1;
    }

    public double max(String paramString, double paramDouble) {
        double d1 = paramDouble;
        double d2 = paramDouble;
        int j;
        if ((j = findColumn(paramString)) == 0)
            return paramDouble;
        try {
            for (int i = 1; i <= getRowCount(); i++) {
                if (getItem(i, j) == null)
                    continue;
                d2 = Double.valueOf(getItem(i, j).toString()).doubleValue();
                if (d2 <= d1)
                    continue;
                d1 = d2;
            }
        } catch (NumberFormatException localNumberFormatException) {
        }
        return d1;
    }

    public double max(String[] paramArrayOfString, double paramDouble) {
        double d1 = paramDouble;
        double d2 = paramDouble;
        for (int i = 0; i < paramArrayOfString.length; i++) {
            d2 = max(paramArrayOfString[i], d2);
            if (d2 <= d1)
                continue;
            d1 = d2;
        }
        return d1;
    }

    public double avg(String paramString) {
        int i = count(paramString);
        if (i > 0)
            return sum(paramString) / i;
        return 0.0D;
    }

    public double sum(String paramString) {
        double d = 0.0D;
        int i = 0;
        int j = 0;
        if ((j = findColumn(paramString)) == 0)
            return 0.0D;
        try {
            for (i = 1; i <= getRowCount(); i++) {
                if (getItem(i, j) == null)
                    continue;
                d += Double.valueOf(getItem(i, j).toString()).doubleValue();
            }
        } catch (NumberFormatException localNumberFormatException) {
        }
        return d;
    }

    public int count(String paramString) {
        int i = 0;
        int j = 0;
        int k = 0;
        if ((j = findColumn(paramString)) == 0)
            return 0;
        for (i = 1; i <= getRowCount(); i++) {
            if (getItem(i, j) == null)
                continue;
            k++;
        }
        return k;
    }

    public int count() {
        return getRowCount();
    }

    private String readLine(BufferedReader paramBufferedReader, String paramString)
            throws IOException {
        String str1 = null;
        String str2;
        while ((str2 = paramBufferedReader.readLine()) != null) {
            if (str1 == null)
                str1 = "";
            if (str2.endsWith(paramString)) {
                str1 = str1 + "\n" + str2.substring(0, str2.length() - paramString.length());
                continue;
            }
            str1 = str1 + "\n" + str2;
        }
        return str1;
    }

    public int read(BufferedReader paramBufferedReader, int paramInt)
            throws IOException {
        return read(paramBufferedReader, "\t", paramInt);
    }

    public int read(BufferedReader paramBufferedReader, String paramString, int paramInt)
            throws IOException {
        String str;
        int i = getRowCount();
        for (; (i < paramInt) && ((str = paramBufferedReader.readLine()) != null); i++) {
            Object[] arrayOfObject = TextUtils.getFields(str, paramString).toArray();
            for (int j = 0; (j < arrayOfObject.length) && (j < getColumnCount()); j++)
                arrayOfObject[j] = SQLTypes.getValue(getColumnType(j + 1), arrayOfObject[j]);
            appendRow(arrayOfObject);
        }
        return i;
    }

    public int read(BufferedReader paramBufferedReader, String paramString1, String paramString2, int paramInt)
            throws IOException {
        String str;
        int i = getRowCount();
        for (; (i < paramInt) && ((str = readLine(paramBufferedReader, paramString2)) != null); i++) {
            Object[] arrayOfObject = TextUtils.getFields(str, paramString1).toArray();
            for (int j = 0; (j < arrayOfObject.length) && (j < getColumnCount()); j++)
                arrayOfObject[j] = SQLTypes.getValue(getColumnType(j + 1), arrayOfObject[j]);
            appendRow(arrayOfObject);
        }
        return i;
    }

    public void writeXMLBody(Writer paramWriter)
            throws IOException {
        writeXMLBody(paramWriter, "dataset", "", this.column_count);
    }

    public void writeXMLBody(Writer paramWriter, int paramInt)
            throws IOException {
        writeXMLBody(paramWriter, "dataset", "", paramInt);
    }

    public void writeXMLBody(Writer paramWriter, String paramString, int paramInt)
            throws IOException {
        writeXMLBody(paramWriter, paramString, "", paramInt);
    }

    public void writeXMLBody(Writer paramWriter, String paramString1, String paramString2, int paramInt)
            throws IOException {
        int k = 0;
        if ((paramString2 != null) && (paramString2.trim().length() > 0))
            paramWriter.write("\t<" + paramString1 + " " + paramString2 + ">\n");
        else
            paramWriter.write("\t<" + paramString1 + ">\n");
        k = getWidth(false);
        paramWriter.write("\t\t<head len=\"" + k + "\">\n");
        for (int i = 1; i <= getColumnCount(); i++) {
            paramWriter.write("\t\t\t<col id=\"" + i + "\" type=\"" + SQLTypes.getTypeName(this.column_type[(i - 1)]) + "\" size=\"" + (int) (100.0D * this.column_size[(i - 1)] / k) + "%\" len=\"" + this.column_size[(i - 1)] + "\" ");
            if ((this.column_type[(i - 1)] == -1) || (this.column_type[(i - 1)] == 12) || (this.column_type[(i - 1)] == 1)) {
                if (this.column_size[(i - 1)] < 20)
                    paramWriter.write("align=\"center\">\n");
                else
                    paramWriter.write("align=\"left\">\n");
            } else if ((this.column_type[(i - 1)] == 91) || (this.column_type[(i - 1)] == 92) || (this.column_type[(i - 1)] == 93))
                paramWriter.write("align=\"center\">\n");
            else
                paramWriter.write("align=\"right\">\n");
            paramWriter.write("\t\t\t\t<name><![CDATA[" + this.column_name[(i - 1)] + "]]></name>\n");
            paramWriter.write("\t\t\t\t<label><![CDATA[" + this.column_label[(i - 1)] + "]]></label>\n");
            paramWriter.write("\t\t\t</col>\n");
        }
        paramWriter.write("\t\t</head>\n");
        boolean[] arrayOfBoolean = new boolean[this.column_count];
        int[] arrayOfInt1 = new int[this.column_count];
        for (int i = 1; i <= getRowCount(); i++) {
            paramWriter.write("\t\t<row id=\"" + i + "\">\n");
            Object[] arrayOfObject = (Object[]) this.cache_data.elementAt(i - 1);
            for (int j = 1; j <= getColumnCount(); j++) {
                paramWriter.write("\t\t\t<col id=\"" + j + "\" ");
                if (j <= paramInt) {
                    arrayOfBoolean[(j - 1)] = rowEquals(i, i - 1, j);
                    if (!arrayOfBoolean[(j - 1)]) {
                        int[] arrayOfInt2 = new int[j];
                        for (int m = 0; m < j; m++)
                            arrayOfInt2[m] = (m + 1);
                        arrayOfInt1[(j - 1)] = countgroup(arrayOfInt2, i);
                    }
                    if (arrayOfBoolean[(j - 1)])
                        paramWriter.write("grp=\"0\" ");
                    else
                        paramWriter.write("grp=\"" + arrayOfInt1[(j - 1)] + "\" ");
                }
                if ((this.column_type[(j - 1)] == -1) || (this.column_type[(j - 1)] == 12) || (this.column_type[(j - 1)] == 1))
                    paramWriter.write("><![CDATA[");
                else
                    paramWriter.write(">");
                if (arrayOfObject[(j - 1)] != null)
                    paramWriter.write(arrayOfObject[(j - 1)].toString());
                if ((this.column_type[(j - 1)] == -1) || (this.column_type[(j - 1)] == 12) || (this.column_type[(j - 1)] == 1))
                    paramWriter.write("]]>");
                paramWriter.write("</col>\n");
            }
            paramWriter.write("\t\t</row>\n");
        }
        paramWriter.write("\t</" + paramString1 + ">\n");
    }

    public void write(PrintStream paramPrintStream) {
        write(paramPrintStream, "\t");
    }

    public void write(PrintStream paramPrintStream, int paramInt) {
        write(paramPrintStream, "\t", paramInt);
    }

    public void write(PrintStream paramPrintStream, String paramString) {
        for (int i = 1; i <= getRowCount(); i++) {
            for (int j = 1; j <= this.column_count; j++) {
                Object localObject = getItem(i, j);
                if (localObject != null)
                    paramPrintStream.print(localObject);
                if (j == this.column_count)
                    continue;
                paramPrintStream.print(paramString);
            }
            paramPrintStream.print("\r\n");
        }
    }

    public void write(PrintStream paramPrintStream, String paramString, int paramInt) {
        for (int i = 1; i <= this.column_count; i++) {
            Object localObject = getItem(paramInt, i);
            if (localObject != null)
                paramPrintStream.print(localObject);
            if (i == this.column_count)
                continue;
            paramPrintStream.print(paramString);
        }
    }

    public void write(PrintStream paramPrintStream, String paramString1, String paramString2) {
        for (int i = 1; i <= getRowCount(); i++) {
            for (int j = 1; j <= this.column_count; j++) {
                Object localObject = getItem(i, j);
                if (localObject != null)
                    paramPrintStream.print(localObject);
                if (j == this.column_count)
                    continue;
                paramPrintStream.print(paramString1);
            }
            paramPrintStream.print(paramString2);
            paramPrintStream.print("\r\n");
        }
    }

    public void write(PrintStream paramPrintStream, String paramString1, String paramString2, int paramInt) {
        for (int i = 1; i <= this.column_count; i++) {
            Object localObject = getItem(paramInt, i);
            if (localObject != null)
                paramPrintStream.print(localObject);
            if (i == this.column_count)
                continue;
            paramPrintStream.print(paramString1);
        }
        paramPrintStream.print(paramString2);
    }

    public void write(Writer paramWriter)
            throws IOException {
        write(paramWriter, "\t");
    }

    public void write(Writer paramWriter, int paramInt)
            throws IOException {
        write(paramWriter, "\t", paramInt);
    }

    public void write(Writer paramWriter, String paramString)
            throws IOException {
        for (int i = 1; i <= getRowCount(); i++) {
            for (int j = 1; j <= this.column_count; j++) {
                Object localObject = getItem(i, j);
                if (localObject != null)
                    paramWriter.write(localObject.toString());
                if (j == this.column_count)
                    continue;
                paramWriter.write(paramString);
            }
            paramWriter.write("\r\n");
        }
    }

    public void write(Writer paramWriter, String paramString, int paramInt)
            throws IOException {
        for (int i = 1; i <= this.column_count; i++) {
            Object localObject = getItem(paramInt, i);
            if (localObject != null)
                paramWriter.write(localObject.toString());
            if (i == this.column_count)
                continue;
            paramWriter.write(paramString);
        }
    }

    public void write(Writer paramWriter, String paramString1, String paramString2)
            throws IOException {
        for (int i = 1; i <= getRowCount(); i++) {
            for (int j = 1; j <= this.column_count; j++) {
                Object localObject = getItem(i, j);
                if (localObject != null)
                    paramWriter.write(localObject.toString());
                if (j == this.column_count)
                    continue;
                paramWriter.write(paramString1);
            }
            paramWriter.write(paramString2);
            paramWriter.write("\r\n");
        }
    }

    public void write(Writer paramWriter, String paramString1, String paramString2, int paramInt)
            throws IOException {
        for (int i = 1; i <= this.column_count; i++) {
            Object localObject = getItem(paramInt, i);
            if (localObject != null)
                paramWriter.write(localObject.toString());
            if (i == this.column_count)
                continue;
            paramWriter.write(paramString1);
        }
        paramWriter.write(paramString2);
    }

    protected void finalize()
            throws Throwable {
        this.cache_data.removeAllElements();
    }

    private String getFixedWidth(String paramString, int paramInt, boolean paramBoolean) {
        StringBuffer localStringBuffer = new StringBuffer();
        if ((paramBoolean) && (paramString != null))
            localStringBuffer.append(paramString);
        for (int i = paramString == null ? 0 : paramString.getBytes().length; i < paramInt; i++)
            localStringBuffer.append(" ");
        if ((!paramBoolean) && (paramString != null))
            localStringBuffer.append(paramString);
        return localStringBuffer.toString();
    }

    private String getFixedChar(char paramChar, int paramInt) {
        StringBuffer localStringBuffer = new StringBuffer();
        for (int i = 0; i < paramInt; i++)
            localStringBuffer.append(paramChar);
        return localStringBuffer.toString();
    }

    public String getFixedHeader() {
        int i = 0;
        StringBuffer localStringBuffer = new StringBuffer();
        for (int j = 1; j <= this.column_count; j++) {
            i = this.column_size[(j - 1)];
            if ((j == this.column_count) && (i > 60))
                i = 60;
            switch (this.column_type[(j - 1)]) {
                case -7:
                case -6:
                case -5:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    localStringBuffer.append(getFixedWidth(this.column_label[(j - 1)], i, false));
                    break;
                case -4:
                case -3:
                case -2:
                case -1:
                case 0:
                case 1:
                default:
                    localStringBuffer.append(getFixedWidth(this.column_label[(j - 1)], i, true));
            }
            if (j >= this.column_count)
                continue;
            localStringBuffer.append(" ");
        }
        return localStringBuffer.toString();
    }

    public String getSeperator() {
        int i = 0;
        StringBuffer localStringBuffer = new StringBuffer();
        for (int j = 1; j <= this.column_count; j++) {
            i = this.column_size[(j - 1)];
            if ((j == this.column_count) && (i > 60))
                i = 60;
            localStringBuffer.append(getFixedChar('-', i));
            if (j >= this.column_count)
                continue;
            localStringBuffer.append(" ");
        }
        return localStringBuffer.toString();
    }

    public String getFixedRow(int paramInt) {
        int i = 0;
        StringBuffer localStringBuffer = new StringBuffer();
        if ((paramInt < 1) || (paramInt > getRowCount()))
            return "";
        Object[] arrayOfObject = getRow(paramInt);
        for (int j = 1; j <= this.column_count; j++) {
            i = this.column_size[(j - 1)];
            if ((j == this.column_count) && (i > 60))
                i = 60;
            switch (this.column_type[(j - 1)]) {
                case -7:
                case -6:
                case -5:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    if (arrayOfObject[(j - 1)] != null)
                        localStringBuffer.append(getFixedWidth(arrayOfObject[(j - 1)].toString(), i, false));
                    else
                        localStringBuffer.append(getFixedWidth(null, i, false));
                    break;
                case 91:
                    if (arrayOfObject[(j - 1)] != null)
                        localStringBuffer.append(getFixedWidth(this.sdftDate.format((Date) arrayOfObject[(j - 1)]), i, true));
                    else
                        localStringBuffer.append(getFixedWidth(null, i, false));
                    break;
                case 92:
                    if (arrayOfObject[(j - 1)] != null)
                        localStringBuffer.append(getFixedWidth(this.sdftTime.format((Date) arrayOfObject[(j - 1)]), i, true));
                    else
                        localStringBuffer.append(getFixedWidth(null, i, false));
                    break;
                case 93:
                    if (arrayOfObject[(j - 1)] != null)
                        localStringBuffer.append(getFixedWidth(this.sdftTimestamp.format((Date) arrayOfObject[(j - 1)]), i, true));
                    else
                        localStringBuffer.append(getFixedWidth(null, i, false));
                    break;
                case -102:
                case -101:
                    if (arrayOfObject[(j - 1)] != null)
                        localStringBuffer.append(getFixedWidth(this.sdftTimestampTz.format((Timestamp) arrayOfObject[(j - 1)]), i, true));
                    else
                        localStringBuffer.append(getFixedWidth(null, i, false));
                    break;
                default:
                    if (arrayOfObject[(j - 1)] != null)
                        localStringBuffer.append(getFixedWidth(arrayOfObject[(j - 1)].toString(), i, true));
                    else
                        localStringBuffer.append(getFixedWidth(null, i, true));
            }
            if (j >= this.column_count)
                continue;
            localStringBuffer.append(" ");
        }
        return localStringBuffer.toString();
    }

    public String getSepHeader(String paramString) {
        StringBuffer localStringBuffer = new StringBuffer();
        for (int i = 1; i <= this.column_count; i++) {
            localStringBuffer.append(this.column_label[(i - 1)]);
            if (i >= this.column_count)
                continue;
            localStringBuffer.append(paramString);
        }
        return localStringBuffer.toString();
    }

    public String getSepRow(String paramString, int paramInt) {
        return getString(paramString, paramInt);
    }

    public String getString(int paramInt) {
        return getString("|", paramInt);
    }

    public String getString(String paramString, int paramInt) {
        int i = 0;
        StringBuffer localStringBuffer = new StringBuffer();
        if ((paramInt < 1) || (paramInt > getRowCount()))
            return "";
        Object[] arrayOfObject = getRow(paramInt);
        for (int j = 1; j <= this.column_count; j++) {
            i = this.column_size[(j - 1)];
            if ((j == this.column_count) && (i > 60))
                i = 60;
            switch (this.column_type[(j - 1)]) {
                case -7:
                case -6:
                case -5:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    if (arrayOfObject[(j - 1)] == null)
                        break;
                    localStringBuffer.append(arrayOfObject[(j - 1)].toString());
                    break;
                case 91:
                    if (arrayOfObject[(j - 1)] == null)
                        break;
                    if ((arrayOfObject[(j - 1)] instanceof Date))
                        localStringBuffer.append(this.sdftDate.format((Date) arrayOfObject[(j - 1)]));
                    else
                        localStringBuffer.append(arrayOfObject[(j - 1)].toString());
                    break;
                case 92:
                    if (arrayOfObject[(j - 1)] == null)
                        break;
                    if ((arrayOfObject[(j - 1)] instanceof Date))
                        localStringBuffer.append(this.sdftTime.format((Date) arrayOfObject[(j - 1)]));
                    else
                        localStringBuffer.append(arrayOfObject[(j - 1)].toString());
                    break;
                case 93:
                    if (arrayOfObject[(j - 1)] == null)
                        break;
                    if ((arrayOfObject[(j - 1)] instanceof Date))
                        localStringBuffer.append(this.sdftTimestamp.format((Date) arrayOfObject[(j - 1)]));
                    else
                        localStringBuffer.append(arrayOfObject[(j - 1)].toString());
                    break;
                case -102:
                case -101:
                    if (arrayOfObject[(j - 1)] == null)
                        break;
                    if ((arrayOfObject[(j - 1)] instanceof Date))
                        localStringBuffer.append(this.sdftTimestampTz.format((Date) arrayOfObject[(j - 1)]));
                    else
                        localStringBuffer.append(arrayOfObject[(j - 1)].toString());
                    break;
                default:
                    if (arrayOfObject[(j - 1)] == null)
                        break;
                    localStringBuffer.append(arrayOfObject[(j - 1)].toString());
            }
            if (j >= this.column_count)
                continue;
            localStringBuffer.append(paramString);
        }
        return localStringBuffer.toString();
    }

    public final int find(int paramInt, Object paramObject) {
        return find(paramInt, paramObject, 1);
    }

    public final int find(int paramInt1, Object paramObject, int paramInt2) {
        if ((paramInt1 < 1) || (paramInt1 > this.column_count))
            return 0;
        for (int i = paramInt2; i <= this.cache_data.size(); i++)
            if (paramObject == null) {
                if (getItem(i, paramInt1) == null)
                    return i;
            } else if (paramObject.equals(getItem(i, paramInt1)))
                return i;
        return 0;
    }

    public final int find(int[] paramArrayOfInt, Object[] paramArrayOfObject) {
        return find(paramArrayOfInt, paramArrayOfObject, 1);
    }

    public final int find(int[] paramArrayOfInt, Object[] paramArrayOfObject, int paramInt) {
        int k = 1;
        if (paramArrayOfInt.length != paramArrayOfObject.length)
            return 0;
        for (int i = 0; i < paramArrayOfInt.length; i++)
            if ((paramArrayOfInt[i] < 1) || (paramArrayOfInt[i] > this.column_count))
                return 0;
        for (int i = paramInt; i <= this.cache_data.size(); i++) {
            k = 1;
            for (int j = 0; j < paramArrayOfInt.length; j++) {
                if (paramArrayOfObject[j] == null) {
                    if (getItem(i, paramArrayOfInt[j]) == null)
                        continue;
                    k = 0;
                    break;
                }
                if (paramArrayOfObject[j].equals(getItem(i, paramArrayOfInt[j])))
                    continue;
                k = 0;
                break;
            }
            if (k != 0)
                return i;
        }
        return 0;
    }

    public final int count(int paramInt, Object paramObject) {
        int i = 0;
        int j = 0;
        while (true) {
            i = find(paramInt, paramObject, i + 1);
            if (i == 0)
                break;

            j++;
        }
        return j;
    }

    public final int[] filter(int paramInt, Object paramObject) {
        int[] arrayOfInt = new int[0];
        int i = 0;
        if ((i = count(paramInt, paramObject)) == 0)
            return arrayOfInt;
        arrayOfInt = new int[i];
        i = 0;
        for (int j = 0; (i = find(paramInt, paramObject, i + 1)) > 0; j++)
            arrayOfInt[j] = i;
        return arrayOfInt;
    }

    public final int count(int[] paramArrayOfInt, Object[] paramArrayOfObject) {
        int i = 0;
        int j = 0;
        while (true) {
            i = find(paramArrayOfInt, paramArrayOfObject, i + 1);
            if (i == 0)
                break;
            j++;
        }
        return j;
    }

    public final int[] filter(int[] paramArrayOfInt, Object[] paramArrayOfObject) {
        int[] arrayOfInt = new int[0];
        int i = 0;
        if ((paramArrayOfInt == null) || (paramArrayOfObject == null) || (paramArrayOfInt.length == 0) || (paramArrayOfInt.length != paramArrayOfObject.length))
            return arrayOfInt;
        arrayOfInt = filter(paramArrayOfInt[0], paramArrayOfObject[0]);
        if (arrayOfInt.length == 0)
            return arrayOfInt;
        for (i = 1; i < paramArrayOfInt.length; i++) {
            arrayOfInt = filter(paramArrayOfInt[i], paramArrayOfObject[i], arrayOfInt);
            if (arrayOfInt.length == 0)
                break;
        }
        return arrayOfInt;
    }

    public final int countgroup(int[] paramArrayOfInt, int paramInt) {
        int i = 1;
        for (int j = paramInt; (j < this.cache_data.size()) && (rowEquals(j, j + 1, paramArrayOfInt)); j++)
            i++;
        return i;
    }

    public boolean rowEquals(int paramInt1, int paramInt2, int[] paramArrayOfInt) {
        boolean bool = true;
        if (paramInt1 == paramInt2)
            return true;
        if ((paramArrayOfInt == null) || (paramArrayOfInt.length == 0))
            return true;
        if ((paramInt1 < 1) || (paramInt1 > this.cache_data.size()) || (paramInt2 < 1) || (paramInt2 > this.cache_data.size()))
            return false;
        Object[] arrayOfObject1 = (Object[]) this.cache_data.elementAt(paramInt1 - 1);
        Object[] arrayOfObject2 = (Object[]) this.cache_data.elementAt(paramInt2 - 1);
        for (int i = 0; i < paramArrayOfInt.length; i++) {
            if ((paramArrayOfInt[i] < 1) || (paramArrayOfInt[i] > this.column_count))
                continue;
            Object localObject1 = arrayOfObject1[(paramArrayOfInt[i] - 1)];
            Object localObject2 = arrayOfObject2[(paramArrayOfInt[i] - 1)];
            if (localObject1 == null) {
                if (localObject2 != null)
                    bool = false;
            } else
                bool = localObject1.equals(localObject2);
            if (!bool)
                break;
        }
        return bool;
    }

    public boolean rowEquals(int paramInt1, int paramInt2, int paramInt3) {
        boolean bool = true;
        if ((paramInt3 < 0) || (paramInt3 > this.column_count))
            return true;
        if (paramInt1 == paramInt2)
            return true;
        if ((paramInt1 < 1) || (paramInt1 > this.cache_data.size()) || (paramInt2 < 1) || (paramInt2 > this.cache_data.size()))
            return false;
        Object[] arrayOfObject1 = (Object[]) this.cache_data.elementAt(paramInt1 - 1);
        Object[] arrayOfObject2 = (Object[]) this.cache_data.elementAt(paramInt2 - 1);
        for (int i = 0; i < paramInt3; i++) {
            Object localObject1 = arrayOfObject1[i];
            Object localObject2 = arrayOfObject2[i];
            if (localObject1 == null) {
                if (localObject2 != null)
                    bool = false;
            } else
                bool = localObject1.equals(localObject2);
            if (!bool)
                break;
        }
        return bool;
    }

    public boolean rowEquals(int paramInt1, int paramInt2, int[] paramArrayOfInt, int paramInt3) {
        boolean bool = true;
        if (paramInt3 < 0)
            return true;
        if (paramInt1 == paramInt2)
            return true;
        if ((paramArrayOfInt == null) || (paramArrayOfInt.length == 0))
            return true;
        if ((paramInt1 < 1) || (paramInt1 > this.cache_data.size()) || (paramInt2 < 1) || (paramInt2 > this.cache_data.size()))
            return false;
        Object[] arrayOfObject1 = (Object[]) this.cache_data.elementAt(paramInt1 - 1);
        Object[] arrayOfObject2 = (Object[]) this.cache_data.elementAt(paramInt2 - 1);
        for (int i = 0; (i < paramArrayOfInt.length) && (i < paramInt3); i++) {
            if ((paramArrayOfInt[i] < 1) || (paramArrayOfInt[i] > this.column_count))
                continue;
            Object localObject1 = arrayOfObject1[(paramArrayOfInt[i] - 1)];
            Object localObject2 = arrayOfObject2[(paramArrayOfInt[i] - 1)];
            if (localObject1 == null) {
                if (localObject2 != null)
                    bool = false;
            } else
                bool = localObject1.equals(localObject2);
            if (!bool)
                break;
        }
        return bool;
    }

    public final Object[] distinct(int paramInt) {
        if ((paramInt < 1) && (paramInt > this.column_count))
            return new Object[0];
        TreeSet localTreeSet = new TreeSet();
        for (int i = 1; i <= getRowCount(); i++) {
            Object localObject = getItem(i, paramInt);
            if (localObject == null)
                continue;
            localTreeSet.add(localObject);
        }
        return localTreeSet.toArray();
    }

    public final Object[] distinct(String paramString) {
        return distinct(findColumn(paramString));
    }

    private final int find(int paramInt1, Object paramObject, int paramInt2, int[] paramArrayOfInt) {
        if ((paramInt1 < 1) || (paramInt1 > this.column_count))
            return 0;
        if ((paramArrayOfInt == null) || (paramArrayOfInt.length == 0))
            return 0;
        for (int i = paramInt2; i < paramArrayOfInt.length; i++)
            if (paramObject == null) {
                if (getItem(paramArrayOfInt[i], paramInt1) == null)
                    return i + 1;
            } else if (paramObject.equals(getItem(paramArrayOfInt[i], paramInt1)))
                return i + 1;
        return 0;
    }

    private final int count(int paramInt, Object paramObject, int[] paramArrayOfInt) {
        int i = 0;
        int j = 0;
        while (true) {
            i = find(paramInt, paramObject, i, paramArrayOfInt);
            if (i == 0)
                break;

            j++;
        }
        return j;
    }

    private final int[] filter(int paramInt, Object paramObject, int[] paramArrayOfInt) {
        int[] arrayOfInt = new int[0];
        int i = 0;
        if ((i = count(paramInt, paramObject, paramArrayOfInt)) == 0)
            return arrayOfInt;

        arrayOfInt = new int[i];
        i = 0;
        for (int j = 0; (i = find(paramInt, paramObject, i, paramArrayOfInt)) > 0; j++)
            arrayOfInt[j] = paramArrayOfInt[(i - 1)];
        return arrayOfInt;
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2) {
        if (paramArrayOfInt1.length == paramArrayOfInt2.length)
            for (int i = 0; i < paramArrayOfInt1.length; i++)
                addCrosstab(paramDBRowCache, paramInt, paramArrayOfInt1[i], paramArrayOfInt2[i]);
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2) {
        if (paramArrayOfString1.length == paramArrayOfString2.length)
            for (int i = 0; i < paramArrayOfString1.length; i++)
                addCrosstab(paramDBRowCache, paramString, paramArrayOfString1[i], paramArrayOfString2[i]);
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, String paramString1, String paramString2, String paramString3) {
        addCrosstab(paramDBRowCache, paramDBRowCache.findColumn(paramString1), paramDBRowCache.findColumn(paramString2), paramDBRowCache.findColumn(paramString3));
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, int paramInt1, int paramInt2, int paramInt3) {
        int j = 0;
        int k = 0;
        Object localObject3 = null;
        Object localObject4 = null;
        if ((paramInt1 < 1) || (paramInt1 > paramDBRowCache.getColumnCount()) || (paramInt2 < 1) || (paramInt2 > paramDBRowCache.getColumnCount()) || (paramInt3 < 1) || (paramInt3 > paramDBRowCache.getColumnCount()))
            return;
        int i = 0;
        if ((i = findColumn(paramDBRowCache.getColumnName(paramInt1))) == 0)
            addColumn(paramDBRowCache.getColumnName(paramInt1), paramDBRowCache.getColumnType(paramInt1));
        else
            setColumnType(i, paramDBRowCache.getColumnType(paramInt1));
        int m = paramDBRowCache.getColumnType(paramInt3);
        Object[] arrayOfObject = paramDBRowCache.distinct(paramInt2);
        for (i = 0; i < arrayOfObject.length; i++) {
            if (findColumn(arrayOfObject[i].toString()) != 0)
                continue;
            addColumn(arrayOfObject[i].toString(), m);
        }
        for (i = 1; i <= paramDBRowCache.getRowCount(); i++) {
            Object localObject1;
            Object localObject2;
            if (((localObject1 = paramDBRowCache.getItem(i, paramInt1)) == null) || ((localObject2 = paramDBRowCache.getItem(i, paramInt2)) == null))
                continue;
            if (!localObject2.equals(localObject4)) {
                if ((k = findColumn(localObject2.toString())) == 0) {
                    addColumn(localObject2.toString(), m);
                    k = this.column_count;
                }
                localObject4 = localObject2;
            }
            if (!localObject1.equals(localObject3)) {
                if ((j = find(1, localObject1)) == 0) {
                    j = appendRow();
                    setItem(j, 1, localObject1);
                }
                localObject3 = localObject1;
            }
            setItem(j, k, paramDBRowCache.getItem(i, paramInt3));
        }
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3) {
        if (paramArrayOfInt2.length == paramArrayOfInt3.length)
            for (int i = 0; i < paramArrayOfInt2.length; i++)
                addCrosstab(paramDBRowCache, paramArrayOfInt1, paramArrayOfInt2[i], paramArrayOfInt3[i]);
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3) {
        if (paramArrayOfString2.length == paramArrayOfString3.length)
            for (int i = 0; i < paramArrayOfString2.length; i++)
                addCrosstab(paramDBRowCache, paramArrayOfString1, paramArrayOfString2[i], paramArrayOfString3[i]);
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, String[] paramArrayOfString, String paramString1, String paramString2) {
        if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
            return;
        int[] arrayOfInt = new int[paramArrayOfString.length];
        for (int i = 0; i < paramArrayOfString.length; i++)
            arrayOfInt[i] = paramDBRowCache.findColumn(paramArrayOfString[i]);
        addCrosstab(paramDBRowCache, arrayOfInt, paramDBRowCache.findColumn(paramString1), paramDBRowCache.findColumn(paramString2));
    }

    public final void addCrosstab(DBRowCache paramDBRowCache, int[] paramArrayOfInt, int paramInt1, int paramInt2) {
        int j = 0;
        int k = 0;
        Object localObject2 = null;
        Object localObject3 = null;
        if ((paramArrayOfInt == null) || (paramArrayOfInt.length == 0))
            return;
        for (int n = 0; n < paramArrayOfInt.length; n++)
            if ((paramArrayOfInt[n] < 1) || (paramArrayOfInt[n] > paramDBRowCache.getColumnCount()))
                return;
        if ((paramInt1 < 1) || (paramInt1 > paramDBRowCache.getColumnCount()) || (paramInt2 < 1) || (paramInt2 > paramDBRowCache.getColumnCount()))
            return;
        int[] arrayOfInt = new int[paramArrayOfInt.length];
        for (int n = 0; n < paramArrayOfInt.length; n++)
            if ((arrayOfInt[n] = findColumn(paramDBRowCache.getColumnName(paramArrayOfInt[n]))) == 0) {
                addColumn(paramDBRowCache.getColumnName(paramArrayOfInt[n]), paramDBRowCache.getColumnType(paramArrayOfInt[n]));
                arrayOfInt[n] = getColumnCount();
            } else {
                setColumnType(arrayOfInt[n], paramDBRowCache.getColumnType(paramArrayOfInt[n]));
            }
        int m = paramDBRowCache.getColumnType(paramInt2);
        Object[] arrayOfObject2 = paramDBRowCache.distinct(paramInt1);
        for (int i = 0; i < arrayOfObject2.length; i++) {
            if (findColumn(arrayOfObject2[i].toString()) != 0)
                continue;
            addColumn(arrayOfObject2[i].toString(), m);
        }
        Object[] arrayOfObject1 = new Object[paramArrayOfInt.length];
        for (int i = 1; i <= paramDBRowCache.getRowCount(); i++) {
            Object localObject1;
            if ((localObject1 = paramDBRowCache.getItem(i, paramInt1)) == null)
                continue;
            if (!localObject1.equals(localObject3)) {
                if ((k = findColumn(localObject1.toString())) == 0) {
                    addColumn(localObject1.toString(), m);
                    k = this.column_count;
                }
                localObject3 = localObject1;
            }
            for (int i1 = 0; i1 < paramArrayOfInt.length; i1++)
                arrayOfObject1[i1] = paramDBRowCache.getItem(i, paramArrayOfInt[i1]);
            j = find(arrayOfInt, arrayOfObject1);
            if (j == 0) {
                j = appendRow();
                for (int i1 = 0; i1 < paramArrayOfInt.length; i1++)
                    setItem(j, arrayOfInt[i1], paramDBRowCache.getItem(i, paramArrayOfInt[i1]));
            }
            setItem(j, k, paramDBRowCache.getItem(i, paramInt2));
        }
    }

    public final DBRowCache getCrosstab(int paramInt1, int paramInt2, int paramInt3) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramInt1, paramInt2, paramInt3);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramInt, paramArrayOfInt1, paramArrayOfInt2);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(String paramString1, String paramString2, String paramString3) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramString1, paramString2, paramString3);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramString, paramArrayOfString1, paramArrayOfString2);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(int[] paramArrayOfInt, int paramInt1, int paramInt2) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramArrayOfInt, paramInt1, paramInt2);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramArrayOfInt1, paramArrayOfInt2, paramArrayOfInt3);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(String[] paramArrayOfString, String paramString1, String paramString2) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramArrayOfString, paramString1, paramString2);
        return localSimpleDBRowCache;
    }

    public final DBRowCache getCrosstab(String[] paramArrayOfString1, String[] paramArrayOfString2, String[] paramArrayOfString3) {
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        localSimpleDBRowCache.addCrosstab(this, paramArrayOfString1, paramArrayOfString2, paramArrayOfString3);
        return localSimpleDBRowCache;
    }

    public final void copyStruct(DBRowCache paramDBRowCache) {
        if (paramDBRowCache == null)
            return;
        for (int i = 1; i <= paramDBRowCache.getColumnCount(); i++)
            addColumn(paramDBRowCache.getColumnName(i), paramDBRowCache.getColumnType(i));
    }

    public void shiftRow(int paramInt1, int paramInt2) {
        if ((paramInt1 > 0) && (paramInt1 <= getRowCount()) && (paramInt2 > 0) && (paramInt2 <= getRowCount()) && (paramInt1 != paramInt2)) {
            Object[] arrayOfObject1 = getRow(paramInt1);
            Object[] arrayOfObject2 = getRow(paramInt2);
            setRow(paramInt1, arrayOfObject2);
            setRow(paramInt2, arrayOfObject1);
        }
    }

    public void quicksort(int paramInt) {
        quicksort(paramInt, true);
    }

    public void quicksort(String paramString) {
        quicksort(findColumn(paramString), true);
    }

    public void quicksort(String paramString, boolean paramBoolean) {
        quicksort(findColumn(paramString), paramBoolean);
    }

    public void quicksort(int paramInt, boolean paramBoolean) {
        if ((paramInt > 0) && (paramInt <= this.column_count)) {
            this.sort_column = paramInt;
            this.sort_asc = paramBoolean;
            Object[] arrayOfObject = this.cache_data.toArray();
            Arrays.sort(arrayOfObject, this);
            deleteAllRow();
            for (int i = 0; i < arrayOfObject.length; i++)
                appendRow((Object[]) arrayOfObject[i]);
        }
    }

    @Override
    public int compare(Object paramObject1, Object paramObject2) {
        int i = 0;
        Object localObject1 = ((Object[]) paramObject1)[(this.sort_column - 1)];
        Object localObject2 = ((Object[]) paramObject2)[(this.sort_column - 1)];
        if ((localObject1 != null) && (localObject2 != null)) {
            i = ((Comparable) localObject1).compareTo((Comparable) localObject2);
            return this.sort_asc ? i : 0 - i;
        }
        if (localObject1 != null)
            i = -1;
        if (localObject2 != null)
            i = 1;
        return i;
    }

    @Override
    public String toString() {
        return "SimpleDBRowCache{" +
                "column_count=" + column_count +
                ", column_label=" + Arrays.toString(column_label) +
                ", column_name=" + Arrays.toString(column_name) +
                ", column_type=" + Arrays.toString(column_type) +
                ", column_size=" + Arrays.toString(column_size) +
                ", cache_data=" + cache_data +
                ", sdftDate=" + sdftDate +
                ", sdftTime=" + sdftTime +
                ", sdftTimestamp=" + sdftTimestamp +
                ", sdftTimestampTz=" + sdftTimestampTz +
                ", sort_column=" + sort_column +
                ", sort_asc=" + sort_asc +
                '}';
    }
}
