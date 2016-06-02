package org.apache.metron.common.dsl;

import org.apache.metron.common.dsl.functions.DateFunctions;
import org.apache.metron.common.dsl.functions.NetworkFunctions;
import org.apache.metron.common.dsl.functions.StringFunctions;

import java.util.List;
import java.util.function.Function;

public enum TransformationFunctions implements Function<List<Object>, Object> {
  TO_LOWER(strings -> strings.get(0)==null?null:strings.get(0).toString().toLowerCase())
  ,TO_UPPER(strings -> strings.get(0) == null?null:strings.get(0).toString().toUpperCase())
  ,TRIM(strings -> strings.get(0) == null?null:strings.get(0).toString().trim())
  ,JOIN(new StringFunctions.JoinFunction())
  ,SPLIT(new StringFunctions.SplitFunction())
  ,GET_FIRST(new StringFunctions.GetFirst())
  ,GET_LAST(new StringFunctions.GetLast())
  ,GET(new StringFunctions.Get())
  ,TO_TLD(new NetworkFunctions.ExtractTLD())
  ,REMOVE_TLD(new NetworkFunctions.RemoveTLD())
  ,URL_TO_HOST(new NetworkFunctions.URLToHost())
  ,URL_TO_PORT(new NetworkFunctions.URLToPort())
  ,URL_TO_PATH(new NetworkFunctions.URLToPath())
  ,URL_TO_PROTOCOL(new NetworkFunctions.URLToProtocol())
  ,TO_TIMESTAMP(new DateFunctions.ToTimestamp())
  ;
  Function<List<Object>, Object> func;
  TransformationFunctions(Function<List<Object>, Object> func) {
    this.func = func;
  }

  @Override
  public Object apply(List<Object> input) {
    return func.apply(input);
  }
}
