package com.asql.core;

import com.asql.core.util.JavaVM;
import com.asql.core.util.TextUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

public final class DBConnection {
    public static final String[] DBTYPE = {"ORACLE", "ORAOCI", "SYBASE", "DB2APP", "DB2NET", "INFX", "MSSQL", "DDORA", "DDSYB", "DDDB2", "DDMSSQL", "DDINFX", "MYSQL", "PGSQL", "SAPDB", "NCR", "AS400", "ODBC", "JTDSMSSQL", "JTDSSYB"};
    public static final String[] DBDRIVER = {"oracle.jdbc.driver.OracleDriver", "oracle.jdbc.driver.OracleDriver", "com.sybase.jdbc2.jdbc.SybDriver", "COM.ibm.db2.jdbc.app.DB2Driver", "COM.ibm.db2.jdbc.net.DB2Driver", "com.informix.jdbc.IfxDriver", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "com.ddtek.jdbc.oracle.OracleDriver", "com.ddtek.jdbc.sybase.SybaseDriver", "com.ddtek.jdbc.db2.DB2Driver", "com.ddtek.jdbc.sqlserver.SQLServerDriver", "com.ddtek.jdbc.informix.InformixDriver", "com.mysql.jdbc.Driver", "org.postgresql.Driver", "com.sap.dbtech.jdbc.DriverSapDB", "com.ncr.teradata.TeraDriver", "com.ibm.as400.access.AS400JDBCDriver", "sun.jdbc.odbc.JdbcOdbcDriver", "net.sourceforge.jtds.jdbc.Driver", "net.sourceforge.jtds.jdbc.Driver"};
    public static final String[] DBURL = {"jdbc:oracle:thin:", "jdbc:oracle:oci8:", "jdbc:sybase:Tds:", "jdbc:db2:", "jdbc:db2://", "jdbc:informix-sqli://", "jdbc:sqlserver://", "jdbc:datadirect:oracle://", "jdbc:datadirect:sybase://", "jdbc:datadirect:db2://", "jdbc:datadirect:sqlserver://", "jdbc:datadirect:infomix://", "jdbc:mysql://", "jdbc:postgresql://", "jdbc:sapdb://", "jdbc:teradata://", "jdbc:as400://", "jdbc:odbc:", "jdbc:jtds:sqlserver://", "jdbc:jtds:sybase://"};

    public static final Properties getProperties(String db) {
        return getProperties(db, null, null);
    }

    public static final Properties getProperties(String db, String user, String password) {
        Properties localProperties = new Properties();
        int i = TextUtils.indexOf(DBTYPE, db);
        if ((db == null) || (i == -1))
            return localProperties;
        if (user != null)
            localProperties.setProperty("user", user);
        if (password != null)
            localProperties.setProperty("password", password);
        switch (i) {
            case 6:
                localProperties.setProperty("SelectMethod", "Cursor");
                break;
            case 7:
            case 9:
            case 11:
                localProperties.setProperty("BatchPerformanceWorkaround", "true");
                break;
            case 8:
            case 10:
                localProperties.setProperty("SelectMethod", "Cursor");
                localProperties.setProperty("BatchPerformanceWorkaround", "true");
                break;
            case 12:
                localProperties.setProperty("useUnicode", "true");
        }
        return localProperties;
    }

    public static final Connection getConnection(String db, String url)
            throws SQLException {
        return getConnection(db, url, null, null);
    }

    public static final Connection getConnection(String db, String url, String user, String password)
            throws SQLException {
        Properties properties = getProperties(db);
        int i = TextUtils.indexOf(DBTYPE, db);
        if (user != null)
            properties.setProperty("user", user);
        if (password != null)
            properties.setProperty("password", password);
        if ((url == null) || (db == null) || (i == -1))
            return null;
        return DriverManager.getConnection(DBURL[i] + url, properties);
    }

    public static final Connection getConnection(String db,
                                                 String url,
                                                 Properties properties)
            throws SQLException {
        return getConnection(db, url, null, null, properties);
    }

    public static final Connection getConnection(String db,
                                                 String url,
                                                 String user,
                                                 String password,
                                                 Properties properties)
            throws SQLException {
        int i = TextUtils.indexOf(DBTYPE, db);
        if (user != null)
            properties.setProperty("user", user);
        if (password != null)
            properties.setProperty("password", password);
        if ((url == null) || (db == null) || (i == -1))
            return null;
        return DriverManager.getConnection(DBURL[i] + url, properties);
    }

    public static final void setEncoding(String paramString) {
        Properties localProperties = System.getProperties();
        localProperties.setProperty("file.encoding", paramString);
        System.setProperties(localProperties);
        JavaVM.refresh();
    }

    public static final void setLocale(String paramString) {
        String str = paramString;
        if (str == null)
            return;
        str = str.toUpperCase();
        if (str.equals("ENGLISH"))
            Locale.setDefault(Locale.ENGLISH);
        else if (str.equals("FRENCH"))
            Locale.setDefault(Locale.FRENCH);
        else if (str.equals("GERMAN"))
            Locale.setDefault(Locale.GERMAN);
        else if (str.equals("ITALIAN"))
            Locale.setDefault(Locale.ITALIAN);
        else if (str.equals("JAPANESE"))
            Locale.setDefault(Locale.JAPANESE);
        else if (str.equals("KOREAN"))
            Locale.setDefault(Locale.KOREAN);
        else if (str.equals("CHINESE"))
            Locale.setDefault(Locale.CHINESE);
        else if (str.equals("SIMPLIFIED_CHINESE"))
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        else if (str.equals("TRADITIONAL_CHINESE"))
            Locale.setDefault(Locale.TRADITIONAL_CHINESE);
        else if (str.equals("FRANCE"))
            Locale.setDefault(Locale.FRANCE);
        else if (str.equals("GERMANY"))
            Locale.setDefault(Locale.GERMANY);
        else if (str.equals("ITALY"))
            Locale.setDefault(Locale.ITALY);
        else if (str.equals("JAPAN"))
            Locale.setDefault(Locale.JAPAN);
        else if (str.equals("KOREA"))
            Locale.setDefault(Locale.KOREA);
        else if (str.equals("CHINA"))
            Locale.setDefault(Locale.CHINA);
        else if (str.equals("PRC"))
            Locale.setDefault(Locale.PRC);
        else if (str.equals("TAIWAN"))
            Locale.setDefault(Locale.TAIWAN);
        else if (str.equals("UK"))
            Locale.setDefault(Locale.UK);
        else if (str.equals("US"))
            Locale.setDefault(Locale.US);
        else if (str.equals("CANADA"))
            Locale.setDefault(Locale.CANADA);
        else if (str.equals("CANADA_FRENCH"))
            Locale.setDefault(Locale.CANADA_FRENCH);
    }

    static {
        for (int i = 0; i < DBDRIVER.length; i++)
            try {
                Class.forName(DBDRIVER[i]);
            } catch (ClassNotFoundException localClassNotFoundException) {
            }
    }
}
