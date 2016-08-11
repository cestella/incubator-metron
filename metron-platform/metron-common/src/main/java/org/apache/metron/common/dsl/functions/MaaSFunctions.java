package org.apache.metron.common.dsl.functions;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.curator.framework.CuratorFramework;
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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MaaSFunctions {
 protected static final Logger LOG = LoggerFactory.getLogger(MaaSFunctions.class);

  public static class ModelApply implements StellarFunction {

    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      if(args.size() < 2) {
        throw new ParseException("Unable to execute model_apply. " +
                                 "Expected arguments: endpoint URL," +
                                 " endpoint method, param key 1, param " +
                                 "value 1, ..., param key k, param value k");
      }
      int i = 0;
      String url = (String)args.get(i++);
      String method = (String)args.get(i++);
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
      }
      return null;
    }

    @Override
    public void initialize(Context context) {

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
      if (modelVersion == null) {
        ModelEndpoint ep =  discoverer.getEndpoint(modelName);
        return ep == null?null:ep.getUrl();
      } else {
        ModelEndpoint ep = discoverer.getEndpoint(modelName, modelVersion);
        return ep == null?null:ep.getUrl();
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
