package org.apache.metron.common.dsl;

import java.util.List;

public class StellarFunctionInfo {
  String description;
  String name;
  String[] params;
  StellarFunction function;
  public StellarFunctionInfo(String description, String name, String[] params, StellarFunction function) {
    this.description = description;
    this.name = name;
    this.params = params;
    this.function = function;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public String[] getParams() {
    return params;
  }

  public StellarFunction getFunction() {
    return function;
  }
}
