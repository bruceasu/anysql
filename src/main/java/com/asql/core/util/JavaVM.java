package com.asql.core.util;

import java.util.Properties;

public final class JavaVM {
  public static String VERSION = "1.3";
  public static int MAIN_VERSION = 1;
  public static int MINOR_VERSION = 3;
  public static String OS = "Windows XP";
  public static String OS_VERSION = "5.0";
  public static String USER_DIRECTORY = "";
  public static String FILE_SEPERATOR = "";
  public static String ENCODING = "GBK";
  public static String JAVA_HOME = "";

  public static void refresh() {
    Properties localProperties = System.getProperties();
    VERSION = localProperties.getProperty("java.version");
    OS = localProperties.getProperty("os.name");
    USER_DIRECTORY = localProperties.getProperty("user.dir");
    OS_VERSION = localProperties.getProperty("os.version");
    FILE_SEPERATOR = localProperties.getProperty("file.separator");
    ENCODING = localProperties.getProperty("file.encoding");
    MAIN_VERSION = Integer.valueOf(VERSION.substring(0, 1)).intValue();
    MINOR_VERSION = Integer.valueOf(VERSION.substring(2, 3)).intValue();
    JAVA_HOME = localProperties.getProperty("java.home");
  }

  static {
    refresh();
  }
}

