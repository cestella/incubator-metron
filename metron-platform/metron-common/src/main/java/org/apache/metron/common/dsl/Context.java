package org.apache.metron.common.dsl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Context {
  public interface Capability {
    Object get();
  }
  public enum Capabilities {
      HBASE_PROVIDER
    , ZOOKEEPER_CLIENT
    , SERVICE_DISCOVERER;

  }

  public static class Builder {
    private Map<String, Capability> capabilityMap = new HashMap<>();

    public Builder with(String s, Capability capability) {
      capabilityMap.put(s, capability);
      return this;
    }
    public Builder with(Enum<?> s, Capability capability) {
      capabilityMap.put(s.toString(), capability);
      return this;
    }
    public Context build() {

      return new Context(capabilityMap);
    }
  }
  public static Context EMPTY_CONTEXT() {
    return
    new Context(new HashMap<>()){
      @Override
      public Optional<Object> getCapability(String capability) {
        return Optional.empty();
      }
    };
  }
  private Map<String, Capability> capabilities;
  private Context( Map<String, Capability> capabilities
                 )
  {
    this.capabilities = capabilities;
  }

  public Optional<Object> getCapability(String capability) {
    Capability c = capabilities.get(capability);
    if(c == null) {
      throw new IllegalStateException("Unable to find capability " + capability + "; it may not be available in your context.");
    }
    return Optional.ofNullable(c.get());
  }

  public void addCapability(String s, Capability capability) {
    this.capabilities.put(s, capability);
  }

  public void addCapability(Enum<?> s, Capability capability) {
    this.capabilities.put(s.toString(), capability);
  }
}
