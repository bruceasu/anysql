package com.asql.core.io;

import java.io.IOException;

public interface CommandReader {
  String readline()
      throws IOException;

  String readPassword()
      throws Exception;

  String getWorkingDir();

  void setWorkingDir(String paramString);
}
