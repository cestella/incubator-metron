package org.apache.metron.maas.service;

import com.google.common.collect.Maps;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.metron.maas.config.Model;
import org.apache.metron.maas.config.ModelRequest;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ContainerTracker {
  private TreeMap<Integer, BlockingQueue<Container>> acceptedContainersByResource = Maps.newTreeMap();
  private HashMap<Model, List<Container>> launchedContainers = Maps.newHashMap();

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
  public List<Container> getList(ModelRequest request) {
    synchronized(acceptedContainersByResource) {
      List<Container> containers = launchedContainers.get(new Model(request));
      if(containers == null) {
        containers = new ArrayList<>();
        launchedContainers.put(new Model(request), containers);
      }
      return containers;
    }
  }
  public void registerContainer(Resource resource, Container container ) {
    synchronized(acceptedContainersByResource) {
      BlockingQueue<Container> queue = getQueue(resource);
      queue.add(container);
    }
  }
  public void registerRequest(Container container, ModelRequest request) {
    synchronized (acceptedContainersByResource) {
      getList(request).add(container);
    }
  }
}
