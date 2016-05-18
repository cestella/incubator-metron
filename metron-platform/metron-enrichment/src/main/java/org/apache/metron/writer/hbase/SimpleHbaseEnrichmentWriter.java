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

package org.apache.metron.writer.hbase;

import backtype.storm.tuple.Tuple;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.metron.common.configuration.Configurations;
import org.apache.metron.common.configuration.ParserConfigurations;
import org.apache.metron.common.configuration.writer.ParserWriterConfiguration;
import org.apache.metron.common.configuration.writer.WriterConfiguration;
import org.apache.metron.common.interfaces.BulkMessageWriter;
import org.apache.metron.common.utils.ConversionUtils;
import org.apache.metron.enrichment.converter.EnrichmentConverter;
import org.apache.metron.enrichment.converter.EnrichmentKey;
import org.apache.metron.enrichment.converter.EnrichmentValue;
import org.apache.metron.enrichment.converter.HbaseConverter;
import org.apache.metron.hbase.HTableProvider;
import org.json.simple.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleHbaseEnrichmentWriter implements BulkMessageWriter<JSONObject>, Serializable {
  public enum Configurations {
    HBASE_TABLE("shew.table")
    ,HBASE_CF("shew.cf")
    ,KEY_COLUMNS("shew.keyColumns")
    ,KEY_DELIM("shew.keyDelim")
    ,ENRICHMENT_TYPE("shew.enrichmentType")
    ;
    String key;
    Configurations(String key) {
      this.key = key;
    }
    public Object get(Map<String, Object> config) {
      return config.get(key);
    }
    public <T> T getAndConvert(Map<String, Object> config, Class<T> clazz) {
      Object o = get(config);
      if(o != null) {
        return ConversionUtils.convert(o, clazz);
      }
      return null;
    }
  }
  public static class KeyTransformer {
    List<String> keys = new ArrayList<>();
    Set<String> keySet;
    private String delim = ":";
    public KeyTransformer(String key) {
      this(key, null);
    }
    public KeyTransformer(String key, String delim) {
      keys.add(key);
      keySet = new HashSet<>(this.keys);
      this.delim = delim == null?this.delim:delim;
    }
    public KeyTransformer(Iterable<String> keys) {
      this(keys, null);
    }
    public KeyTransformer(Iterable<String> keys, String delim) {
      Iterables.addAll(this.keys, keys);
      keySet = new HashSet<>(this.keys);
      this.delim = delim == null?this.delim:delim;
    }

    public String transform(final JSONObject message) {
      return
      keys.stream().map( x -> {
        Object o = message.get(x);
        return o == null?"":o.toString();
      }).collect(Collectors.joining(delim));
    }
  }
  private EnrichmentConverter converter = new EnrichmentConverter();
  private String tableName;
  private String cf;
  private HTableInterface table;
  private HTableProvider provider;
  private Map.Entry<Object, KeyTransformer> keyTransformer;

  public SimpleHbaseEnrichmentWriter() {
  }

  @Override
  public void init(Map stormConf, WriterConfiguration configuration) throws Exception {
  }

  protected synchronized HTableProvider getProvider() {
    if(provider == null) {
      provider = new HTableProvider();
    }
    return provider;
  }

  public HTableInterface getTable(String tableName, String cf) throws IOException {
    synchronized(this) {
      boolean isInitial = this.tableName == null || this.cf == null;
      boolean isValid = tableName != null && cf != null;

      if( isInitial || (isValid && (!this.tableName.equals(tableName) || !this.cf.equals(cf)) )
        )
      {
        Configuration conf = HBaseConfiguration.create();
        //new table connection
        table = getProvider().getTable(conf, tableName);
        this.tableName = tableName;
        this.cf = cf;
      }
      return table;
    }
  }

  public HTableInterface getTable(Map<String, Object> config) throws IOException {
    return getTable(Configurations.HBASE_TABLE.getAndConvert(config, String.class)
                   ,Configurations.HBASE_CF.getAndConvert(config, String.class)
                   );

  }

  private List<String> getKeyColumns(Object keyColumnsObj) {
    Object o = keyColumnsObj;
    if(o instanceof String) {
      return ImmutableList.of(o.toString());
    }
    else if (o instanceof List) {
      List<String> keyCols = new ArrayList<>();
      for(Object key : (List)o) {
        keyCols.add(key.toString());
      }
      return keyCols;
    }
    else {
      throw new RuntimeException("Unable to configure KeyTransformer with columns: " + o);
    }
  }

  private KeyTransformer getTransformer(Map<String, Object> config) {
    Object o = Configurations.KEY_COLUMNS.get(config);
    KeyTransformer transformer = null;
    if(keyTransformer.getKey() == o) {
      return keyTransformer.getValue();
    }
    else {
      List<String> keys = getKeyColumns(o);
      Object delimObj = Configurations.KEY_DELIM.get(config);
      String delim = (delimObj == null || !(delimObj instanceof String))?null:delimObj.toString();
      transformer = new KeyTransformer(keys, delim);
      keyTransformer = new AbstractMap.SimpleEntry<>(o, transformer);
      return transformer;
    }
  }


  private EnrichmentValue getValue(JSONObject message, Set<String> keyColumns) {
    Map<String, Object> metadata = new HashMap<>();
    for(Object kv : message.entrySet()) {
      Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>)kv;
      if(!keyColumns.contains(entry.getKey())) {
        metadata.put(entry.getKey().toString(), entry.getValue());
      }
    }
    return new EnrichmentValue(metadata);
  }

  private EnrichmentKey getKey(JSONObject message, KeyTransformer transformer, String enrichmentType) {
    if(enrichmentType != null) {
      return new EnrichmentKey(enrichmentType, transformer.transform(message));
    }
    else {
      return null;
    }
  }

  @Override
  public void write( String sensorType
                    , WriterConfiguration configurations
                    , Iterable<Tuple> tuples
                    , List<JSONObject> messages
                    ) throws Exception
  {
    Map<String, Object> sensorConfig = configurations.getSensorConfig(sensorType);
    HTableInterface table = getTable(sensorConfig);
    KeyTransformer transformer = getTransformer(sensorConfig);
    Object enrichmentTypeObj = Configurations.ENRICHMENT_TYPE.get(sensorConfig);
    String enrichmentType = enrichmentTypeObj == null?null:enrichmentTypeObj.toString();
    List<Put> puts = new ArrayList<>();
    for(JSONObject message : messages) {
      EnrichmentKey key = getKey(message, transformer, enrichmentType);
      EnrichmentValue value = getValue(message, transformer.keySet);
      if(key == null || value == null) {
        continue;
      }
      Put put = converter.toPut(this.cf, key, value);
      if(put != null) {
        puts.add(put);
      }
    }
    table.put(puts);
  }

  @Override
  public void close() throws Exception {
    synchronized(this) {
      if(table != null) {
        table.close();
      }
    }
  }
}
