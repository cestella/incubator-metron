package org.apache.metron.common.configuration.enrichment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrichmentConfig {
  private Map<String, List<String>> fieldMap = new HashMap<>();
  private Map<String, List<String>> fieldToTypeMap = new HashMap<>();

  public Map<String, List<String>> getFieldMap() {
    return fieldMap;
  }

  public void setFieldMap(Map<String, List<String>> fieldMap) {
    this.fieldMap = fieldMap;
  }

  public Map<String, List<String>> getFieldToTypeMap() {
    return fieldToTypeMap;
  }

  public void setFieldToTypeMap(Map<String, List<String>> fieldToTypeMap) {
    this.fieldToTypeMap = fieldToTypeMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EnrichmentConfig that = (EnrichmentConfig) o;

    if (getFieldMap() != null ? !getFieldMap().equals(that.getFieldMap()) : that.getFieldMap() != null) return false;
    return getFieldToTypeMap() != null ? getFieldToTypeMap().equals(that.getFieldToTypeMap()) : that.getFieldToTypeMap() == null;

  }

  @Override
  public int hashCode() {
    int result = getFieldMap() != null ? getFieldMap().hashCode() : 0;
    result = 31 * result + (getFieldToTypeMap() != null ? getFieldToTypeMap().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EnrichmentConfig{" +
            "fieldMap=" + fieldMap +
            ", fieldToTypeMap=" + fieldToTypeMap +
            '}';
  }
}
