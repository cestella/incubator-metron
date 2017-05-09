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

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.common.configuration.enrichment.handler.ConfigHandler;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.dsl.StellarFunctions;
import org.apache.metron.common.dsl.VariableResolver;
import org.apache.metron.common.stellar.StellarProcessor;
import org.apache.metron.enrichment.bolt.CacheKey;
import org.apache.metron.enrichment.interfaces.EnrichmentAdapter;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static org.apache.metron.enrichment.bolt.GenericEnrichmentBolt.STELLAR_CONTEXT_CONF;

public class StellarAdapter implements EnrichmentAdapter<CacheKey>,Serializable {
  protected static final Logger _LOG = LoggerFactory.getLogger(StellarAdapter.class);

  private enum EnrichmentType implements Function<SensorEnrichmentConfig, ConfigHandler>{
    ENRICHMENT(config -> config.getEnrichment().getEnrichmentConfigs().get("stellar"))
    ,THREAT_INTEL(config -> config.getThreatIntel().getEnrichmentConfigs().get("stellar"))
    ;
    Function<SensorEnrichmentConfig, ConfigHandler> func;
    EnrichmentType(Function<SensorEnrichmentConfig, ConfigHandler> func) {
      this.func = func;
    }

    @Override
    public ConfigHandler apply(SensorEnrichmentConfig cacheKey) {
      return func.apply(cacheKey);
    }
  }
  transient Function<SensorEnrichmentConfig, ConfigHandler> getHandler;
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
  public String getStreamSubGroup(String enrichmentType, String field) {
    return field;
  }

  @Override
  public JSONObject enrich(CacheKey value) {
    Context stellarContext = (Context) value.getConfig().getConfiguration().get(STELLAR_CONTEXT_CONF);
    ConfigHandler handler = getHandler.apply(value.getConfig());
    Map<String, Object> globalConfig = value.getConfig().getConfiguration();
    Map<String, Object> sensorConfig = value.getConfig().getEnrichment().getConfig();
    if(handler == null) {
        _LOG.trace("Stellar ConfigHandler is null.");
      return new JSONObject();
    }
    Map<String, Object> message = value.getValue(Map.class);
    VariableResolver resolver = new MapVariableResolver(message, sensorConfig, globalConfig);
    StellarProcessor processor = new StellarProcessor();
    Collection<Map.Entry<String, Object>> stellarStatements = getStatements(value.getField().length() == 0? handler.getConfig()
                                                                          : handler.getConfig().get(value.getField()));
    if(stellarStatements != null) {
      for (Map.Entry<String, Object> kv : stellarStatements) {
        if(kv.getValue() instanceof String) {
          long startTime = System.currentTimeMillis();
          String stellarStatement = (String) kv.getValue();
          if(_LOG.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - startTime;
            if(duration > 1000) {
              _LOG.debug("SLOW LOG: " + stellarStatement + " took" + duration + "ms");
            }
          }
          Object o = processor.parse(stellarStatement, resolver, StellarFunctions.FUNCTION_RESOLVER(), stellarContext);
          if(o != null && o instanceof Map) {
            for(Map.Entry<Object, Object> valueKv : ((Map<Object, Object>)o).entrySet()) {
              String newKey = ((kv.getKey().length() > 0)?kv.getKey() + "." : "" )+ valueKv.getKey();
              message.put(newKey, valueKv.getValue());
            }
          }
          else {
            message.put(kv.getKey(), o);
          }
        }
      }
    }
    JSONObject enriched = new JSONObject(message);
    _LOG.trace("Stellar Enrichment Success: " + enriched);
    return enriched;
  }

  Collection<Map.Entry<String, Object>> getStatements(Object o) {
    if(o instanceof Map) {
      return ((Map) o).entrySet();
    }
    else if(o instanceof List) {
      List<Map.Entry<String, Object>> ret = new ArrayList<>();
      List<String> entries = (List<String>)o;
      for(String entry : entries) {
        Iterable<String> parts = Splitter.on(":=").split(entry);
        String var = Iterables.getFirst(parts, null);
        String statement = Iterables.getLast(parts, null);
        if(var != null && statement != null) {
          ret.add(new AbstractMap.SimpleEntry<>(var.trim(), statement.trim()));
        }
      }
      return ret;
    }
    else {
      throw new IllegalStateException("Stellar statements must either be a map or a list.");
    }
  }

  @Override
  public boolean initializeAdapter(Map<String, Object> config) {
    getHandler = EnrichmentType.valueOf(enrichmentType);
    return true;
  }

  @Override
  public void updateAdapter(Map<String, Object> config) {
  }

  @Override
  public void cleanup() {

  }
}
