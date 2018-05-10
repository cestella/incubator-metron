package org.apache.metron.vectorizer.transform;

import com.google.common.base.Function;
import org.apache.metron.vectorizer.config.FieldTransformation;
import org.apache.metron.vectorizer.config.Config;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Transform implements Function<JSONObject, Map<String, Object>> {
  private Config config;
  public Transform(Config config) {
    this.config = config;
  }

  @Nullable
  @Override
  public Map<String, Object> apply(@Nullable JSONObject message) {
    Map<String, Object> ret = new LinkedHashMap<>();
    for(Map.Entry<String, FieldTransformation> kv : config.getSchema().entrySet()) {
      Object raw = message.get(kv.getKey());
      if(raw == null) {
        continue;
      }
      Object transformed = kv.getValue().getType().typeSpecific(raw);
      if(transformed != null) {
        ret.put(kv.getKey(), transformed);
      }
    }
    return ret;
  }
}
