package org.apache.metron.vectorizer.context;

import java.io.Serializable;
import java.util.Map;

public class Context implements Serializable {
  Map<String, Object> context;
  public Context(Map<String, Object> context) {
    this.context = context;
  }

  public Context() {}

  public Map<String, Object> getContext() {
    return context;
  }

  public void setContext(Map<String, Object> context) {
    this.context = context;
  }
}
