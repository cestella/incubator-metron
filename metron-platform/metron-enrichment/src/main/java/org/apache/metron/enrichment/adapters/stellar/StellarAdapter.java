/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public String getOutputPrefix(CacheKey value) {
		return "";
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
