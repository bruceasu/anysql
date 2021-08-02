package com.asql.mysql.invoker;

import static com.asql.mysql.MySqlCMDType.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.sql.Types.*;

import com.asql.core.*;
import com.asql.core.log.CommandLog;
import com.asql.core.util.JavaVm;
import com.asql.core.util.TextUtils;
import com.asql.mysql.MySqlSQLExecutor;
import java.io.*;
import java.sql.*;

public class MultipleInvoker implements ModuleInvoker
{
    MySqlSQLExecutor executor;
    CommandLog       out;
    CMDType          cmdType;

    public MultipleInvoker(MySqlSQLExecutor executor)
    {
        this.executor = executor;
        out = executor.getCommandLog();
        cmdType = executor.getCmdType();
        
    }


    @Override
    public boolean invoke(Command cmd)
    {
        int id = executor.getMultipleID(cmd.command);
        switch (id) {
        case ASQL_MULTIPLE_LOB:
            return executor.time(cmd, this::procLob);
        case ASQL_MULTIPLE_LOBEXP:
            return executor.time(cmd, this::procLobExp);
        case ASQL_MULTIPLE_LOBLEN:
            return executor.time(cmd, this::procLobLen);
        case ASQL_MULTIPLE_LOBIMP:
            return executor.time(cmd, this::procLobImp);
        case ASQL_MULTIPLE_EXPLAIN:
            return executor.time(cmd, this::procExplain);
        default:
        }

        return true;
    }

