package org.apache.metron.vectorizer.type;

import org.apache.metron.vectorizer.context.TypeTransformer;

public class TextTypeTransformer implements TypeTransformer {
  @Override
  public Object typeSpecific(Object o) {
    return o == null?null:o.toString();
  }
}
