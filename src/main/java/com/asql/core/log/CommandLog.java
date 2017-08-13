package com.asql.core.log;

import com.asql.core.DBRowCache;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface CommandLog {
  void println();

  void print(ResultSet paramResultSet) throws SQLException;

  void print(DBRowCache paramDBRowCache);

  void print(int paramInt);

  void print(String paramString);

  void println(String paramString);

  void print(Exception paramException);

  void print(SQLException paramSQLException);

  void prompt(String paramString);

  int getPagesize();

  void setPagesize(int paramInt);

  void setSeperator(String paramString);

  void setRecord(String paramString);

  void setHeading(boolean paramBoolean);

  String getSeperator();

  String getRecord();

  boolean getHeading();

  void setAutoTrace(boolean paramBoolean);

  void setTermOut(boolean paramBoolean);

  void close();

  void setLogFile(CommandLog paramCommandLog);

  CommandLog getLogFile();

  void setFormDisplay(boolean paramBoolean);

  boolean getFormDisplay();
}