package org.apache.metron.management.utils;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUtils {
  public static String slurp(String loc) {
    try {
      return Joiner.on("\n").join(Files.readLines( new File(loc), Charset.defaultCharset())).trim();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
