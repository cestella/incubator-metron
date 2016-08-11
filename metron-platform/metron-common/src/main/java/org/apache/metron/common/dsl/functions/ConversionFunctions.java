package org.apache.metron.common.dsl.functions;

import org.apache.metron.common.dsl.BaseStellarFunction;
import org.apache.metron.common.utils.ConversionUtils;

import java.util.List;

public class ConversionFunctions {
  public static class Cast<T> extends BaseStellarFunction {
    Class<T> clazz;
    public Cast(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override
    public Object apply(List<Object> strings ) {
      return strings.get(0) == null?null: ConversionUtils.convert(strings.get(0), clazz);
    }
  }
}
