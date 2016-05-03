package org.apache.metron.threatintel.triage.predicate;


import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.util.List;

public enum StringFunctions implements Function<List<String>, String> {
  TO_LOWER(strings -> strings.get(0).toLowerCase())
  ,TO_UPPER(strings -> strings.get(0).toUpperCase())
  ,TRIM(strings -> strings.get(0).trim())
  ;
  Function<List<String>, String> func;
  StringFunctions(Function<List<String>, String> func) {
    this.func = func;
  }

  @Nullable
  @Override
  public String apply(@Nullable List<String> input) {
    return func.apply(input);
  }
}
