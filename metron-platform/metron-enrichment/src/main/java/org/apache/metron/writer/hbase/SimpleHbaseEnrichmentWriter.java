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
import org.apache.metron.common.interfaces.BulkMessageWriter;
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
  public static final String HBASE_TABLE_CONF = "table";
  public static final String HBASE_CF_CONF = "cf";
  public static final String KEY_COLUMNS_CONF = "keyColumns";
  public static final String KEY_DELIM_CONF= "keyDelim";
  public static final String ENRICHMENT_TYPE_CONF = "enrichmentType";
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
  private String enrichmentType;

  public SimpleHbaseEnrichmentWriter() {
  }

  @Override
  public void init(Map stormConf, Configurations configuration) throws Exception {
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
    return getTable(config.get(HBASE_TABLE_CONF).toString()
                   ,config.get(HBASE_CF_CONF).toString()
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
    Object o = config.get(KEY_COLUMNS_CONF);
    KeyTransformer transformer = null;
    if(keyTransformer.getKey() == o) {
      return keyTransformer.getValue();
    }
    else {
      List<String> keys = getKeyColumns(o);
      Object delimObj = config.get(KEY_DELIM_CONF);
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
  public void write(String sensorType, Configurations configurations, List<Tuple> tuples, List<JSONObject> messages) throws Exception {
    HTableInterface table = getTable(configurations.getGlobalConfig());
    KeyTransformer transformer = getTransformer(configurations.getGlobalConfig());
    Object enrichmentTypeObj = configurations.getGlobalConfig().get(ENRICHMENT_TYPE_CONF);
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
