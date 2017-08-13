package com.asql.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

public interface DBRowCache {
    int getColumnCount();

    int getRowCount();

    void addColumn(String paramString, int paramInt);

    void removeAllColumn();

    int findColumn(String paramString);

    String getColumnName(int paramInt);

    String getColumnLabel(int paramInt);

    String getColumnLabel(String paramString);

    void setColumnLabel(int paramInt, String paramString);

    void setColumnLabel(String paramString1, String paramString2);

    void setColumnType(int paramInt1, int paramInt2);

    void setColumnType(String paramString, int paramInt);

    void setColumnSize(int paramInt1, int paramInt2);

    void setColumnSize(String paramString, int paramInt);

    int getColumnType(int paramInt);

    int getColumnType(String paramString);

    int getColumnSize(int paramInt);

    int getColumnSize(String paramString);

    void setItem(int paramInt1, int paramInt2, Object paramObject);

    void setItem(int paramInt, String paramString, Object paramObject);

    Object getItem(int paramInt1, int paramInt2);

    Object getItem(int paramInt, String paramString);

    String getString(int paramInt1, int paramInt2);

    String getString(int paramInt, String paramString);

    int getWidth(boolean paramBoolean);

    Object[] getRow(int paramInt);

    void setRow(int paramInt, Object[] paramArrayOfObject);

    int insertRow(int paramInt);

    int insertRow(int paramInt, Object[] paramArrayOfObject);

    int appendRow();

    int appendRow(Object[] paramArrayOfObject);

    void deleteRow(int paramInt);

    void deleteRow(int paramInt1, int paramInt2);

    void deleteAllRow();

    double min(String paramString, double paramDouble);

    double min(String[] paramArrayOfString, double paramDouble);

    double max(String paramString, double paramDouble);

    double max(String[] paramArrayOfString, double paramDouble);

    double avg(String paramString);

    double sum(String paramString);

    int count(String paramString);

    int count();

    int read(BufferedReader paramBufferedReader, int paramInt)
            throws IOException;

    int read(BufferedReader paramBufferedReader, String paramString, int paramInt)
            throws IOException;

    int read(BufferedReader paramBufferedReader,
             String paramString1,
             String paramString2,
             int paramInt)
            throws IOException;

    void write(PrintStream paramPrintStream);

    void write(PrintStream paramPrintStream, int paramInt);

    void write(PrintStream paramPrintStream, String paramString);

    void write(PrintStream paramPrintStream, String paramString1, String paramString2);

    void write(PrintStream paramPrintStream, String paramString, int paramInt);

    void write(PrintStream paramPrintStream,
               String paramString1,
               String paramString2,
               int paramInt);

    void write(Writer paramWriter)
            throws IOException;

    void write(Writer paramWriter, int paramInt)
            throws IOException;

    void write(Writer paramWriter, String paramString)
            throws IOException;

    void write(Writer paramWriter, String paramString1, String paramString2)
            throws IOException;

    void write(Writer paramWriter, String paramString, int paramInt)
            throws IOException;

    void write(Writer paramWriter, String paramString1, String paramString2, int paramInt)
            throws IOException;

    String getFixedHeader();

    String getFixedRow(int paramInt);

    String getSeperator();

    String getSepHeader(String paramString);

    String getSepRow(String paramString, int paramInt);

    void shiftRow(int paramInt1, int paramInt2);

    void copyStruct(DBRowCache paramDBRowCache);

    void quicksort(int paramInt, boolean paramBoolean);

    void quicksort(String paramString, boolean paramBoolean);

    void quicksort(int paramInt);

    void quicksort(String paramString);

    String getString(int paramInt);

    String getString(String paramString, int paramInt);

    int find(int paramInt, Object paramObject);

    int find(int paramInt1, Object paramObject, int paramInt2);

    int find(int[] paramArrayOfInt, Object[] paramArrayOfObject);

    int find(int[] paramArrayOfInt, Object[] paramArrayOfObject, int paramInt);

    int count(int paramInt, Object paramObject);

    int[] filter(int paramInt, Object paramObject);

    Object[] distinct(int paramInt);

    Object[] distinct(String paramString);

    void addCrosstab(DBRowCache paramDBRowCache, int paramInt1, int paramInt2, int paramInt3);

    void addCrosstab(DBRowCache paramDBRowCache,
                     String paramString1,
                     String paramString2,
                     String paramString3);

    void addCrosstab(DBRowCache paramDBRowCache,
                     int paramInt,
                     int[] paramArrayOfInt1,
                     int[] paramArrayOfInt2);

    void addCrosstab(DBRowCache paramDBRowCache,
                     String paramString,
                     String[] paramArrayOfString1,
                     String[] paramArrayOfString2);

    void addCrosstab(DBRowCache paramDBRowCache,
                     int[] paramArrayOfInt,
                     int paramInt1,
                     int paramInt2);

    void addCrosstab(DBRowCache paramDBRowCache,
                     String[] paramArrayOfString,
                     String paramString1,
                     String paramString2);

    void addCrosstab(DBRowCache paramDBRowCache,
                     int[] paramArrayOfInt1,
                     int[] paramArrayOfInt2,
                     int[] paramArrayOfInt3);

    void addCrosstab(DBRowCache paramDBRowCache,
                     String[] paramArrayOfString1,
                     String[] paramArrayOfString2,
                     String[] paramArrayOfString3);

    DBRowCache getCrosstab(int paramInt1, int paramInt2, int paramInt3);

    DBRowCache getCrosstab(String paramString1, String paramString2, String paramString3);

    DBRowCache getCrosstab(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2);

    DBRowCache getCrosstab(String paramString,
                           String[] paramArrayOfString1,
                           String[] paramArrayOfString2);

    void writeXMLBody(Writer paramWriter)
            throws IOException;

    void writeXMLBody(Writer paramWriter, int paramInt)
            throws IOException;

    void writeXMLBody(Writer paramWriter, String paramString, int paramInt)
            throws IOException;

    void writeXMLBody(Writer paramWriter, String paramString1, String paramString2, int paramInt)
            throws IOException;
}

