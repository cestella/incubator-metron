package org.apache.metron.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum KafkaUtils {
  INSTANCE;
  public List<String> getBrokersFromZookeeper(String zkQuorum) throws Exception {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework framework = CuratorFrameworkFactory.newClient(zkQuorum, retryPolicy);
    framework.start();
    try {
      return getBrokersFromZookeeper(framework);
    } finally {
      framework.close();
    }
  }
  public List<String> getBrokersFromZookeeper(CuratorFramework client) throws Exception {
    List<String> ret = new ArrayList<>();
    for(String id : client.getChildren().forPath("/brokers/ids")) {
      byte[] data = client.getData().forPath("/brokers/ids/" + id);
      String brokerInfoStr = new String(data);
      Map<String, Object> brokerInfo = JSONUtils.INSTANCE.load(brokerInfoStr, new TypeReference<Map<String, Object>>() {
      });
      ret.add(brokerInfo.get("host") + ":" + brokerInfo.get("port"));
    }
    return ret;
  }
}
