package org.apache.metron.common.query;

public class ParseException extends RuntimeException {
  public ParseException(String reason) {
    super(reason);
  }
  public ParseException(String reason, Throwable t) {
    super(reason, t);
  }
}
