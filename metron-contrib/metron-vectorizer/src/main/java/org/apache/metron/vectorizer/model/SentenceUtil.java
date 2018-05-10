package org.apache.metron.vectorizer.model;

import org.apache.metron.vectorizer.config.FieldTransformation;
import org.apache.metron.vectorizer.config.Config;
import org.apache.metron.vectorizer.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum SentenceUtil {
  INSTANCE;

  public Iterable<String> toSentence(Map<String, Object> message, Context contexts, Config config, boolean normalize) {
    List<String> ret = new ArrayList<>();
    for(Map.Entry<String, FieldTransformation> kv : config.getSchema().entrySet()) {
      Object raw = message.get(kv.getKey());
      if (raw == null) {
        continue;
      }

      Object context = contexts.getContext().get(kv.getKey());
      FieldTransformation transformation = kv.getValue();
      if(normalize) {
        raw = transformation.getType().typeSpecific(raw);
        if(raw == null) {
          continue;
        }
      }
      Optional<String> word = transformation.getType().toWord(kv.getKey(), raw, context, transformation.getTypeConfig());
      if(word.isPresent()) {
        ret.add(word.get());
      }
    }
    return ret;
  }
}
