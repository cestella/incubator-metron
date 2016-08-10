package org.apache.metron.common.dsl;

import java.util.List;
import java.util.Map;

public abstract class BaseStellarFunction implements StellarFunction {
  public abstract Object apply(List<Object> args);

  @Override
  public Object apply(List<Object> args, Context context) throws ParseException {
    return apply(args);
  }

  @Override
  public void initialize(Context context) {

  }
}
