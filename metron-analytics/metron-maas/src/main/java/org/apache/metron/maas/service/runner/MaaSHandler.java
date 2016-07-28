package org.apache.metron.maas.service.runner;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.metron.maas.config.MaaSConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MaaSHandler {

  protected CuratorFramework client;
  protected NodeCache cache;
  protected MaaSConfig config;
  private String zkQuorum;
  private String root;
  private ObjectMapper _mapper = new ObjectMapper();
  protected ReadWriteLock lock = new ReentrantReadWriteLock();

  public MaaSHandler(String zkQuorum, String root) {
    this.zkQuorum = zkQuorum;
    this.root = root;
  }

  public MaaSConfig getConfig() {
    return config;
  }

  public CuratorFramework getClient() {
    return client;
  }



  public void start() throws Exception {
    if (client == null) {
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
      client = CuratorFrameworkFactory.newClient(zkQuorum, retryPolicy);
    }
    cache = new NodeCache(client, root);
    cache.getListenable().addListener(() -> {
              byte[] data = cache.getCurrentData().getData();
              Lock wLock= lock.writeLock();
              wLock.lock();
              try {
                config = _mapper.readValue(data, MaaSConfig.class);
              }
              finally {
                wLock.unlock();
              }
            }
    );
  }
}
