package org.apache.metron.common.dsl.functions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.hadoop.security.authorize.Service;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.ParseException;
import org.apache.metron.common.dsl.StellarFunction;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.maas.config.Endpoint;
import org.apache.metron.maas.config.MaaSConfig;
import org.apache.metron.maas.config.ModelEndpoint;
import org.apache.metron.maas.discovery.ServiceDiscoverer;
import org.apache.metron.maas.util.ConfigUtil;
import org.apache.metron.maas.util.RESTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MaaSFunctions {
 protected static final Logger LOG = LoggerFactory.getLogger(MaaSFunctions.class);
  private static class ModelCacheKey {
    String name;
    String version;
    String method;
    Map<String, String> args;
    public ModelCacheKey(String name, String version, String method, Map<String, String> args) {
      this.name = name;
      this.version = version;
      this.method = method;
      this.args = args;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ModelCacheKey that = (ModelCacheKey) o;

      if (name != null ? !name.equals(that.name) : that.name != null) return false;
      if (version != null ? !version.equals(that.version) : that.version != null) return false;
      if (method != null ? !method.equals(that.method) : that.method != null) return false;
      return args != null ? args.equals(that.args) : that.args == null;

    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (version != null ? version.hashCode() : 0);
      result = 31 * result + (method != null ? method.hashCode() : 0);
      result = 31 * result + (args != null ? args.hashCode() : 0);
      return result;
    }
  }
  public static class ModelApply implements StellarFunction {
    private ServiceDiscoverer discoverer;
    private Cache<ModelCacheKey, Map<String, Object> > resultCache;
    public ModelApply() {
      resultCache = CacheBuilder.newBuilder()
                            .concurrencyLevel(4)
                            .weakKeys()
                            .maximumSize(100000)
                            .expireAfterWrite(10, TimeUnit.MINUTES)
                            .build();
    }

    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      if(args.size() < 2) {
        throw new ParseException("Unable to execute model_apply. " +
                                 "Expected arguments: endpoint_map:map, " +
                                 " [endpoint method:string], model_args:map"
                                 );
      }
      int i = 0;
      if(args.size() == 0) {
        return null;
      }
      Object endpointObj = args.get(i++);
      Map endpoint = null;
      String modelName;
      String modelVersion;
      String modelUrl;
      if(endpointObj instanceof Map) {
        endpoint = (Map)endpointObj;
        modelName = endpoint.get("name") + "";
        modelVersion = endpoint.get("version") + "";
        modelUrl = endpoint.get("url") + "";
      }
      else {
        return null;
      }
      String modelFunction = "apply";
      Map<String, String> modelArgs = new HashMap<>();
      if(args.get(i) instanceof String) {
        String func = (String)args.get(i);
        if(endpoint.containsKey("endpoint:" + func)) {
          modelFunction = "" + endpoint.get("endpoint:" + func);
        }
        else {
          modelFunction = func;
        }
        i++;
      }

      if(args.get(i) instanceof Map) {
        if(endpoint.containsKey("endpoint:apply")) {
          modelFunction = "" + endpoint.get("endpoint:apply");
        }
        modelArgs = (Map)args.get(i);
      }
      if( modelName == null
       || modelVersion == null
       || modelFunction == null
        ) {
        return null;
      }
      ModelCacheKey cacheKey = new ModelCacheKey(modelName, modelVersion, modelFunction, modelArgs);
      Map<String, Object> ret = resultCache.getIfPresent(cacheKey);
      if(ret != null) {
        return ret;
      }
      else {
        String url = modelUrl;
        if (url.endsWith("/")) {
          url = url.substring(0, url.length() - 1);
        }
        if (modelFunction.startsWith("/")) {
          modelFunction = modelFunction.substring(1);
        }
        try {
          URL u = new URL(url + "/" + modelFunction);

          String results = RESTUtil.INSTANCE.getRESTJSONResults(u, modelArgs);
          ret = JSONUtils.INSTANCE.load(results, new TypeReference<Map<String, Object>>() {
          });
          resultCache.put(cacheKey, ret);
          return ret;
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
          if (discoverer != null) {
            try {
              URL u = new URL(modelUrl);
              discoverer.blacklist(u);
            } catch (MalformedURLException e1) {
            }
          }
        }
      }
      return null;
    }

    @Override
    public void initialize(Context context) {
      try {
        Optional<ServiceDiscoverer> discovererOpt = (Optional) (context.getCapability(Context.Capabilities.SERVICE_DISCOVERER));
        if (discovererOpt.isPresent()) {
          discoverer = discovererOpt.get();
        }
      }
      catch(Exception ex) {
        LOG.error(ex.getMessage(), ex);
      }
    }
  }

  public static class GetEndpoint implements StellarFunction {
    ServiceDiscoverer discoverer;
    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      if(discoverer == null) {
        throw new ParseException("Unable to find ServiceDiscoverer service...");
      }
      String modelName = null;
      String modelVersion = null;
      if(args.size() >= 1) {
        modelName = args.get(0).toString();
      }
      if(args.size() >= 2)
      {
        modelVersion = args.get(1).toString();
      }
      if(modelName == null) {
        return null;
      }
      try {
        ModelEndpoint ep = null;
        if (modelVersion == null) {
          ep = discoverer.getEndpoint(modelName);
        } else {
          ep = discoverer.getEndpoint(modelName, modelVersion);
        }
        return ep == null ? null : endpointToMap(ep.getName(), ep.getVersion(), ep.getEndpoint());
      }
      catch(Exception ex) {
        LOG.error("Unable to discover endpoint: " + ex.getMessage(), ex);
        return null;
      }
    }
    public static Map<String, String> endpointToMap(String name, String version, Endpoint ep) {
      Map<String, String> ret = new HashMap<>();
      ret.put("url", ep.getUrl());
      ret.put("name", name);
      ret.put("version", version);
      for(Map.Entry<String, String> kv : ep.getEndpoints().entrySet()) {
        ret.put("endpoint:" + kv.getKey(), kv.getValue());
      }
      return ret;
    }
    @Override
    public void initialize(Context context) {
      Optional<Object> clientOptional = context.getCapability(Context.Capabilities.ZOOKEEPER_CLIENT);
      CuratorFramework client = null;
      if(clientOptional.isPresent() && clientOptional.get() instanceof CuratorFramework) {
        client = (CuratorFramework)clientOptional.get();
      }
      else {
        return;
      }
      try {
        MaaSConfig config = ConfigUtil.INSTANCE.read(client, "/metron/maas/config", new MaaSConfig(), MaaSConfig.class);
        discoverer = new ServiceDiscoverer(client, config.getServiceRoot());
        discoverer.start();
        context.addCapability(Context.Capabilities.SERVICE_DISCOVERER, () -> discoverer);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        return;
      }
    }
  }
}
