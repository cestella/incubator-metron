package org.apache.metron.maas.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import org.apache.metron.maas.config.Model;
import org.apache.metron.maas.config.ModelEndpoint;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServiceDiscoverer {
  private static final Log LOG = LogFactory.getLog(ServiceDiscoverer.class);
  private ServiceCache<ModelEndpoint> cache;
  private ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private ServiceDiscovery<ModelEndpoint> serviceDiscovery;
  private Map<Model, List<ModelEndpoint>> state = new HashMap<>();
  private Map<String, ServiceInstance<ModelEndpoint>> containerToEndpoint = new HashMap<>();

  public ServiceDiscoverer(CuratorFramework client, String root) {
    JsonInstanceSerializer<ModelEndpoint> serializer = new JsonInstanceSerializer<>(ModelEndpoint.class);
    serviceDiscovery =ServiceDiscoveryBuilder.builder(ModelEndpoint.class)
                .client(client)
                .basePath(root)
                .serializer(serializer)
                .build();
    cache = serviceDiscovery.serviceCacheBuilder().name("dummy").build();
    cache.addListener(new ServiceCacheListener() {
      @Override
      public void cacheChanged() {
        updateState();
      }

      @Override
      public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
      }
    });
  }

  private void updateState() {
    Map<Model, List<ModelEndpoint>> state = new HashMap<>();
    Map<String, ServiceInstance<ModelEndpoint>> containerToEndpoint = new HashMap<>();
    try {
      for(String name : serviceDiscovery.queryForNames()) {
        for(ServiceInstance<ModelEndpoint> endpoint: serviceDiscovery.queryForInstances(name)) {
          ModelEndpoint ep = endpoint.getPayload();
          LOG.info("Found model endpoint " + ep);
          containerToEndpoint.put(ep.getContainerId(), endpoint);
          Model model = new Model(ep.getName(), ep.getVersion());
          List<ModelEndpoint> endpoints = state.get(model);
          if(endpoints == null) {
            endpoints = new ArrayList<>();
            state.put(model, endpoints);
          }
          endpoints.add(ep);
        }
      }
      rwLock.writeLock().lock();
      this.state = state;
      this.containerToEndpoint = containerToEndpoint;
      rwLock.writeLock().unlock();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
    }

  }


  public void start() {
    try {
      serviceDiscovery.start();
      cache.start();
      updateState();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new IllegalStateException("Unable to start", e);
    }
  }

  public void unregisterByContainer(String containerId) {
    rwLock.readLock().lock();
    try {
      ServiceInstance<ModelEndpoint> ep = containerToEndpoint.get(containerId);
      if(ep != null) {
        serviceDiscovery.unregisterService(ep);
        LOG.info("Unregistered endpoint " + ep.getPayload());
      }
      else {
        LOG.warn("Unable to find registered model associated with container " + containerId);
      }
    } catch (Exception e) {
      LOG.error("Unable to unregister container " + containerId + " due to: " + e.getMessage(), e);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  public List<ModelEndpoint> getEndpoints(Model model) {
    rwLock.readLock().lock();
    try {
      return state.getOrDefault(model, new ArrayList<>());
    }
    finally {
      rwLock.readLock().unlock();
    }
  }

  public ModelEndpoint getEndpoint(Model model) {
    rwLock.readLock().lock();
    try {
      List<ModelEndpoint> endpoints = state.get(model);
      ModelEndpoint ret = null;
      if(endpoints != null) {
        int i = ThreadLocalRandom.current().nextInt(endpoints.size());
        ret = endpoints.get(i);
      }
      return ret;
    }
    finally {
      rwLock.readLock().unlock();
    }
  }
}
