package com.asql.core.log;

import com.asql.core.CommandExecutor;
import com.asql.core.DBRowCache;
import com.asql.core.SimpleDBRowCache;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OutputCommandLog implements CommandLog {
    private int pageSize = 14;
    private String seperator = " ";
    private String record = "\r\n";
    private boolean heading = true;
    private boolean autoTrace = false;
    private boolean termOut = true;
    private PrintStream out = null;
    private CommandLog logFile = null;
    private CommandExecutor sql = null;
    private boolean dispForm = false;

    public void setFormDisplay(boolean paramBoolean) {
        this.dispForm = paramBoolean;
    }

    public boolean getFormDisplay() {
        return this.dispForm;
    }

    public OutputCommandLog(CommandExecutor paramCommandExecutor, OutputStream paramOutputStream) {
        this.sql = paramCommandExecutor;
        this.out = new PrintStream(paramOutputStream);
    }

    public OutputCommandLog(CommandExecutor paramCommandExecutor, PrintStream paramPrintStream) {
        this.sql = paramCommandExecutor;
        this.out = paramPrintStream;
    }

    public void close() {
        this.out.close();
    }

    public void setTermOut(boolean paramBoolean) {
        this.termOut = paramBoolean;
    }

    private String getFixedWidth(String content, int length, boolean paddingEnd) {
        StringBuilder builder = new StringBuilder();
        if ((paddingEnd) && (content != null)) {
            builder.append(content);
        }
        for (int i = content == null ? 0 : content.getBytes().length; i < length; i++) {
            builder.append(" ");
        }
        if ((!paddingEnd) && (content != null)) {
            builder.append(content);
        }
        return builder.toString();
    }

    public void print(DBRowCache paramDBRowCache) {
        int i = 0;
        int j = 0;
        int k = 0;
        if (paramDBRowCache == null)
            return;
        if (paramDBRowCache.getColumnCount() == 0)
            return;
        if (this.seperator.equals(" "))
            paramDBRowCache.getWidth(false);
        i = paramDBRowCache.getRowCount();
        for (int m = 1; m <= paramDBRowCache.getColumnCount(); m++) {
            if (paramDBRowCache.getColumnName(m).length() <= k) continue;
            k = paramDBRowCache.getColumnName(m).length();
        }
        if (!this.dispForm)
            for (int m = 1; m <= i; m++) {
                display(paramDBRowCache, j, m);
                j++;
            }
        for (int m = 1; m <= i; m++) {
            for (int n = 1; n <= paramDBRowCache.getColumnCount(); n++) {
                String msg = getFixedWidth(paramDBRowCache.getColumnName(n), k + 1, true)
                        + ": " + paramDBRowCache.getString(m, n);
                println(msg);
            }
            if (m >= i) continue;

            println();
        }
        paramDBRowCache.deleteAllRow();
        if (i > 0) {
            println();
        }
        if (this.termOut)
            this.out.println(j + " rows returned.");
        if (this.logFile != null)
            this.logFile.println(j + " rows returned.");
    }

    private void display(DBRowCache paramDBRowCache, int j, int m) {
        if (this.heading) {
            if ((j == 0) || ((this.pageSize > 0)
                    && ((m % this.pageSize == 1) || (this.pageSize == 1)))) {
                if (j > 0) {
                    println();
                }
                if (this.seperator.equals(" ")) {
                    String fixedHeader = paramDBRowCache.getFixedHeader();
                    String seperator = paramDBRowCache.getSeperator();
                    println(fixedHeader);
                    println(seperator);
                } else {
                    String sepHeader = paramDBRowCache.getSepHeader(this.seperator);
                    println(sepHeader);
                }
            }
        } else if (((j == 0) ||
                    ((this.pageSize > 0) && ((m % this.pageSize == 1) || (this.pageSize == 1))))
                   && (j > 0)) {
            println();
        }
        if (this.seperator.equals(" ")) {
            String fixedRow = paramDBRowCache.getFixedRow(m);
            println(fixedRow);
        } else {
            String sepRow = paramDBRowCache.getSepRow(this.seperator, m);
            println(sepRow);
        }
    }

    public void print(ResultSet paramResultSet)
            throws SQLException {
        SimpleDBRowCache rowCache = new SimpleDBRowCache();
        int i = this.pageSize > 0 ? 400 / this.pageSize * this.pageSize : 400;
        int j = 0;
        int k = 0;
        int m = 0;
        String x = k + " rows returned.";
        if (this.autoTrace) {
            while (paramResultSet.next())
                k++;
            print(x);
            return;
        }
        while ((j = this.sql.fetch(paramResultSet, rowCache, i)) > 0) {
            if (m == 0)
                for (int n = 1; n <= rowCache.getColumnCount(); n++) {
                    if (rowCache.getColumnName(n).length() <= m)
                        continue;
                    m = rowCache.getColumnName(n).length();
                }
            if (this.seperator.equals(" "))
                rowCache.getWidth(false);
            if (!this.dispForm)
                for (int n = 1; n <= rowCache.getRowCount(); n++) {
                    display(rowCache, k, n);
                    k++;
                }
            for (int n = 1; n <= j; n++) {
                for (int i1 = 1; i1 <= rowCache.getColumnCount(); i1++) {
                    String msg = getFixedWidth(rowCache.getColumnName(i1), m + 1, true)
                            + ": " + rowCache.getString(n, i1);
                   println(msg);
                }
                if (n < j) {
                    if (this.termOut)
                        this.out.println();
                    if (this.logFile != null)
                        this.logFile.println();
                }
                k++;
            }
            rowCache.deleteAllRow();
            if (j <= 0)
                continue;
            if (this.termOut)
                this.out.println();
            if (this.logFile == null)
                continue;
            this.logFile.println();
        }
        print(x);
    }

    public void println() {
        if (this.termOut)
            this.out.println();
        if (this.logFile != null)
            this.logFile.println();
    }

    public void println(String msg) {
        if (this.termOut)
            this.out.println(msg);
        if (this.logFile != null)
            this.logFile.println(msg);
    }

    public void print(int rowNum) {
        String x = rowNum + " rows affected.";
        print(x);
    }

    public void print(String paramString) {
        if (this.termOut)
            this.out.print(paramString);
        if (this.logFile != null)
            this.logFile.print(paramString);
    }

    public void prompt(String paramString) {
        this.out.print(paramString);
        if (this.logFile != null)
            this.logFile.print(paramString);
    }

    private String removeNewLine(String paramString) {
        char[] arrayOfChar = paramString.toCharArray();
        int i = arrayOfChar.length - 1;
        for (; (i >= 0) && isWhitespace(arrayOfChar[i]); i--)
            ;
        if (i >= 0)
            return String.valueOf(arrayOfChar, 0, i + 1);
        return "";
    }

    private boolean isWhitespace(char c) {
        return (c == '\r') || (c == '\n') || (c == '\t') || (c == ' ');
    }

    public void print(Exception paramException) {
        String str = paramException.getMessage();
        println(removeNewLine(str));
    }

    public void print(SQLException paramSQLException) {
        String str = paramSQLException.getMessage();
        println(removeNewLine(str));
    }

    public int getPagesize() {
        return this.pageSize;
    }

    public void setPagesize(int paramInt) {
        if ((paramInt >= 0) && (paramInt <= 200))
            this.pageSize = paramInt;
    }

    public void setSeperator(String seperator) {
        this.seperator = seperator;
    }

    public void setRecord(String record) {
        if ((record == null) || (record.length() == 0)) {
            this.record = "\r\n";
            return;
        }
        char[] arrayOfChar = record.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arrayOfChar.length; i++)
            if (arrayOfChar[i] == '\\') {
                if (i + 1 < arrayOfChar.length) {
                    if (arrayOfChar[(i + 1)] == 'r') {
                        builder.append('\r');
                    } else if (arrayOfChar[(i + 1)] == 'n') {
                        builder.append('\n');
                    } else {
                        builder.append(arrayOfChar[i]);
                        builder.append(arrayOfChar[(i + 1)]);
                    }
                    i += 1;
                } else {
                    builder.append(arrayOfChar[i]);
                }
            } else
                builder.append(arrayOfChar[i]);
        this.record = builder.toString();
    }

    public String getSeperator() {
        return this.seperator;
    }

    public String getRecord() {
        return this.record;
    }

    public boolean getHeading() {
        return this.heading;
    }

    public void setAutoTrace(boolean paramBoolean) {
        this.autoTrace = paramBoolean;
    }

    public void setHeading(boolean paramBoolean) {
        this.heading = paramBoolean;
    }

    public void setLogFile(CommandLog paramCommandLog) {
        this.logFile = paramCommandLog;
    }

    public CommandLog getLogFile() {
        return this.logFile;
    }
}
