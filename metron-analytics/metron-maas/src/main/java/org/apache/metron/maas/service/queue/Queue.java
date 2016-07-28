package org.apache.metron.maas.service.queue;

import org.apache.metron.maas.config.ModelRequest;

import java.util.Map;

public interface Queue {
  ModelRequest dequeue();
  void enqueue(ModelRequest request);
  void configure(Map<String, Object> config);
}
