package org.apache.metron.vectorizer.transform;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.metron.vectorizer.config.Config;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public enum TransformUtil {
  INSTANCE;



  public List<Map<String, Object>> transform(Config config, byte[] message) {
    Parse parse = new Parse(config);
    Transform transform = new Transform(config);
    List<JSONObject> parsed = parse.apply(message);
    return Lists.newArrayList(Iterables.transform(parsed, transform));
  }

  private static class TransformMapper implements FlatMapFunction<byte[], Map<String, Object>> {
    Config config;
    transient boolean initialized = false;
    public TransformMapper(Config config) {
      this.config = config;
    }

    private synchronized Config getConfig() {
      if(!initialized) {
        config.initialize();
        initialized = true;
      }
      return config;
    }

    @Override
    public Iterable<Map<String, Object>> call(byte[] b) throws Exception {
      return TransformUtil.INSTANCE.transform(getConfig(), b);
    }
  }

  public JavaRDD<Map<String, Object>> transform(Config config, JavaRDD<byte[]> messages) {
    return messages.flatMap(new TransformMapper(config));
  }

}
