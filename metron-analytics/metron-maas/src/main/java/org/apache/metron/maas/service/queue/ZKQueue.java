package org.apache.metron.maas.service.queue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.apache.metron.maas.service.ConfigUtil;
import org.apache.metron.maas.config.ModelRequest;

import java.util.Map;

public class ZKQueue implements Queue {
  public static String ZK_PATH = "zk_path";
  public static String ZK_CLIENT = "zk_client";
  private SimpleDistributedQueue queue;
  @Override
  public ModelRequest dequeue() {
    try {
      byte[] payload = queue.take();
      return ConfigUtil.INSTANCE.read(payload, ModelRequest.class);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to dequeue: " + e.getMessage(), e);
    }
  }

  @Override
  public void enqueue(ModelRequest request) {
    try {
      byte[] payload = ConfigUtil.INSTANCE.toBytes(request);
      queue.offer(payload);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to enqueue: " + e.getMessage(), e);
    }
  }

  @Override
  public void configure(Map<String, Object> config) {
    String path = (String)config.get(ZK_PATH);
    if(path == null) {
      throw new IllegalStateException("You must specify " + ZK_PATH + " for a zk queue");
    }
    CuratorFramework client = (CuratorFramework) config.get(ZK_CLIENT);
    queue = new SimpleDistributedQueue(client, path);
  }
}
