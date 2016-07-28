package org.apache.metron.maas.service.yarn;

import org.apache.hadoop.yarn.api.records.Resource;

import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public enum Resources {

    MEMORY( resource -> resource.getMemory() )
  , V_CORE( resource -> resource.getVirtualCores())
  ;

  private Function<Resource, Integer> callback;
  Resources(Function<Resource, Integer> resourcesCallback) {
    this.callback = resourcesCallback;
  }
  public static EnumMap<Resources, Integer> getRealisticResourceRequest( EnumMap<Resources, Integer> requestedResources
                                                               , Resource resource
                                                               )
  {
    EnumMap<Resources, Integer> ret = new EnumMap<>(Resources.class);
    for(Resources r : values()) {
      Integer request = requestedResources.get(r);
      int resourceAmt = r.callback.apply(resource);
      if(request == null || request < 0) {
          ret.put(r, resourceAmt);
      }
      else {
        ret.put(r, Math.min(resourceAmt, request));
      }
    }
    return ret;
  }
  public Map.Entry<Resources, Integer> of(int n) {
    return new AbstractMap.SimpleEntry<Resources, Integer>(this, n);
  }
  public static EnumMap<Resources, Integer> toResourceMap( Map.Entry<Resources, Integer>... entry ) {
    EnumMap<Resources, Integer> ret = new EnumMap<>(Resources.class);
    for(Map.Entry<Resources, Integer> kv : entry) {
      ret.put(kv.getKey(), kv.getValue());
    }
    return ret;
  }
  public static Resource toResource(EnumMap<Resources, Integer> resourceMap) {
    return Resource.newInstance(resourceMap.get(Resources.MEMORY), resourceMap.get(Resources.V_CORE));
  }
}