    public Boolean procExplain(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ASQL_MULTIPLE_EXPLAIN]).size();
        String query = cmdLine;
        if (executor.checkNotConnected()) {
            return FALSE;
        }
        SQLStatement statement = null;
        ResultSet rs = null;
        try {
            statement = executor.prepareStatement(executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            rs = statement.stmt.executeQuery();
            this.out.print(rs);
            return TRUE;
        } catch (Exception e) {
            out.print(e);
            return FALSE;
        } finally {
            executor.clearWarnings(executor.database, out);
            closeQuietly(rs);
            closeQuietly(statement);
        }

    }


    public Boolean procLob(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ASQL_MULTIPLE_LOB]).size();
        String param = executor.skipWord(cmdLine, i);
        String direction = null;
        String file = null;
        int j = param.indexOf("<<");
        int k = param.indexOf(">>");
        if (j >= 0) {
            file = param.substring(0, j).trim();
            direction = param.substring(j + 2).trim();
            procLobForImport(file, direction);
            return TRUE;
        } else if (k >= 0) {
            file = param.substring(0, k).trim();
            direction = param.substring(k + 2).trim();
            procLobForExport(file, direction);
            return TRUE;
        } else {
            showUsageForLob();
            return FALSE;
        }


    }

    private void procLobForExport(String query, String file)
    {
        if ((JavaVm.MAIN_VERSION == 1) && (JavaVm.MINOR_VERSION < 3)) {
            out.println("Java VM 1.3 or above required to support this feature.");
            return;
        }
        if ((file == null) || (query == null) || (file.length() == 0) || (
                query.length() == 0)) {
            showUsageForLob();
            return;
        }
        file = executor.sysVariable.parseString(file);
        if (executor.checkNotConnected()) {
            return;
        }
        SQLStatement statement = null;
        ResultSet rs = null;
        File localFile = new File(file);
        try {
            statement = executor.prepareStatement(executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            rs = statement.stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnType = metaData.getColumnType(1);
            boolean col1TypeChar = columnType == LONGVARCHAR;
            boolean col1TypeVarchar = columnType == LONGVARBINARY;
            boolean col1TypeClob = columnType == CLOB;
            boolean col1TypeBlob = columnType == BLOB;

            if (rs.next()) {
                if (col1TypeChar) {
                    exportLongVarchar(rs, localFile, 1);
                } else if (col1TypeVarchar) {
                    exportLongBinary(rs, localFile, 1);
                } else {
                    if (col1TypeClob) {
                        exportClob(rs, localFile, 1);
                    } else if (col1TypeBlob) {
                        exportBlob(rs, localFile, 1);
                    }
                }
                out.println("Command succeed.");
            } else {
                out.println(" 0 record returned.");
            }
        } catch (Exception e) {
            out.print(e);
        }
        executor.clearWarnings(executor.database, out);
        closeQuietly(rs);
        closeQuietly(statement);
    }

    private void exportLongVarchar(ResultSet rs, File localFile, int col) throws SQLException, IOException
    {
        Reader reader = rs.getCharacterStream(col);
        if (reader != null) {
            try(FileWriter writer = new FileWriter(localFile)){
                copyStream(reader, writer);
            } catch (IOException e) {
                out.print(e);
            } finally {
                reader.close();
            }
        }
    }

    private void exportLongBinary(ResultSet rs, File localFile, int col) throws SQLException, IOException
    {
        InputStream inStream = rs.getBinaryStream(col);
        if (inStream != null) {
            try(FileOutputStream outputStream = new FileOutputStream(localFile);) {
                copyStream(inStream, outputStream);
            } catch (IOException localIOException2) {
                out.print(localIOException2);
            } finally {
                inStream.close();
            }
        }
    }

    private void exportClob(ResultSet rs, File localFile, int col) throws SQLException, IOException
    {
        Clob clob = rs.getClob(col);
        if (clob != null) {
            Reader reader = clob.getCharacterStream();
            if (reader != null) {
                try(FileWriter writer = new FileWriter(localFile);) {
                    copyStream(reader, writer);
                } catch (IOException e) {
                    out.print(e);
                } finally {
                    reader.close();
                }
            }
        }
    }

    private void exportBlob(ResultSet rs, File localFile, int col) throws SQLException, IOException
    {
        Blob blob = rs.getBlob(col);
        if (blob != null) {
            InputStream inputStream = blob.getBinaryStream();
            if (inputStream != null) {
                try(FileOutputStream outputStream = new FileOutputStream(localFile);) {
                    copyStream(inputStream, outputStream);
                } catch (IOException e) {
                    out.print(e);
                } finally {
                    inputStream.close();
                }
            }
        }
    }

    private void procLobForImport(String query, String file)
    {
        long l1 = 0L;
        if ((JavaVm.MAIN_VERSION == 1) && (JavaVm.MINOR_VERSION < 4)) {
            out.println("Java VM 1.4 or above required to support this feature.");
            return;
        }
        if (((file == null) && (query == null))
                || (file.length() == 0)
                || (query.length() == 0)) {
            showUsageForLob();
            return;
        }
        file = executor.sysVariable.parseString(file);
        if (executor.checkNotConnected()) {
            return;
        }
        SQLStatement statement = null;
        ResultSet rs = null;
        File localFile = new File(file);
        if (!localFile.exists()) {
            out.println("File " + file + " does not exists!");
            return;
        }
        if (!localFile.isFile()) {
            out.println(file + " is not a valid file!");
            return;
        }
        if (!localFile.canRead()) {
            out.println("Cannot read file " + file + "!");
            return;
        }
        try {
            statement = executor.prepareStatement(executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            rs = statement.stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnType = metaData.getColumnType(1);
            boolean col1TypeClob = columnType == CLOB;
            boolean col1TypeBlob = columnType == BLOB;
            if (rs.next()) {
                if (col1TypeClob) {
                    importClob(rs, localFile, 1);
                } else if (col1TypeBlob) {
                    importBlob(rs, localFile, 1);
                }
                out.println("Command succeed.");
            } else {
                out.println(" 0 record returned.");
            }
        } catch (Exception localException) {
            out.print(localException);
        }
        executor.clearWarnings(executor.database, out);
        closeQuietly(rs);
        closeQuietly(statement);

    }

    private void importBlob(ResultSet rs, File localFile, int col) throws SQLException
    {
        Blob blob = rs.getBlob(col);
        if (blob != null) {
            try(FileInputStream inputStream = new FileInputStream(localFile);) {
                blob.truncate(0);
                OutputStream outputStream = blob.setBinaryStream(0);
                copyStream(inputStream, outputStream);
                outputStream.close();
            } catch (IOException localIOException2) {
                out.print(localIOException2);
            }
        }
    }

    private void importClob(ResultSet rs, File localFile, int col) throws SQLException
    {
        Clob clob = rs.getClob(col);
        if (clob != null) {
            try(Reader reader = new FileReader(localFile)) {
                clob.truncate(0);
                Writer writer = clob.setCharacterStream(0);
                copyStream(reader, writer);
                writer.close();
            } catch (IOException e) {
                out.print(e);
            }
        }
    }

    private void showUsageForLob()
    {
        out.println("Usage:");
        out.println("  LOB query >> file");
        out.println("  LOB query << file");
        out.println("Note :");
        out.println("  >> mean export long/long raw/blob/clob to a file ");
        out.println("  << mean import a file to blob/clob field, the query");
        out.println("     should include the for update clause");
    }

    /**
     * export
     * @param cmdLine Command line
     * @return Boolean
     */
    public Boolean procLobExp(String cmdLine)
    {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[1]).size();
        String query = executor.skipWord(cmdLine, i);
        query = query.trim();
        if ((JavaVm.MAIN_VERSION == 1) && (JavaVm.MINOR_VERSION < 3)) {
            out.println("Java VM 1.3 required to support this feature.");
            return FALSE;
        }
        if (query.length() == 0) {
            showLobExpUsage();
            return FALSE;
        }
        if (executor.checkNotConnected()) {
            return FALSE;
        }
        SQLStatement statement = null;
        ResultSet rs = null;
        try {
            statement = executor.prepareStatement(executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            rs = statement.stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            boolean colCntNot2 = metaData.getColumnCount() != 2;
            // FIXME:这里是抄Oracle的未必对。
            int col1Type = metaData.getColumnType(1);
            int col2Type = metaData.getColumnType(2);

            // 第1行类型
            boolean col1TypeNot_VARCHAR = col1Type != VARCHAR;
            boolean col1TypeNot_CHAR = col1Type != CHAR;
            // 第2行类型
            boolean col2Type_LONGVARCHAR = col2Type == LONGVARCHAR;
            boolean col2Type_LONGVARBINARY = col2Type == LONGVARBINARY;
            boolean col2Type_CLOB = col2Type == CLOB;
            boolean col2Type_BLOB = col2Type != BLOB;

            boolean col2TypeNotChar = col2Type != LONGVARCHAR;
            boolean col2TypeNotVarchar = col2Type != LONGVARBINARY;
            boolean col2TypeNotBlob = col2Type != BLOB;
            boolean col2TypeNotClob = col2Type != CLOB;
            boolean condition2 = col1TypeNot_VARCHAR && col1TypeNot_CHAR;
            boolean condition3 = col2TypeNotChar
                    && col2TypeNotVarchar
                    && col2TypeNotBlob
                    && col2TypeNotClob;

            if (colCntNot2 || condition2 || condition3) {
                showLobExpUsage();
                closeQuietly(rs);
                closeQuietly(statement);
                return FALSE;
            }

            while (rs.next()) {
                String fileName = rs.getString(1);
                if (fileName == null) {
                    out.println("The file name is null!");
                } else {
                    out.println("Export lob data to file: " + fileName);
                }
                File file = new File(fileName);

                if (col2Type_LONGVARCHAR) {
                    exportLongVarchar(rs, file, 2);
                    continue;
                }
                if (col2Type_LONGVARBINARY) {
                    exportLongBinary(rs, file, 2);
                    continue;
                }

                if (col2Type_CLOB) {
                    exportClob(rs, file, 2);
                    continue;
                }

                if (col2TypeNotBlob) {
                    continue;
                }

                exportBlob(rs, file, 2);
            }
            out.println("Command succeed.");
        } catch (Exception localException) {
            out.print(localException);
        }
        executor.clearWarnings(executor.database, out);
        closeQuietly(rs);
        closeQuietly(statement);
        return TRUE;
    }

    private void showLobExpUsage()
    {
        out.println("Usage:");
        out.println("  LOBEXP query");
        out.println("Note :");
        out.println("  Query should return tow column as following:");
        out.println("  col1 : CHAR or VARCHAR specify the filename.");
        out.println("  col2 : long/long raw/blob/clob field.");
    }


    /**
     *
     * @param cmdLine
     * @return
     */
    public Boolean procLobLen(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ASQL_MULTIPLE_LOBLEN]).size();
        String query = executor.skipWord(cmdLine, i);
        query = query.trim();


        if ((JavaVm.MAIN_VERSION == 1) && (JavaVm.MINOR_VERSION < 3)) {
            out.println("Java VM 1.3 required to support this feature.");
            return FALSE;
        }
        if (query.length() == 0) {
            showLogLenUsage();
            return FALSE;
        }
        if (executor.checkNotConnected()) {
            return FALSE;
        }
        SQLStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = executor.prepareStatement(
                    executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            executor.currentStmt = statement.stmt;
            resultSet = statement.stmt.executeQuery();
            executor.resultSet = resultSet;
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnType = metaData.getColumnType(1);
            boolean notSingleColumn = metaData.getColumnCount() != 1;
            boolean isNot_LONGVARCHAR = columnType != LONGVARCHAR;
            boolean is_LONGVARCHAR = columnType == LONGVARCHAR;
            boolean isNot_LONGVARBINARY = columnType != LONGVARBINARY;
            boolean is_LONGVARBINARY = columnType == LONGVARBINARY;
            boolean isNot_BLOB = columnType != BLOB;
            boolean is_BLOB = columnType == BLOB;
            boolean isNot_CLOB = columnType != CLOB;
            boolean is_CLOB = columnType == CLOB;
            boolean isNot_CHAR = columnType != CHAR;
            boolean isNot_VARCHAR = columnType != VARCHAR;
            if (notSingleColumn
                    || (isNot_VARCHAR
                        && isNot_CHAR
                        && isNot_LONGVARCHAR
                        && isNot_LONGVARBINARY
                        && isNot_BLOB
                        && isNot_CLOB)) {
                showLogLenUsage();
                closeQuietly(resultSet);
                closeQuietly(statement);
                return true;
            }
            while (resultSet.next()) {
                long length = 0L;
                if (is_LONGVARCHAR) {
                    length = getLongVarcharLength(resultSet);
                } else if (is_LONGVARBINARY) {
                    length = getLongVarbinaryLength(resultSet);
                } else {
                    if (!is_CLOB) {
                        if (is_BLOB) {
                            length = getBlobLength(resultSet);
                        }
                    } else {
                        Clob clob = resultSet.getClob(1);
                        if (clob != null) {
                            length = getClobLength(clob);
                        }
                    }
                }
                out.println(length + "," + length / 1024L);
            }
        } catch (Exception localException) {
            out.print(localException);
            return FALSE;
        } finally {
            executor.currentStmt = null;
            executor.resultSet = null;
            executor.clearWarnings(executor.database, out);
            closeQuietly(resultSet);
            closeQuietly(statement);
        }
        return TRUE;
    }

    private long getClobLength(Clob clob) throws SQLException
    {
        long cnt = 0;
        Reader reader = clob.getCharacterStream();
        if (reader != null) {
            try {
                int n = 0;
                char[] buffer = new char[8196];
                while ((n = reader.read(buffer)) > 0) {
                    cnt += n;
                }
                reader.close();
            } catch (IOException e) {
                out.println();
                out.print(e);
            }
        }
        return cnt;
    }

    private long getBlobLength(ResultSet resultSet) throws SQLException
    {
        long cnt = 0;
        Blob blob = resultSet.getBlob(1);
        if (blob != null) {
            InputStream inStream = blob.getBinaryStream();
            if (inStream != null) {
                try {
                    int k = 0;
                    byte[] buffer = new byte[8196];
                    while ((k = inStream.read(buffer)) > 0) {
                        cnt += k;
                    }
                    inStream.close();
                } catch (IOException localIOException4) {
                    out.println();
                    out.print(localIOException4);
                }
            }
        }
        return cnt;
    }

    private long getLongVarbinaryLength(ResultSet resultSet) throws SQLException
    {
        int cnt = 0;
        InputStream inStream = resultSet.getBinaryStream(1);
        if (inStream != null) {
            try {
                int k = 0;
                byte[] buffer = new byte[8192];
                while ((k = inStream.read(buffer)) > 0) {
                    cnt += k;
                }
                inStream.close();
            } catch (IOException e) {
                out.println();
                out.print(e);
            }
        }
        return cnt;
    }

    private long getLongVarcharLength(ResultSet resultSet) throws SQLException
    {
        long cnt = 0;
        Reader reader = resultSet.getCharacterStream(1);
        if (reader != null) {
            try {
                char[] buffer = new char[8192];
                int k = 0;
                while ((k =reader.read(buffer)) > 0) {
                    cnt += k;
                }
                reader.close();
            } catch (IOException e) {
                out.println();
                out.print(e);
            }
        }
        return cnt;
    }

    private void showLogLenUsage()
    {
        out.println("Usage:");
        out.println("  LOBLEN query");
        out.println("Note :");
        out.println("  Query should return one column as following:");
        out.println("  col1 : long/long raw/blob/clob field.");
    }

    public Boolean procLobImp(String cmdLine) {
        int i = TextUtils.getWords(cmdType.getASQLMultiple()[ASQL_MULTIPLE_LOBIMP]).size();
        String query = executor.skipWord(cmdLine, i);
        query = query.trim();
        char[] buffer = new char[8192];
        byte[] arrayOfByte = new byte[8192];
        if ((JavaVm.MAIN_VERSION == 1) && (JavaVm.MINOR_VERSION < 4)) {
            out.println("Java VM 1.4 required to support this feature.");
            return FALSE;
        }
        if (query.length() == 0) {
            showLobImpUsage();
            return FALSE;
        }
        if (executor.checkNotConnected()) {
            return FALSE;
        }
        SQLStatement statement = null;
        ResultSet rs = null;
        try {
            statement = executor.prepareStatement(
                    executor.database, query, executor.sysVariable);
            statement.bind(executor.sysVariable);
            executor.currentStmt = statement.stmt;
            rs = statement.stmt.executeQuery();
            executor.resultSet = rs;
            ResultSetMetaData metaData = rs.getMetaData();
            boolean colCntIsNot2 = metaData.getColumnCount() != 2;
            int col1Type = metaData.getColumnType(1);
            int col2Type = metaData.getColumnType(2);
            boolean col1IsNot_VARCHAR = col1Type != VARCHAR;
            boolean col1IsNot_CHAR = col1Type != CHAR;
            boolean col2IsNot_BLOB = col2Type != BLOB;
            boolean col2IsNot_CLOB = col2Type != CLOB;
            boolean col1TypeIsNotAccept = col1IsNot_VARCHAR && col1IsNot_CHAR;
            boolean col2TypeIsNotAccept = col2IsNot_BLOB && col2IsNot_CLOB;
            if (colCntIsNot2 || col1TypeIsNotAccept || col2TypeIsNotAccept) {
                showLobImpUsage();
                closeQuietly(rs);
                closeQuietly(statement);
                return false;
            }

            boolean col2Is_BLOB = col2Type == BLOB;
            boolean col2Is_CLOB = col2Type == CLOB;
            while (rs.next()) {
                String filename = rs.getString(1);
                File localFile = new File(filename);
                if (!localFile.exists()) {
                    out.println("File " + filename + " does not exists!");
                    continue;
                }
                if (!localFile.isFile()) {
                    out.println(filename + " is not a valid file!");
                    continue;
                }
                if (!localFile.canRead()) {
                    out.println("Cannot read file " + filename + "!");
                    continue;
                }
                long l2;
                if (col2Is_CLOB) {
                    importClob(rs, localFile, 2);
                } else if (col2Is_BLOB) {
                    importBlob(rs, localFile, 2);
                }
                out.println("File " + filename + " loaded.");
            }
            out.println("Command succeed.");
        } catch (Exception e) {
            out.print(e);
        }
        executor.currentStmt = null;
        executor.resultSet = null;
        executor.clearWarnings(executor.database, out);
        closeQuietly(rs);
        closeQuietly(statement);
        return true;
    }

    private void showLobImpUsage() {
        out.println("Usage:");
        out.println("  LOBIMP query");
        out.println("Note :");
        out.println("  Query should return tow column as following:");
        out.println("  col1 : CHAR or VARCHAR specify the filename.");
        out.println("  col2 : blob/clob field.");
    }


    private void closeQuietly(AutoCloseable rs)
    {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            out.print(e);
        }
    }

    private void copyStream(Reader reader, Writer writer) throws IOException
    {
        int i = 0;
        char[] buffer = new char[8192];
        while ((i = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, i);
        }
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream)
    throws IOException
    {
        int i = 0;
        byte[] buffer = new byte[8192];
        while ((i = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, i);
        }
    }




}
