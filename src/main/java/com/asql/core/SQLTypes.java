package com.asql.core;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public final class SQLTypes {
  public static final int getTypeID(String paramString) {
    if (paramString == null)
      return 12;
    String str = paramString.trim().toUpperCase();
    if (str.equals("CHAR"))
      return 1;
    if (str.equals("VARCHAR"))
      return 12;
    if (str.equals("LONGVARCHAR"))
      return -1;
    if (str.equals("BINARY"))
      return -2;
    if (str.equals("VARBINARY"))
      return -3;
    if (str.equals("LONGVARBINARY"))
      return -4;
    if (str.equals("NUMERIC"))
      return 2;
    if (str.equals("DECIMAL"))
      return 3;
    if (str.equals("BIT"))
      return -7;
    if (str.equals("TINYINT"))
      return -6;
    if (str.equals("SMALLINT"))
      return 5;
    if (str.equals("INTEGER"))
      return 4;
    if (str.equals("BIGINT"))
      return -5;
    if (str.equals("REAL"))
      return 7;
    if (str.equals("FLOAT"))
      return 6;
    if (str.equals("DOUBLE"))
      return 8;
    if (str.equals("DATE"))
      return 91;
    if (str.equals("TIME"))
      return 92;
    if (str.equals("TIMESTAMP"))
      return 93;
    if (str.equals("BLOB"))
      return 2004;
    if (str.equals("CLOB"))
      return 2005;
    return 12;
  }

  public static final String getTypeName(int paramInt) {
    switch (paramInt) {
      case 1:
        return "CHAR";
      case 12:
        return "VARCHAR";
      case -1:
        return "LONGVARCHAR";
      case -2:
        return "BINARY";
      case -3:
        return "VARBINARY";
      case -4:
        return "LONGVARBINARY";
      case 2:
        return "NUMERIC";
      case 3:
        return "DECIMAL";
      case -7:
        return "BIT";
      case -6:
        return "TINYINT";
      case 5:
        return "SMALLINT";
      case 4:
        return "INTEGER";
      case -5:
        return "BIGINT";
      case 7:
        return "REAL";
      case 6:
        return "FLOAT";
      case 8:
        return "DOUBLE";
      case 91:
        return "DATE";
      case 92:
        return "TIME";
      case 93:
        return "TIMESTAMP";
      case 2004:
        return "BLOB";
      case 2005:
        return "CLOB";
    }
    return "VARCHAR";
  }

  public static final Class getTypeClass(int paramInt) {
    switch (paramInt) {
      case -1:
      case 1:
      case 12:
        return String.class;
      case 2:
      case 3:
        return BigDecimal.class;
      case -7:
        return Boolean.class;
      case -6:
        return Byte.class;
      case 4:
      case 5:
        return Integer.class;
      case -5:
        return Long.class;
      case 7:
        return Float.class;
      case 6:
      case 8:
        return Double.class;
      case 91:
        return Date.class;
      case 92:
        return Time.class;
      case 93:
        return Timestamp.class;
    }
    return Object.class;
  }

  public static final Object getValue(int paramInt, Object paramObject)
      throws NumberFormatException {
    if (paramObject == null)
      return null;
    switch (paramInt) {
      case -4:
      case -1:
      case 1:
      case 12:
      case 2004:
      case 2005:
        return paramObject.toString();
      case 2:
      case 3:
        return new BigDecimal(paramObject.toString());
      case -7:
        return Boolean.valueOf(paramObject.toString());
      case -6:
        return Byte.valueOf(paramObject.toString());
      case 4:
      case 5:
        return Integer.valueOf(paramObject.toString());
      case -5:
        return Long.valueOf(paramObject.toString());
      case 7:
        return Float.valueOf(paramObject.toString());
      case 6:
      case 8:
        return Double.valueOf(paramObject.toString());
      case 91:
        return Date.valueOf(paramObject.toString());
      case 92:
        return Time.valueOf(paramObject.toString());
    }
    return paramObject;
  }
}
