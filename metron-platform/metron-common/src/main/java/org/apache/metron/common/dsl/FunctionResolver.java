package org.apache.metron.common.dsl;

import java.util.function.Function;

public interface FunctionResolver extends Function<String, StellarFunction> {
  void initializeFunctions(Context context);
}
