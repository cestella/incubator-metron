package org.apache.metron.common.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import org.apache.metron.common.Constants;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.common.utils.JSONUtils;

import java.io.IOException;
import java.util.Map;

public enum ConfigurationType implements Function<String, Object> {
  GLOBAL("."
        ,Constants.ZOOKEEPER_GLOBAL_ROOT
        , s -> {
    try {
      return JSONUtils.INSTANCE.load(s, new TypeReference<Map<String, Object>>() {
      });
    } catch (IOException e) {
      throw new RuntimeException("Unable to load " + s, e);
    }
  })
  , SENSOR(Constants.SENSORS_CONFIG_NAME
          ,Constants.ZOOKEEPER_SENSOR_ROOT
          , s -> {
    try {
      return JSONUtils.INSTANCE.load(s, SensorEnrichmentConfig.class);
    } catch (IOException e) {
      throw new RuntimeException("Unable to load " + s, e);
    }
  });
  String directory;
  String zookeeperRoot;
  Function<String,?> deserializer;
  ConfigurationType(String directory, String zookeeperRoot, Function<String, ?> deserializer) {
    this.directory = directory;
    this.zookeeperRoot = zookeeperRoot;
    this.deserializer = deserializer;
  }

  public String getDirectory() {
    return directory;
  }

  public Object deserialize(String s)
  {
    return deserializer.apply(s);
  }
  @Override
  public Object apply(String s) {
    return deserialize(s);
  }

  public String getZookeeperRoot() {
    return zookeeperRoot;
  }
}
