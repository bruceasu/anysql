package com.asql.core;

import static com.asql.core.CMDType.*;

import com.asql.core.io.CommandReader;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVM;
import com.asql.core.util.TextUtils;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public abstract class CommandExecutor extends SQLExecutor
{

    public static final int       SQL_QUERY_TIMEOUT = 3600;
    private             int       debugLevel        = 0;
    public volatile     boolean   exitShowLoop      = false;
    public              Statement currentStmt       = null;
    public              ResultSet resultSet         = null;
    private             Process   currentPid        = null;
    private             CtrlC     signalHandler     = new CtrlC();
    private             boolean   showComplete      = true;
    private             int       fetchSize         = 100;
    private             boolean   echoOn            = true;
    private             boolean   termOut           = true;

    public final int getDebugLevel()
    {
        return this.debugLevel;
    }

    public final void setDebugLevel(int debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    public final boolean getEcho()
    {
        return this.echoOn;
    }

    public final void setTermOut(boolean paramBoolean)
    {
        this.termOut = paramBoolean;
    }

    public final boolean getTermOut()
    {
        return this.termOut;
    }

    public final void setFetchSize(int size)
    {
        this.fetchSize = size;
        if (size < 1) {
            this.fetchSize = 1;
        }
        if (size > 1000) {
            this.fetchSize = 1000;
        }
    }

    public abstract boolean execute(Command paramCommand);

    public abstract CommandLog getCommandLog();

    public abstract void setCommandLog(CommandLog paramCommandLog);

    public abstract CommandReader getCommandReader();

    public abstract void setCommandReader(CommandReader paramCommandReader);

    public abstract CMDType getCommandType();

    public abstract void showVersion();

    public abstract boolean isConnected();

    public abstract void disconnect();

    public abstract String getLastCommand();

    public final String removeNewLine(String paramString)
    {
        char[] arrayOfChar = paramString.toCharArray();
        int i;
        for (i = arrayOfChar.length - 1;
                (i >= 0) && ((arrayOfChar[i] == '\r') || (arrayOfChar[i] == '\n') || (arrayOfChar[i]
                        == '\t') || (arrayOfChar[i] == ' ')); i--) {
            ;
        }
        if (i >= 0) {
            return String.valueOf(arrayOfChar, 0, i + 1);
        }
        return "";
    }

    public int fetch(ResultSet rs, DBRowCache rowCache) throws SQLException
    {
        return fetch(rs, rowCache, 100);
    }

    public int fetch(ResultSet rs, DBRowCache rowCache, int size)
    throws SQLException
    {
        int i = 0;
        int j = 0;
        int k = 0;
        byte[] byteBuffer1 = new byte[8192];
        char[] charBuffer1 = new char[4096];
        byte[] byteBuffer2 = new byte[65536];
        char[] charBuffer2 = new char[65536];
        ResultSetMetaData resultSetMetaData;
        if (rowCache.getColumnCount() == 0) {
            resultSetMetaData = rs.getMetaData();
            for (i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                if (resultSetMetaData.getColumnName(i) != null) {
                    if (rowCache.findColumn(resultSetMetaData.getColumnName(i)) == 0) {
                        rowCache.addColumn(resultSetMetaData.getColumnName(i),
                                resultSetMetaData.getColumnType(i));
                    } else {
                        for (j = 1; rowCache.findColumn(
                                resultSetMetaData.getColumnName(i) + "_" + j) != 0; j++) {
                            ;
                        }
                        rowCache.addColumn(resultSetMetaData.getColumnName(i) + "_" + j,
                                resultSetMetaData.getColumnType(i));
                    }
                } else {
                    for (j = 1; rowCache.findColumn("NULL" + j) != 0; j++) {
                        ;
                    }
                    rowCache.addColumn("NULL" + j, resultSetMetaData.getColumnType(i));
                }
            }
        }
        if (rowCache.getColumnCount() == 0) {
            return 0;
        }
        Object[] values;
        for (i = rowCache.getRowCount(); (i < size) && (rs.next());
                i = rowCache.appendRow(values)) {
            values = new Object[rowCache.getColumnCount()];
            for (j = 1; j <= rowCache.getColumnCount(); j++) {
                int m;
                Object value = null;
                Reader reader;
                InputStream stream;

                switch (rowCache.getColumnType(j)) {
                case -1:
                    reader = rs.getCharacterStream(j);
                    if (reader == null) {
                        break;
                    }
                    try {
                        m = reader.read(charBuffer2);
                        if (m > 0) {
                            value = String.valueOf(charBuffer2, 0, m);
                        }
                        reader.close();
                    } catch (IOException e) {
                    }
                case -4:
                    stream = rs.getBinaryStream(j);
                    if (stream == null) {
                        break;
                    }
                    try {
                        m = stream.read(byteBuffer2);
                        if (m > 0) {
                            value = new String(byteBuffer2, 0, m);
                        }
                        stream.close();
                    } catch (IOException localIOException2) {
                    }
                case 2005:
                    Clob clob = rs.getClob(j);
                    if (clob == null) {
                        break;
                    }
                    reader = clob.getCharacterStream();
                    if (reader == null) {
                        break;
                    }
                    try {
                        m = reader.read(charBuffer2);
                        if (m > 0) {
                            value = String.valueOf(charBuffer2, 0, m);
                        }
                        reader.close();
                    } catch (IOException localIOException3) {
                    }
                case 2004:
                    Blob blob = rs.getBlob(j);
                    if (blob == null) {
                        break;
                    }
                    stream = blob.getBinaryStream();
                    if (stream == null) {
                        break;
                    }
                    try {
                        m = stream.read(byteBuffer2);
                        if (m > 0) {
                            value = new String(byteBuffer2, 0, m);
                        }
                        stream.close();
                    } catch (IOException localIOException4) {
                    }
                case 1:
                case 12:
                    reader = rs.getCharacterStream(j);
                    if (reader == null) {
                        break;
                    }
                    try {
                        m = reader.read(charBuffer1);
                        if (rowCache.getColumnType(j) == 1) {
                            while ((m > 0) && (charBuffer1[(m - 1)] == ' ')) {
                                m--;
                            }
                        }
                        if (m > 0) {
                            value = String.valueOf(charBuffer1, 0, m);
                        }
                        reader.close();
                    } catch (IOException localIOException5) {
                    }
                case -3:
                case -2:
                    stream = rs.getAsciiStream(j);
                    if (stream == null) {
                        break;
                    }
                    try {
                        m = stream.read(byteBuffer1);
                        if (rowCache.getColumnType(j) == -2) {
                            while ((m > 0) && (byteBuffer1[(m - 1)] == 32)) {
                                m--;
                            }
                        }
                        if (m > 0) {
                            value = new String(byteBuffer1, 0, m);
                        }
                        stream.close();
                    } catch (IOException localIOException6) {
                    }
                case 91:
                    value = rs.getDate(j);
                    break;
                case 92:
                    value = rs.getTime(j);
                    break;
                case -102:
                case -101:
                case 93:
                    value = rs.getTimestamp(j);
                    break;
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
                case 16:
                    value = rs.getObject(j);
                    break;
                case -14:
                case -13:
                case -10:
                case 0:
                case 70:
                case 1111:
                case 2000:
                case 2001:
                case 2002:
                case 2003:
                case 2006:
                    value = "N/A";
                    break;
                default:
                    value = rs.getString(j);
                }
                values[(j - 1)] = value;
            }
        }
        return i;
    }

    public final void cancel()
    {
        this.exitShowLoop = true;
        if (this.currentPid != null) {
            this.currentPid.destroy();
            this.currentPid = null;
        }
        if (this.resultSet != null) {
            try {
                this.resultSet.close();
            } catch (SQLException localSQLException1) {
            }
            this.resultSet = null;
        }
        if (this.currentStmt != null) {
            try {
                this.currentStmt.cancel();
            } catch (SQLException e) {
                getCommandLog().print(e);
            }
            this.currentStmt = null;
        }
    }

    public final void setShowComplete(boolean paramBoolean)
    {
        this.showComplete = paramBoolean;
    }

    public final void setEcho(boolean paramBoolean)
    {
        this.echoOn = paramBoolean;
    }

    public static final void readBuffer(RandomAccessFile file, byte[] buffer, long skip)
    throws IOException
    {
        file.seek(skip);
        file.readFully(buffer, 0, buffer.length);
    }

    public static final void writeBuffer(RandomAccessFile file, byte[] buffer, long skip)
    throws IOException
    {
        file.seek(skip);
        file.write(buffer, 0, buffer.length);
    }

    public static final byte[] hex2byte(char[] buffer)
    {
        if ((buffer == null) || (buffer.length < 2)) {
            return new byte[0];
        }
        byte[] arrayOfByte = new byte[buffer.length / 2];
        for (int i = 0; i < buffer.length / 2; i++) {
            arrayOfByte[i] = Byte.parseByte(String.valueOf(buffer, i * 2, 2), 16);
        }
        return arrayOfByte;
    }

    public void editHex(String path, String skip, String hexData)
    {
        if ((skip == null) || (hexData == null)) {
            return;
        }
        try {
            long l = Long.valueOf(skip, 16).longValue();
            byte[] arrayOfByte = hex2byte(hexData.trim().toCharArray());
            File localFile = new File(path);
            if ((!localFile.exists()) || (!localFile.isFile()) || (!localFile.canWrite())) {
                getCommandLog().println("Cannot access file : " + path);
                return;
            }
            RandomAccessFile localRandomAccessFile = new RandomAccessFile(localFile, "rw");
            writeBuffer(localRandomAccessFile, arrayOfByte, l);
            localRandomAccessFile.close();
        } catch (IOException localIOException) {
            getCommandLog().print(localIOException);
        } catch (Exception localException) {
            getCommandLog().println("Invalid hexial address or value.");
        }
    }

    public final void viewHex(String path, int startPage, int pages, int pageSize)
    {
        File file = new File(path);
        if ((!file.exists()) || (!file.isFile()) || (!file.canRead())) {
            getCommandLog().println("Cannot access file : " + path);
            return;
        }
        if (startPage < 0) {
            startPage = 0;
        }
        if (pages < 1) {
            pages = 1;
        }
        if (pages > 8) {
            pages = 8;
        }
        if (pageSize < 1) {
            pageSize = 1;
        }
        if (pageSize > 32) {
            pageSize = 32;
        }
        StringBuffer buffer1 = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();
        int i = 0;
        byte[] arrayOfByte = new byte[pages * pageSize * 1024];
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(startPage * pageSize * 1024);
            i = randomAccessFile.read(arrayOfByte);
            if (i > 0) {
                getCommandLog().println();
                getCommandLog().println("BLOCK#=0");
                getCommandLog().println(
                        "          -0 -1 -2 -3 -4 -5 -6 -7  -8 -9 -A -B -C -D -E -F  0123456789ABCDEF");
                getCommandLog().println(
                        "--------- -----------------------  -----------------------  ----------------");
                for (int j = 0; j < i; j++) {
                    if ((j > 0) && (j % 16 == 0)) {
                        getCommandLog().print(buffer1.toString() + " ");
                        getCommandLog().println(buffer2.toString());
                        buffer1.delete(0, buffer1.length());
                        buffer2.delete(0, buffer2.length());
                        if (j % 256 == 0) {
                            getCommandLog().println();
                        }
                        if (j % 512 == 0) {
                            if (j % (pageSize * 1024) == 0) {
                                getCommandLog().println("BLOCK#=" + j / (pageSize * 1024));
                            }
                            getCommandLog().println(
                                    "          -0 -1 -2 -3 -4 -5 -6 -7  -8 -9 -A -B -C -D -E -F  0123456789ABCDEF");
                            getCommandLog().println(
                                    "--------- -----------------------  -----------------------  ----------------");
                        }
                    } else if ((j > 0) && (j % 8 == 0)) {
                        buffer1.append(" ");
                    }
                    if (j % 16 == 0) {
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 28 & 0xF));
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 24 & 0xF));
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 20 & 0xF));
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 16 & 0xF));
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 12 & 0xF));
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 8 & 0xF));
                        buffer1.append(Integer.toHexString(j + startPage * 1024 >> 4 & 0xF));
                        buffer1.append("0: ");
                    }
                    buffer1.append(Integer.toHexString(arrayOfByte[j] >> 4 & 0xF));
                    buffer1.append(Integer.toHexString(arrayOfByte[j] & 0xF));
                    if ((arrayOfByte[j] != 13) && (arrayOfByte[j] != 10) && (arrayOfByte[j] != 7)
                            && (arrayOfByte[j] != 9) && (arrayOfByte[j] != 0) && (arrayOfByte[j]
                            < 128) && (arrayOfByte[j] > 0) && (!Character.isISOControl(
                            (char) arrayOfByte[j]))) {
                        buffer2.append((char) arrayOfByte[j]);
                    } else {
                        buffer2.append('.');
                    }
                    buffer1.append(" ");
                }
                getCommandLog().print(buffer1.toString() + " ");
                getCommandLog().println(buffer2.toString());
                getCommandLog().println();
            } else {
                getCommandLog().println("Skip exceed file size.");
            }
            randomAccessFile.close();
            return;
        } catch (IOException localIOException1) {
            getCommandLog().print(localIOException1);
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException localIOException2) {
            }
        }
    }

    public void host(String paramString) throws IOException
    {
        String[] arrayOfString = null;
        String str1 = paramString;
        if (JavaVM.OS.startsWith("Windows")) {
            str1 = "CMD /C " + paramString;
        }
        File localFile = new File(JavaVM.USER_DIRECTORY);
        Process localProcess = Runtime.getRuntime().exec(str1, arrayOfString, localFile);
        this.currentPid = localProcess;
        InputStream localInputStream1 = localProcess.getInputStream();
        InputStream localInputStream2 = localProcess.getErrorStream();
        BufferedReader localBufferedReader1 = new BufferedReader(
                new InputStreamReader(localInputStream1));
        BufferedReader localBufferedReader2 = new BufferedReader(
                new InputStreamReader(localInputStream2));
        String str2 = null;
        while ((str2 = localBufferedReader1.readLine()) != null) {
            getCommandLog().println(str2);
        }
        while ((str2 = localBufferedReader2.readLine()) != null) {
            getCommandLog().println(str2);
        }
        try {
            localProcess.waitFor();
        } catch (InterruptedException localInterruptedException) {
            getCommandLog().println("process was interrupted");
        }
        localProcess.destroy();
        localBufferedReader1.close();
        localBufferedReader2.close();
        this.currentPid = null;
    }

    public static final long getLong(String paramString, long paramLong)
    {
        try {
            return Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException) {
        }
        return paramLong;
    }

    public static final int getInt(String paramString, int paramInt)
    {
        try {
            return Integer.valueOf(paramString).intValue();
        } catch (NumberFormatException localNumberFormatException) {
        }
        return paramInt;
    }

    public final String lpad(String paramString, int paramInt)
    {
        return getFixedWidth(paramString, paramInt, true);
    }

    public final String rpad(String paramString, int paramInt)
    {
        return getFixedWidth(paramString, paramInt, false);
    }

    private String getFixedWidth(String content, int length, boolean padEnd)
    {
        StringBuilder builder = new StringBuilder();
        if ((padEnd) && (content != null)) {
            builder.append(content);
        }
        for (int i = content == null ? 0 : content.getBytes().length; i < length; i++) {
            builder.append(" ");
        }
        if ((!padEnd) && (content != null)) {
            builder.append(content);
        }
        return builder.toString();
    }

    public final int commandAt(String[] commands, String[] command)
    {
        if (command == null) {
            return -1;
        }
        if (command.length == 0) {
            return -1;
        }
        if (commands.length == 0) {
            return -1;
        }
        int i = 0;
        int j = 0;
        int k = 0;
        for (i = 0; i < commands.length; i++) {
            j = 0;
            String[] arr = TextUtils.toStringArray(TextUtils.getWords(commands[i]));
            if (arr.length > command.length) {
                continue;
            }
            k = 1;
            for (j = 0; j < arr.length; j++) {
                if (arr[j].equalsIgnoreCase(command[j])) {
                    continue;
                }
                k = 0;
                break;
            }
            if (k != 0) {
                return i;
            }
        }
        return -1;
    }

    public final void debug(String paramString,
                            SQLQuery paramSQLQuery,
                            VariableTable paramVariableTable)
    {
        if (this.debugLevel > 0) {
            CommandLog localCommandLog = getCommandLog();
            localCommandLog.println(
                    "==================== DEBUG L=" + this.debugLevel + " ====================");
            localCommandLog.println("Time: " + new java.util.Date());
            localCommandLog.println(paramString);
            if ((this.debugLevel > 1) && (paramSQLQuery != null) && (
                    paramSQLQuery.getParamNames().length > 0)) {
                localCommandLog.println();
                localCommandLog.println("BIND VARIABLE:");
                for (int i = 0; i < paramSQLQuery.getParamNames().length; i++) {
                    localCommandLog.print(paramSQLQuery.getParamNames()[i]);
                    localCommandLog.print(" = ");
                    localCommandLog.println(paramSQLQuery.getParamTypes()[i]);
                }
            }
            if ((this.debugLevel > 1) && (paramVariableTable != null)) {
                String[] arrayOfString = paramVariableTable.getNames();
                if (arrayOfString.length > 0) {
                    localCommandLog.println();
                    localCommandLog.println("HOST VARIABLE:");
                    for (int j = 0; j < arrayOfString.length; j++) {
                        localCommandLog.print(arrayOfString[j]);
                        localCommandLog.print("=");
                        Object localObject = paramVariableTable.getValue(arrayOfString[j]);
                        if (localObject != null) {
                            localCommandLog.println(localObject.toString());
                        } else {
                            localCommandLog.println("(null)");
                        }
                    }
                }
            }
            localCommandLog.println("==================== DEBUG END ===================");
            localCommandLog.println();
        }
    }

    public abstract void doServerMessage() throws SQLException;

    public abstract DBRowCache executeQuery(String paramString, VariableTable paramVariableTable)
    throws SQLException;

    public abstract DBRowCache executeQuery(String paramString,
                                            VariableTable paramVariableTable,
                                            int paramInt) throws SQLException;

    public final DBRowCache executeQuery(Connection paramConnection,
                                         String paramString,
                                         VariableTable paramVariableTable) throws SQLException
    {
        return executeQuery(paramConnection, paramString, paramVariableTable, 10000);
    }

    public final DBRowCache executeQuery(SQLStatement paramSQLStatement,
                                         VariableTable paramVariableTable) throws SQLException
    {
        return executeQuery(paramSQLStatement, paramVariableTable, 10000);
    }


    public final DBRowCache executeQuery(SQLStatement paramSQLStatement,
                                         VariableTable paramVariableTable,
                                         int paramInt) throws SQLException
    {
        ResultSet localResultSet = null;
        SQLException sqlException = null;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            debug(paramSQLStatement.getDestSQL(), null, paramVariableTable);
            paramSQLStatement.stmt.setMaxRows(paramInt);
            paramSQLStatement.bind(paramVariableTable);
            this.currentStmt = paramSQLStatement.stmt;
            localResultSet = paramSQLStatement.stmt.executeQuery();
            this.resultSet = localResultSet;
            fetch(localResultSet, localSimpleDBRowCache, paramInt);
            localResultSet.close();
            this.resultSet = null;
            this.currentStmt = null;
        } catch (SQLException localSQLException1) {
            sqlException = localSQLException1;
        }
        try {
            if (localResultSet != null) {
                localResultSet.close();
            }
        } catch (SQLException localSQLException2) {
        }
        if (sqlException != null) {
            throw sqlException;
        }
        return localSimpleDBRowCache;
    }

    public final DBRowCache executeQuery(Connection paramConnection,
                                         String paramString,
                                         VariableTable paramVariableTable,
                                         int paramInt) throws SQLException
    {
        SQLStatement localSQLStatement = null;
        ResultSet localResultSet = null;
        SQLException sqlException = null;
        SimpleDBRowCache localSimpleDBRowCache = new SimpleDBRowCache();
        try {
            localSQLStatement = prepareStatement(paramConnection, paramString, paramVariableTable);
            localSQLStatement.stmt.setMaxRows(paramInt);
            localSQLStatement.bind(paramVariableTable);
            this.currentStmt = localSQLStatement.stmt;
            localResultSet = localSQLStatement.stmt.executeQuery();
            this.resultSet = localResultSet;
            fetch(localResultSet, localSimpleDBRowCache, paramInt);
            localResultSet.close();
            this.resultSet = null;
            this.currentStmt = null;
        } catch (SQLException localSQLException1) {
            sqlException = localSQLException1;
        }
        try {
            if (localResultSet != null) {
                localResultSet.close();
            }
        } catch (SQLException localSQLException2) {
        }
        try {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null)) {
                localSQLStatement.stmt.close();
            }
        } catch (SQLException localSQLException3) {
        }
        if (sqlException != null) {
            throw sqlException;
        }
        return localSimpleDBRowCache;
    }

    public final SQLStatement prepareScript(Connection paramConnection,
                                            String paramString,
                                            VariableTable paramVariableTable) throws SQLException
    {
        String[] arrayOfString = new String[0];
        String str = paramVariableTable.parseString(paramString, '&', '\\');
        debug(str, null, paramVariableTable);
        PreparedStatement preparedStatement = paramConnection.prepareStatement(str, 1003, 1007);
        preparedStatement.setQueryTimeout(SQL_QUERY_TIMEOUT);
        preparedStatement.setFetchSize(this.fetchSize);
        return new SQLStatement(preparedStatement,
                new SQLQuery(paramString, str, arrayOfString, arrayOfString));
    }

    public final SQLStatement prepareStatement(Connection paramConnection,
                                               String paramString,
                                               VariableTable paramVariableTable) throws SQLException
    {
        return prepareStatement(paramConnection, paramString, paramVariableTable, 1003, 1007);
    }

    public final SQLStatement prepareStatement(Connection paramConnection,
                                               String query,
                                               VariableTable paramVariableTable,
                                               int paramInt1,
                                               int paramInt2) throws SQLException
    {
        SQLQuery localSQLQuery = SQLConvert.parseSQL(query == null ? "" : query,
                paramVariableTable);
        debug(localSQLQuery.getDestSQL(), localSQLQuery, paramVariableTable);
        PreparedStatement preparedStatement = paramConnection.prepareStatement(
                localSQLQuery.getDestSQL(), paramInt1, paramInt2);
        preparedStatement.setQueryTimeout(SQL_QUERY_TIMEOUT);
        preparedStatement.setFetchSize(this.fetchSize);
        return new SQLStatement(preparedStatement, localSQLQuery);
    }

    public final SQLCallable prepareCall(Connection paramConnection,
                                         String paramString,
                                         VariableTable paramVariableTable) throws SQLException
    {
        return prepareCall(paramConnection, paramString, paramVariableTable, 1003, 1007);
    }

    public final SQLCallable prepareCall(Connection paramConnection,
                                         String paramString,
                                         VariableTable paramVariableTable,
                                         int paramInt1,
                                         int paramInt2) throws SQLException
    {
        CallableStatement localCallableStatement = null;
        SQLQuery localSQLQuery = SQLConvert.parseCall(paramString == null ? "" : paramString,
                paramVariableTable);
        debug(localSQLQuery.getDestSQL(), localSQLQuery, paramVariableTable);
        localCallableStatement = paramConnection.prepareCall(
                "{ " + localSQLQuery.getDestSQL() + " }", paramInt1, paramInt2);
        localCallableStatement.setQueryTimeout(SQL_QUERY_TIMEOUT);
        localCallableStatement.setFetchSize(this.fetchSize);
        return new SQLCallable(localCallableStatement, localSQLQuery);
    }

    public void procDisabledCommand(Command paramCommand)
    {
    }

    public void procUnknownCommand(Command paramCommand)
    {
        getCommandLog().println("Unknown command!");
    }

    public final void run(Command paramCommand) throws IOException
    {
        CommandLog log = getCommandLog();
        CMDType localCMDType = getCommandType();
        CommandReader localCommandReader = getCommandReader();
        int i = -1;
        if (paramCommand.TYPE1 == ASQL_EXIT) {
            return;
        }
        if ((paramCommand.TYPE1 != ASQL_COMMENT) && (paramCommand.TYPE1 != NULL_COMMAND) && (
                paramCommand.TYPE1 != MULTI_COMMENT_START)) {
            if (paramCommand.TYPE1 == UNKNOWN_COMMAND) {
                procUnknownCommand(paramCommand);
            } else if (paramCommand.TYPE1 == DISABLED_COMMAND) {
                procDisabledCommand(paramCommand);
            } else if ((paramCommand.TYPE1 == ASQL_SINGLE) || (paramCommand.TYPE1
                    == ASQL_DB_COMMAND) || (paramCommand.TYPE1 == ASQL_MULTIPLE)) {
                if ((paramCommand.TYPE2 != ASQL_CANCEL) && (!execute(paramCommand))) {
                    return;
                }
            } else if (paramCommand.TYPE1 == ASQL_SQL_FILE) {
                if (!execute(paramCommand)) {
                    return;
                }
            } else if (isConnected()) {
                if ((paramCommand.TYPE2 != ASQL_CANCEL) && (!execute(paramCommand))) {
                    return;
                }
            } else {
                log.println("Database not connected!");
            }
        }
    }

    public final Command run(CommandReader paramCommandReader) throws IOException
    {
        CommandLog log = getCommandLog();
        CMDType cmdType = getCommandType();
        int i = -1;
        Command localCommand = new Command(ASQL_EXIT, ASQL_EXIT, null);
        while (true) {
            if ((this.echoOn) && (this.termOut)) {
                localCommand = cmdType.readCommand(paramCommandReader, log, true);
            } else if (log.getLogFile() != null) {
                localCommand = cmdType.readCommand(paramCommandReader, log.getLogFile(), true);
            } else {
                localCommand = cmdType.readCommand(paramCommandReader);
            }
            if (localCommand.TYPE1 == ASQL_EXIT) {
                return localCommand;
            }
            if ((localCommand.TYPE1 == ASQL_COMMENT) || (localCommand.TYPE1 == NULL_COMMAND) || (
                    localCommand.TYPE1 == MULTI_COMMENT_START)) {
                continue;
            }
            if (localCommand.TYPE1 == UNKNOWN_COMMAND) {
                procUnknownCommand(localCommand);
                continue;
            }
            if (localCommand.TYPE1 == DISABLED_COMMAND) {
                procDisabledCommand(localCommand);
                continue;
            }
            if ((localCommand.TYPE1 == ASQL_SINGLE) || (localCommand.TYPE1 == ASQL_DB_COMMAND) || (
                    localCommand.TYPE1 == ASQL_MULTIPLE)) {
                if ((localCommand.TYPE2 == ASQL_CANCEL) || (execute(localCommand))) {
                    continue;
                }
                break;
            }
            if (localCommand.TYPE1 == ASQL_SQL_FILE) {
                if (execute(localCommand)) {
                    continue;
                }
                break;
            }
            if (isConnected()) {
                if ((localCommand.TYPE2 == ASQL_CANCEL) || (execute(localCommand))) {
                    continue;
                }
                break;
            }
            log.println("Database not connected!");
        }
        return localCommand;
    }

    public final void run() throws IOException
    {
        CommandLog log = getCommandLog();
        CMDType localCMDType = getCommandType();
        CommandReader commandReader = getCommandReader();
        Command command = null;
        showVersion();
        int i = -1;
        try {
            Signal.handle(new Signal("INT"), this.signalHandler);
        } catch (Exception localException) {
        }
        while (true) {
            System.runFinalization();
            command = localCMDType.readCommand(commandReader, log, false);
            System.gc();
            if (command.TYPE1 == ASQL_EXIT) {
                break;
            }
            if ((command.TYPE1 == ASQL_COMMENT) || (command.TYPE1 == NULL_COMMAND) || (command.TYPE1
                    == MULTI_COMMENT_START)) {
                continue;
            }
            if (command.TYPE1 == UNKNOWN_COMMAND) {
                procUnknownCommand(command);
                continue;
            }
            if (command.TYPE1 == DISABLED_COMMAND) {
                procDisabledCommand(command);
                continue;
            }
            if ((command.TYPE1 == ASQL_SINGLE) || (command.TYPE1 == ASQL_DB_COMMAND) || (
                    command.TYPE1 == ASQL_MULTIPLE)) {
                if ((command.TYPE2 == ASQL_CANCEL) || (execute(command))) {
                    continue;
                }
                break;
            }
            if (command.TYPE1 == ASQL_SQL_FILE) {
                if (execute(command)) {
                    continue;
                }
                break;
            }
            if (isConnected()) {
                if ((command.TYPE2 == ASQL_CANCEL) || (execute(command))) {
                    continue;
                }
                break;
            }
            log.println("Database not connected!");
        }
        disconnect();
    }

    public final void clearWarnings(Statement statement, CommandLog log)
    {
        try {
            SQLWarning localSQLWarning = statement.getWarnings();
            if (localSQLWarning != null) {
                log.print(localSQLWarning);
            }
            statement.clearWarnings();
        } catch (SQLException e) {
            log.print(e);
        }
    }

    public final void executeScript(Connection connection, Command command, CommandLog log)
    {
        VariableTable table = new VariableTable();
        executeScript(connection, command, table, log);
    }

    public final void executeScript(Connection connection,
                                    Command command,
                                    VariableTable table,
                                    CommandLog log)
    {
        int i = 0;
        int j = -1;
        boolean bool = false;
        ResultSet localResultSet = null;
        SQLStatement localSQLStatement = null;
        String str = command.COMMAND;
        if (str == null) {
            return;
        }
        if (str.equalsIgnoreCase("COMMIT")) {
            doCommit(connection, log);
            return;
        }
        if (str.equalsIgnoreCase("ROLLBACK")) {
            doRollback(connection, log);
            return;
        }
        try {
            localSQLStatement = prepareScript(connection, str, table);
            if (localSQLStatement.stmt == null) {
                return;
            }
            localSQLStatement.bind(table);
            this.currentStmt = localSQLStatement.stmt;
            bool = localSQLStatement.stmt.execute();
            do {
                if (bool) {
                    localResultSet = localSQLStatement.stmt.getResultSet();
                    this.resultSet = localResultSet;
                    log.print(localResultSet);
                    this.resultSet = null;
                    localResultSet.close();
                } else {
                    doServerMessage();
                    try {
                        j = localSQLStatement.stmt.getUpdateCount();
                    } catch (SQLException localSQLException1) {
                        j = -1;
                    }
                    if (j >= 0) {
                        log.print(j);
                    } else if (this.showComplete) {
                        int length = Math.min(100, command.COMMAND.length());
                        Vector words = TextUtils.getWords(command.COMMAND.substring(0, length));
                        String[] arrayOfString1 = TextUtils.toStringArray(words);
                        String[] arrayOfString2 = getCommandType().getCommandHint();
                        int k = commandAt(arrayOfString2, arrayOfString1);
                        if (k != -1) {
                            log.println(arrayOfString2[k] + " Succeed.");
                        } else {
                            log.println("Command completed.");
                        }
                    }
                }
                bool = localSQLStatement.stmt.getMoreResults();
            } while ((bool) || (j != -1));
        } catch (SQLException localSQLException2) {
            log.print(localSQLException2);
        } finally {
            if ((localSQLStatement != null) && (localSQLStatement.stmt != null)) {
                clearWarnings(localSQLStatement.stmt, log);
                try {
                    localSQLStatement.stmt.close();
                } catch (SQLException localSQLException3) {
                }
            }
        }
        this.currentStmt = null;
        this.resultSet = null;
        clearWarnings(connection, log);
    }

    public long writeData(BufferedWriter paramBufferedWriter,
                          ResultSet paramResultSet,
                          String paramString1,
                          String paramString2,
                          boolean paramBoolean) throws SQLException, IOException
    {
        long l1 = 0L;
        String str1 = null;
        int i = 0;
        int j = 32768;
        byte[] arrayOfByte1 = new byte[8192];
        char[] arrayOfChar1 = new char[4096];
        byte[] arrayOfByte2 = new byte[65536];
        char[] arrayOfChar2 = new char[65536];
        long l2 = System.currentTimeMillis();
        ResultSetMetaData localResultSetMetaData = paramResultSet.getMetaData();
        int k = localResultSetMetaData.getColumnCount();
        int[] arrayOfInt = new int[k];
        SimpleDateFormat localSimpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat localSimpleDateFormat2 = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat localSimpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        for (int m = 0; m < k; m++) {
            arrayOfInt[m] = localResultSetMetaData.getColumnType(m + 1);
            if (!paramBoolean) {
                continue;
            }
            str1 = localResultSetMetaData.getColumnName(m + 1);
            if (str1 != null) {
                paramBufferedWriter.write(str1);
            }
            if (m >= k - 1) {
                continue;
            }
            paramBufferedWriter.write(paramString1);
        }
        if (paramBoolean) {
            paramBufferedWriter.write(paramString2);
        }
        while (paramResultSet.next()) {
            l1 += 1L;
            for (int m = 1; m <= k; m++) {
                Object localObject1;
                Object localObject2;
                switch (arrayOfInt[(m - 1)]) {
                case -1:
                    Reader localReader = paramResultSet.getCharacterStream(m);
                    if (localReader == null) {
                        break;
                    }
                    try {
                        i = localReader.read(arrayOfChar2);
                        if (i > 0) {
                            paramBufferedWriter.write(arrayOfChar2, 0, i);
                        }
                        localReader.close();
                    } catch (IOException localIOException1) {
                    }
                case -4:
                    InputStream localInputStream1 = paramResultSet.getBinaryStream(m);
                    if (localInputStream1 == null) {
                        break;
                    }
                    try {
                        i = localInputStream1.read(arrayOfByte2);
                        if (i > 0) {
                            paramBufferedWriter.write(new String(arrayOfByte2, 0, i));
                        }
                        localInputStream1.close();
                    } catch (IOException localIOException2) {
                    }
                case 2005:
                    Clob localClob = paramResultSet.getClob(m);
                    if (localClob == null) {
                        break;
                    }
                    localObject1 = localClob.getCharacterStream();
                    if (localObject1 == null) {
                        break;
                    }
                    try {
                        i = ((Reader) localObject1).read(arrayOfChar2);
                        if (i > 0) {
                            paramBufferedWriter.write(arrayOfChar2, 0, i);
                        }
                        ((Reader) localObject1).close();
                    } catch (IOException localIOException3) {
                    }
                case 2004:
                    localObject1 = paramResultSet.getBlob(m);
                    if (localObject1 == null) {
                        break;
                    }
                    localObject2 = ((Blob) localObject1).getBinaryStream();
                    if (localObject2 == null) {
                        break;
                    }
                    try {
                        i = ((InputStream) localObject2).read(arrayOfByte2);
                        if (i > 0) {
                            paramBufferedWriter.write(new String(arrayOfByte2, 0, i));
                        }
                        ((InputStream) localObject2).close();
                    } catch (IOException localIOException4) {
                    }
                case 1:
                case 12:
                    localObject2 = paramResultSet.getCharacterStream(m);
                    if (localObject2 == null) {
                        break;
                    }
                    try {
                        i = ((Reader) localObject2).read(arrayOfChar1);
                        if (arrayOfInt[(m - 1)] == 1) {
                            while ((i > 0) && (arrayOfChar1[(i - 1)] == ' ')) {
                                i--;
                            }
                        }
                        if (i > 0) {
                            paramBufferedWriter.write(arrayOfChar1, 0, i);
                        }
                        ((Reader) localObject2).close();
                    } catch (IOException localIOException5) {
                    }
                case -3:
                case -2:
                    InputStream localInputStream2 = paramResultSet.getAsciiStream(m);
                    if (localInputStream2 == null) {
                        break;
                    }
                    try {
                        i = localInputStream2.read(arrayOfByte1);
                        if (arrayOfInt[(m - 1)] == -2) {
                            while ((i > 0) && (arrayOfByte1[(i - 1)] == 32)) {
                                i--;
                            }
                        }
                        if (i > 0) {
                            paramBufferedWriter.write(new String(arrayOfByte1, 0, i));
                        }
                        localInputStream2.close();
                    } catch (IOException localIOException6) {
                    }
                case 91:
                    java.sql.Date localDate = paramResultSet.getDate(m);
                    if (localDate == null) {
                        break;
                    }
                    paramBufferedWriter.write(localSimpleDateFormat1.format(localDate));
                    break;
                case 92:
                    Time localTime = paramResultSet.getTime(m);
                    if (localTime == null) {
                        break;
                    }
                    paramBufferedWriter.write(localSimpleDateFormat2.format(localTime));
                    break;
                case 93:
                    Timestamp localTimestamp = paramResultSet.getTimestamp(m);
                    if (localTimestamp == null) {
                        break;
                    }
                    paramBufferedWriter.write(localSimpleDateFormat3.format(localTimestamp));
                    break;
                case 0:
                case 70:
                case 1111:
                case 2000:
                case 2001:
                case 2002:
                case 2003:
                case 2006:
                    break;
                default:
                    String str2 = paramResultSet.getString(m);
                    if (str2 == null) {
                        break;
                    }
                    paramBufferedWriter.write(str2);
                }
                if (m < k) {
                    paramBufferedWriter.write(paramString1);
                } else {
                    paramBufferedWriter.write(paramString2);
                }
            }
            if (l1 % 100000L != 0L) {
                continue;
            }
            getCommandLog().println(
                    lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(
                            System.currentTimeMillis() - l2));
        }
        if (l1 % 100000L != 0L) {
            getCommandLog().println(
                    lpad(String.valueOf(l1), 12) + " rows writed in " + DBOperation.getElapsed(
                            System.currentTimeMillis() - l2));
        }
        return l1;
    }

    public final void executeSQL(Connection connection, Command command, CommandLog log)
    {
        VariableTable localVariableTable = new VariableTable();
        executeSQL(connection, command, localVariableTable, log);
    }

    public final void executeSQL(Connection connection,
                                 Command command,
                                 VariableTable table,
                                 CommandLog log)
    {
        int i = 0;
        int j = -1;
        boolean bool = false;
        ResultSet rs = null;
        SQLStatement statement = null;
        String query = command.COMMAND;
        if (query == null) {
            return;
        }
        if (query.equalsIgnoreCase("COMMIT")) {
            doCommit(connection, log);
            return;
        }
        if (query.equalsIgnoreCase("ROLLBACK")) {
            doRollback(connection, log);
            return;
        }
        try {
            if ((query.endsWith("/G")) || (query.endsWith("/g")) || (query.endsWith("\\G"))
                    || (query.endsWith("\\g"))) {
                log.setFormDisplay(true);
                statement = prepareStatement(connection, query.substring(0, query.length() - 2),
                        table);
            } else {
                statement = prepareStatement(connection, query, table);
            }
            if (statement.stmt == null) {
                return;
            }
            statement.bind(table);
            this.currentStmt = statement.stmt;
            bool = statement.stmt.execute();
            do {
                if (bool) {
                    rs = statement.stmt.getResultSet();
                    this.resultSet = rs;
                    log.print(rs);
                    this.resultSet = null;
                    rs.close();
                } else {
                    doServerMessage();
                    try {
                        j = statement.stmt.getUpdateCount();
                    } catch (SQLException localSQLException1) {
                        j = -1;
                    }
                    if (j >= 0) {
                        if ((command.TYPE1 == 0) || (command.TYPE1 == 1)) {
                            log.print(j);
                        }
                    } else if ((command.TYPE1 != 0) && (command.TYPE1 != 1)) {
                        if (command.TYPE1 == 13) {
                            log.println("Procedure executed.");
                        } else if (this.showComplete) {
                            String[] arrayOfString1 = TextUtils.toStringArray(TextUtils.getWords(
                                    command.COMMAND.substring(0,
                                            Math.min(100, command.COMMAND.length()))));
                            String[] arrayOfString2 = getCommandType().getCommandHint();
                            int k = commandAt(arrayOfString2, arrayOfString1);
                            if (k != -1) {
                                log.println(arrayOfString2[k] + " Succeed.");
                            } else {
                                log.println("Command completed.");
                            }
                        }
                    }
                }
                bool = statement.stmt.getMoreResults();
            } while ((bool) || (j != -1));
        } catch (SQLException localSQLException2) {
            log.print(localSQLException2);
        } finally {
            if ((statement != null) && (statement.stmt != null)) {
                clearWarnings(statement.stmt, log);
                try {
                    statement.stmt.close();
                } catch (SQLException localSQLException3) {
                }
            }
        }
        this.resultSet = null;
        this.currentStmt = null;
        log.setFormDisplay(false);
        clearWarnings(connection, log);
    }

    public final void executeCall(Connection connection, Command command, CommandLog log)
    {
        VariableTable localVariableTable = new VariableTable();
        executeCall(connection, command, localVariableTable, log);
    }

    public final void executeCall(Connection connection,
                                  Command command,
                                  VariableTable table,
                                  CommandLog log)
    {
        int i = 0;
        int j = -1;
        boolean bool = false;
        ResultSet localResultSet = null;
        SQLCallable localSQLCallable = null;
        String str = command.COMMAND;
        if (str == null) {
            return;
        }
        if (str.equalsIgnoreCase("COMMIT")) {
            doCommit(connection, log);
            return;
        }
        if (str.equalsIgnoreCase("ROLLBACK")) {
            doRollback(connection, log);
            return;
        }
        try {
            localSQLCallable = prepareCall(connection, str, table);
            if (localSQLCallable.stmt == null) {
                return;
            }
            localSQLCallable.bind(table);
            this.currentStmt = localSQLCallable.stmt;
            bool = localSQLCallable.stmt.execute();
            localSQLCallable.fetch(table);
            do {
                if (bool) {
                    localResultSet = localSQLCallable.stmt.getResultSet();
                    this.resultSet = localResultSet;
                    log.print(localResultSet);
                    this.resultSet = null;
                    localResultSet.close();
                } else {
                    doServerMessage();
                    try {
                        j = localSQLCallable.stmt.getUpdateCount();
                    } catch (SQLException localSQLException1) {
                        j = -1;
                    }
                    if (j >= 0) {
                        if ((command.TYPE1 == 0) || (command.TYPE1 == 1)) {
                            log.print(j);
                        }
                    } else if ((command.TYPE1 != 0) && (command.TYPE1 != 1)
                            && (this.showComplete)) {
                        log.println("Procedure executed.");
                    }
                }
                bool = localSQLCallable.stmt.getMoreResults();
            } while ((bool) || (j != -1));
        } catch (SQLException localSQLException2) {
            log.print(localSQLException2);
        } finally {
            if ((localSQLCallable != null) && (localSQLCallable.stmt != null)) {
                clearWarnings(localSQLCallable.stmt, log);
                try {
                    localSQLCallable.stmt.close();
                } catch (SQLException localSQLException3) {
                }
            }
        }
        this.currentStmt = null;
        this.resultSet = null;
        clearWarnings(connection, log);
    }

    public final String parseRecord(String paramString)
    {
        if ((paramString == null) || (paramString.length() == 0)) {
            return "\r\n";
        }
        char[] arrayOfChar = paramString.toCharArray();
        StringBuffer localStringBuffer = new StringBuffer();
        for (int i = 0; i < arrayOfChar.length; i++) {
            if (arrayOfChar[i] == '\\') {
                if (i + 1 < arrayOfChar.length) {
                    if (arrayOfChar[(i + 1)] == 'r') {
                        localStringBuffer.append('\r');
                    } else if (arrayOfChar[(i + 1)] == 'n') {
                        localStringBuffer.append('\n');
                    } else if (arrayOfChar[(i + 1)] == 't') {
                        localStringBuffer.append('\t');
                    } else if (arrayOfChar[(i + 1)] == 'b') {
                        localStringBuffer.append('\b');
                    } else if (arrayOfChar[(i + 1)] == 'f') {
                        localStringBuffer.append('\f');
                    } else if (arrayOfChar[(i + 1)] == 'x') {
                        if (i + 3 < arrayOfChar.length) {
                            try {
                                char c = (char) Byte.parseByte(
                                        String.valueOf(arrayOfChar, i + 2, 2), 16);
                                localStringBuffer.append(c);
                                i += 3;
                            } catch (NumberFormatException localNumberFormatException) {
                            }
                        }
                    } else {
                        localStringBuffer.append(arrayOfChar[i]);
                        localStringBuffer.append(arrayOfChar[(i + 1)]);
                    }
                    i += 1;
                } else {
                    localStringBuffer.append(arrayOfChar[i]);
                }
            } else {
                localStringBuffer.append(arrayOfChar[i]);
            }
        }
        return localStringBuffer.toString();
    }

    class CtrlC implements SignalHandler
    {

        CtrlC()
        {
        }

        public void handle(Signal paramSignal)
        {
            CommandExecutor.this.cancel();
        }
    }
}

