package org.apache.metron.vectorizer.config;

import org.apache.metron.vectorizer.type.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FieldTransformation implements Serializable {
  private Type type;
  private Map<String, Object> typeConfig = new HashMap<>();

  public FieldTransformation() {

  }

  public FieldTransformation(Type type, Map<String, Object> typeConfig) {
    this.type = type;
    this.typeConfig = typeConfig;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Map<String, Object> getTypeConfig() {
    return typeConfig;
  }

  public void setTypeConfig(Map<String, Object> typeConfig) {
    this.typeConfig = typeConfig;
  }
}
