package org.apache.metron.common.dsl.functions;

import org.apache.metron.common.dsl.BaseStellarFunction;

import java.util.Collection;
import java.util.List;

public class DataStructureFunctions {
  public static class IsEmpty extends BaseStellarFunction {

    @Override
    public Object apply(List<Object> list) {
      if(list.size() == 0) {
        throw new IllegalStateException("IS_EMPTY expects one string arg");
      }
      Object o = list.get(0);
      if(o instanceof Collection) {
        return ((Collection)o).isEmpty();
      }
      else if(o instanceof String) {
        String val = (String) list.get(0);
        return val == null || val.isEmpty() ? true : false;
      }
      else {
        throw new IllegalStateException("IS_EMPTY expects a collection or string");
      }
    }
  }
}
