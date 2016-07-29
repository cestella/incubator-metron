package org.apache.metron.maas.util;

import java.util.ArrayList;
import java.util.List;

public enum Utils {
  INSTANCE;
  public <T> List<T> toList(T[] arr) {
    List<T> ret = new ArrayList<T>(arr.length);
    for(T o : arr) {
      ret.add(o);
    }
    return ret;
  }
}
