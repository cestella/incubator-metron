package org.apache.metron.elasticsearch.utils;

import org.apache.commons.collections4.map.AbstractMapDecorator;

import java.util.HashMap;
import java.util.Map;

public class FieldProperties extends AbstractMapDecorator<String, Object> {
  public FieldProperties() {
    super(new HashMap<>());
  }

  public FieldProperties(Map<String, Object> m) {
    super(m);
  }
}
