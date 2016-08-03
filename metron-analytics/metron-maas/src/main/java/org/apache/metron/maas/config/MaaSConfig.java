package org.apache.metron.maas.config;

import org.apache.metron.maas.service.queue.Queue;
import org.apache.metron.maas.service.queue.QueueHandler;
import org.apache.metron.maas.service.queue.ZKQueue;

import java.util.HashMap;
import java.util.Map;

public class MaaSConfig {
  private QueueHandler queue = QueueHandler.ZOOKEEPER;
  private Map<String, Object> queueConfig = new HashMap<String, Object>() {{
    put(ZKQueue.ZK_PATH, "/maas/queue");
  }};
  private String serviceRoot = "/maas/service";

  public String getServiceRoot() {
    return serviceRoot;
  }

  public void setServiceRoot(String serviceRoot) {
    this.serviceRoot = serviceRoot;
  }


  public QueueHandler getQueue() {
    return queue;
  }

  public void setQueue(QueueHandler queue) {
    this.queue = queue;
  }

  public Map<String, Object> getQueueConfig() {
    return queueConfig;
  }

  public void setQueueConfig(Map<String, Object> queueConfig) {
    this.queueConfig = queueConfig;
  }
  public Queue createQueue(Map<String, Object> additionalConfig) {
    Map<String, Object> configs = new HashMap<>(getQueueConfig());
    configs.putAll(additionalConfig);
    return getQueue().create(configs);
  }

}
