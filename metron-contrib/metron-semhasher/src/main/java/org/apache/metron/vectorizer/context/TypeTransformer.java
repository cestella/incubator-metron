package org.apache.metron.vectorizer.context;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public interface TypeTransformer {
  Object typeSpecific(Object o);

  default Optional<String> toWord(String field, Object o, Object context, Map<String, Object> wordConfig) {
    if(o == null) {
      return Optional.empty();
    }
    String s = o.toString();
    return StringUtils.isEmpty(s)?Optional.empty():Optional.of(field + ":" + s);
  }

  default Optional<Object> init() {
    return Optional.empty();
  }

  default Object map(Object datum, Object context) {
    return null;
  }

  default Object reduce(Object left, Object right) {
    return null;
  }
}
