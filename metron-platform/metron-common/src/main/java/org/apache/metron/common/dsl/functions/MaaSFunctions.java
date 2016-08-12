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

  public static class ModelApply implements StellarFunction {
    private ServiceDiscoverer discoverer;
    private Cache<URL, Boolean> resultCache;
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
                                 "Expected arguments: endpoint URL," +
                                 " endpoint method, param key 1, param " +
                                 "value 1, ..., param key k, param value k");
      }
      int i = 0;
      String origUrl = (String)args.get(i++);
      String method = (String)args.get(i++);
      String url = origUrl;
      if(url.endsWith("/")) {
        url = url.substring(0, url.length() - 1);
      }
      if(method.startsWith("/")) {
        method = method.substring(1);
      }
      try {
        URL u = new URL(url + "/" + method);

        Map<String, String> params = new HashMap<>();
        for(;i < args.size();i += 2) {
          String key = "" + args.get(i);
          String value = "" + args.get(i+1);
          params.put(key, value);
        }
        String results = RESTUtil.INSTANCE.getRESTJSONResults(u, params);
        return JSONUtils.INSTANCE.load(results, new TypeReference<Map<String, Object>>(){});
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        if(discoverer != null) {
          try {
            URL u = new URL(origUrl);
            discoverer.blacklist(u);
          } catch (MalformedURLException e1) {
          }
        }
      }
      return null;
    }

    @Override
    public void initialize(Context context) {
      try {
        Optional<ServiceDiscoverer> discovererOpt = (Optional<ServiceDiscoverer>) context.getCapability(Context.Capabilities.SERVICE_DISCOVERER).get();
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
        if (modelVersion == null) {
          ModelEndpoint ep = discoverer.getEndpoint(modelName);
          return ep == null ? null : ep.getUrl();
        } else {
          ModelEndpoint ep = discoverer.getEndpoint(modelName, modelVersion);
          return ep == null ? null : ep.getUrl();
        }
      }
      catch(Exception ex) {
        LOG.error("Unable to discover endpoint: " + ex.getMessage(), ex);
        return null;
      }

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
