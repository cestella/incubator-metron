package org.apache.metron.maas.service;

import com.google.common.collect.Maps;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ContainerTracker {
  private TreeMap<Integer, BlockingQueue<Container>> acceptedContainersByResource = Maps.newTreeMap();
  int minimumContainerSize;
  public ContainerTracker(int minimumContainerSize) {
    this.minimumContainerSize = minimumContainerSize;
  }

  public int getAdjustedSize(int size) {
    return (int)(minimumContainerSize * Math.ceil(1.0*size/minimumContainerSize));
  }

  public BlockingQueue<Container> getQueue(Resource resource) {
    synchronized(acceptedContainersByResource) {
      int key = getAdjustedSize(resource.getMemory());
      BlockingQueue<Container> queue = acceptedContainersByResource.get(key);
      if(queue == null) {
        queue = new LinkedBlockingDeque<>();
        acceptedContainersByResource.put(key,queue );
      }
      return queue;
    }
  }
  public void registerContainer(Resource resource, Container container) {
    synchronized(acceptedContainersByResource) {
      int key = getAdjustedSize(resource.getMemory());
      BlockingQueue<Container> queue = getQueue(resource);
      queue.add(container);
    }
  }

}
