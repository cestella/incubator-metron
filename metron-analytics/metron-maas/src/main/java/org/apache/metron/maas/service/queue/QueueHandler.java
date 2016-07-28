package org.apache.metron.maas.service.queue;

import java.util.Map;
import java.util.function.Function;

public enum QueueHandler {
  ZOOKEEPER(config -> {
    Queue ret = new ZKQueue();
    ret.configure(config);
    return ret;
  });
  Function<Map<String, Object>, Queue> queueCreator;
  QueueHandler(Function<Map<String, Object>, Queue> creator) {
    this.queueCreator = creator;
  }
  public Queue create(Map<String, Object> config) {
    return queueCreator.apply(config);
  }
}
