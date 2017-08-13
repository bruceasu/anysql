package com.asql.core;

import java.util.Properties;

public final class OptionCommand {
  private Properties option = new Properties();
  private String command = "";
  private boolean sensitive = false;

  public OptionCommand(String paramString) {
    parse(paramString);
  }

  public OptionCommand(String paramString, boolean paramBoolean) {
    this.sensitive = paramBoolean;
    parse(paramString);
  }

  public final String getCommand() {
    return this.command;
  }

  public final boolean getBoolean(String paramString, boolean paramBoolean) {
    String str = this.option.getProperty(paramString);
    if (str == null)
      return paramBoolean;
    return (str.equalsIgnoreCase("YES")) || (str.equalsIgnoreCase("TRUE")) || (str.equalsIgnoreCase("ON"));
  }

  public final int getInt(String paramString, int paramInt) {
    String str = this.option.getProperty(paramString);
    if (str == null)
      return paramInt;
    try {
      return Integer.valueOf(str).intValue();
    } catch (NumberFormatException localNumberFormatException) {
    }
    return paramInt;
  }

  public final long getLong(String paramString, long paramLong) {
    String str = this.option.getProperty(paramString);
    if (str == null)
      return paramLong;
    try {
      return Long.valueOf(str).longValue();
    } catch (NumberFormatException localNumberFormatException) {
    }
    return paramLong;
  }

  public final float getFloat(String paramString, float paramFloat) {
    String str = this.option.getProperty(paramString);
    if (str == null)
      return paramFloat;
    try {
      return Float.valueOf(str).floatValue();
    } catch (NumberFormatException localNumberFormatException) {
    }
    return paramFloat;
  }

  public final double getDouble(String paramString, double paramDouble) {
    String str = this.option.getProperty(paramString);
    if (str == null)
      return paramDouble;
    try {
      return Double.valueOf(str).doubleValue();
    } catch (NumberFormatException localNumberFormatException) {
    }
    return paramDouble;
  }

  public final String getString(String paramString1, String paramString2) {
    String str = this.option.getProperty(paramString1);
    if (str == null)
      return paramString2;
    return str;
  }

  public final String getOption(String paramString1, String paramString2) {
    String str = this.option.getProperty(paramString1);
    if (str == null)
      return paramString2;
    return str;
  }

  private void parse(String paramString) {
    if (paramString == null)
      return;
    if (paramString.length() == 0)
      return;
    char[] arrayOfChar = paramString.toCharArray();
    int i = 0;
    int j = 0;
    String str1 = null;
    String str2 = null;
    while (true) {
      i = j;
      while ((i < arrayOfChar.length) && ((arrayOfChar[i] == ' ') || (arrayOfChar[i] == '\t') || (arrayOfChar[i] == '\r') || (arrayOfChar[i] == '\n'))) {
        i++;
        j = i;
      }
      if ((i >= arrayOfChar.length) || (arrayOfChar[i] != '-'))
        break;
      while ((j < arrayOfChar.length) && (arrayOfChar[j] != ' ') && (arrayOfChar[j] != '\t') && (arrayOfChar[j] != '\r') && (arrayOfChar[j] != '\n'))
        j++;
      if (j == i + 1) {
        j = i;
        break;
      }
      str1 = String.valueOf(arrayOfChar, i, j - i);
      i = j;
      while ((i < arrayOfChar.length) && ((arrayOfChar[i] == ' ') || (arrayOfChar[i] == '\t') || (arrayOfChar[i] == '\r') || (arrayOfChar[i] == '\n'))) {
        i++;
        j = i;
      }
      while ((j < arrayOfChar.length) && (arrayOfChar[j] != ' ') && (arrayOfChar[j] != '\t') && (arrayOfChar[j] != '\r') && (arrayOfChar[j] != '\n'))
        j++;
      str2 = String.valueOf(arrayOfChar, i, j - i);
      if (!this.sensitive) {
        this.option.setProperty(str1.substring(1).toUpperCase(), str2);
        continue;
      }
      this.option.setProperty(str1.substring(1), str2);
    }
    if (j < arrayOfChar.length)
      this.command = String.valueOf(arrayOfChar, j, arrayOfChar.length - j);
  }
}
