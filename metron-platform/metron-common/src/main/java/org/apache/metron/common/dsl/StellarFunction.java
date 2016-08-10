package org.apache.metron.common.dsl;

import java.util.List;

public interface StellarFunction {
  Object apply(List<Object> args, Context context) throws ParseException;
  void initialize(Context context);
}
