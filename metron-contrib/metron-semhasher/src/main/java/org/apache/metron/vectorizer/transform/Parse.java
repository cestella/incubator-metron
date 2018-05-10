package org.apache.metron.vectorizer.transform;

import com.google.common.base.Function;
import org.apache.metron.vectorizer.config.Config;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.List;

public final class Parse implements Function<byte[], List<JSONObject>> {
  private Config config;
  public Parse(Config config) {
    this.config = config;
  }

  @Nullable
  @Override
  public List<JSONObject> apply(@Nullable byte[] message) {
    return config.getParser().parse(message);
  }
}
