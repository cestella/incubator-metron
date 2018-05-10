package org.apache.metron.vectorizer.type;

import org.apache.metron.vectorizer.context.TypeTransformer;

import java.util.Map;
import java.util.Optional;

public enum Type implements TypeTransformer {
  NUMERIC(new NumericTypeTransformer()),
  TEXT(new TextTypeTransformer())
  ;
  TypeTransformer func;
  Type(TypeTransformer func) {
    this.func = func;
  }

  @Override
  public Object typeSpecific(Object o) {
    return func.typeSpecific(o);
  }

  @Override
  public Optional<String> toWord(String field, Object o, Object context, Map<String, Object> wordConfig) {
    return func.toWord(field, o, context, wordConfig);
  }

  @Override
  public Optional<Object> init() {
    return func.init();
  }

  @Override
  public Object map(Object datum, Object context) {
    return func.map(datum, context);
  }

  @Override
  public Object reduce(Object left, Object right) {
    return func.reduce(left, right);
  }
}
