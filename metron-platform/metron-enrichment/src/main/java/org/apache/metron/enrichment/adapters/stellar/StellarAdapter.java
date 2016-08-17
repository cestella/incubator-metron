package org.apache.metron.enrichment.adapters.stellar;

import org.apache.metron.common.configuration.enrichment.handler.ConfigHandler;
import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.dsl.VariableResolver;
import org.apache.metron.common.stellar.StellarProcessor;
import org.apache.metron.enrichment.bolt.CacheKey;
import org.apache.metron.enrichment.interfaces.EnrichmentAdapter;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

public class StellarAdapter implements EnrichmentAdapter<CacheKey>,Serializable {

  private enum EnrichmentType implements Function<CacheKey, ConfigHandler>{
    ENRICHMENT(value -> value.getConfig().getEnrichment().getEnrichmentConfigs().get("stellar"))
    ,THREAT_INTEL(value -> value.getConfig().getThreatIntel().getEnrichmentConfigs().get("stellar"))
    ;
    Function<CacheKey, ConfigHandler> func;
    EnrichmentType(Function<CacheKey, ConfigHandler> func) {
      this.func = func;
    }

    @Override
    public ConfigHandler apply(CacheKey cacheKey) {
      return func.apply(cacheKey);
    }
  }
  transient Function<CacheKey, ConfigHandler> getHandler;
  private String enrichmentType;
  public StellarAdapter ofType(String enrichmentType) {
    this.enrichmentType = enrichmentType;
    return this;
  }

  @Override
  public void logAccess(CacheKey value) {

  }

  @Override
  public JSONObject enrich(CacheKey value) {
    ConfigHandler handler = getHandler.apply(value);
    Map<String, Object> globalConfig = value.getConfig().getConfiguration();
    Map<String, Object> sensorConfig = value.getConfig().getEnrichment().getConfig();
    if(handler == null) {
      return new JSONObject();
    }
    Map<String, Object> message = value.getValue(Map.class);
    VariableResolver resolver = new MapVariableResolver(message, sensorConfig, globalConfig);
    StellarProcessor processor = new StellarProcessor();
    for(Map.Entry<String, Object> kv : handler.getConfig().entrySet()) {
      String stellarStatement = (String) kv.getValue();
      Object o = processor.parse(stellarStatement, resolver);
      message.put(kv.getKey(), o);
    }
    return new JSONObject(message);
  }

  @Override
  public boolean initializeAdapter() {
    getHandler = EnrichmentType.valueOf(enrichmentType);
    return true;
  }

  @Override
  public void cleanup() {

  }
}
