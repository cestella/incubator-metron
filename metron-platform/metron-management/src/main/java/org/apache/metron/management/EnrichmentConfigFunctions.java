package org.apache.metron.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jakewharton.fliptables.FlipTable;
import org.apache.log4j.Logger;
import org.apache.metron.common.configuration.FieldTransformer;
import org.apache.metron.common.configuration.enrichment.EnrichmentConfig;
import org.apache.metron.common.configuration.enrichment.SensorEnrichmentConfig;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.ParseException;
import org.apache.metron.common.dsl.Stellar;
import org.apache.metron.common.dsl.StellarFunction;
import org.apache.metron.common.utils.ConversionUtils;
import org.apache.metron.common.utils.JSONUtils;

import java.util.*;

import static org.apache.metron.common.configuration.ConfigurationType.ENRICHMENT;

public class EnrichmentConfigFunctions {

  private static final Logger LOG = Logger.getLogger(ConfigurationFunctions.class);
  public enum Type {
    ENRICHMENT, THREAT_INTEL, THREATINTEL;
  }
  public static Map<String, Object> getStellarHandler(EnrichmentConfig enrichmentConfig) {
    Map<String, Object> fieldMap = enrichmentConfig.getFieldMap();
    Map<String, Object> stellarHandler = (Map<String, Object>) fieldMap.get("stellar");
    if(stellarHandler == null ) {
      stellarHandler = new HashMap<String, Object>();
      fieldMap.put("stellar", stellarHandler);
    }

    if(stellarHandler.get("config") == null){
      stellarHandler.put("config", new LinkedHashMap<String, Object>());
    }
    return stellarHandler;
  }

  public static EnrichmentConfig getConfig(SensorEnrichmentConfig sensorConfig, Type type) {
      EnrichmentConfig enrichmentConfig = null;
      switch(type) {
        case ENRICHMENT:
          enrichmentConfig = sensorConfig.getEnrichment();
          break;
        case THREAT_INTEL:
        case THREATINTEL:
          enrichmentConfig = sensorConfig.getThreatIntel();
      }
      return enrichmentConfig;
  }

  @Stellar(
           namespace = "ENRICHMENT_STELLAR_TRANSFORM"
          ,name = "PRINT"
          ,description = "Retrieve stellar enrichment transformations."
          ,params = {"sensorConfig - Sensor config to add transformation to."
                    ,"type - ENRICHMENT or THREAT_INTEL"
                    }
          ,returns = "The String representation of the transformations"
          )
  public static class GetStellarTransformation implements StellarFunction {

    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      String config = (String) args.get(0);
      SensorEnrichmentConfig configObj;
      if(config == null || config.isEmpty()) {
        configObj = new SensorEnrichmentConfig();
      }
      else {
        configObj = (SensorEnrichmentConfig) ENRICHMENT.deserialize(config);
      }
      Type type = Type.valueOf((String) args.get(1));
      EnrichmentConfig enrichmentConfig = getConfig(configObj, type);

      Map<String, Object> stellarHandler = getStellarHandler(enrichmentConfig);
      Map<String, Object> transforms = (Map<String, Object>) stellarHandler.get("config");
      String[] headers = new String[] { "Group", "Field", "Transformation"};
      List<String[]> objs = new ArrayList<>();
      for(Map.Entry<String, Object> kv : transforms.entrySet()) {
        if(kv.getValue() instanceof Map) {
          Map<String, String> groupMap = (Map<String, String>) kv.getValue();
          for(Map.Entry<String, String> groupKv : groupMap.entrySet()) {
            objs.add(new String[]{kv.getKey(), groupKv.getKey(), groupKv.getValue().toString()});
          }
        }
        else {
          objs.add(new String[]{"(default)", kv.getKey(), kv.getValue().toString()});
        }
      }
      String[][] data = new String[objs.size()][3];
      for(int i = 0;i < objs.size();++i) {
        data[i] = objs.get(i);
      }
      return FlipTable.of(headers, data);
    }

    @Override
    public void initialize(Context context) {

    }

    @Override
    public boolean isInitialized() {
      return true;
    }
  }

  @Stellar(
           namespace = "ENRICHMENT_STELLAR_TRANSFORM"
          ,name = "ADD"
          ,description = "Add stellar field transformation."
          ,params = {"sensorConfig - Sensor config to add transformation to."
                    ,"type - ENRICHMENT or THREAT_INTEL"
                    ,"stellarTransforms - A Map associating fields to stellar expressions"
                    ,"group - Group to add to (optional)"
                    }
          ,returns = "The String representation of the config in zookeeper"
          )
  public static class AddStellarTransformation implements StellarFunction{

    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      int i = 0;
      String config = (String) args.get(i++);
      SensorEnrichmentConfig configObj;
      if(config == null || config.isEmpty()) {
        configObj = new SensorEnrichmentConfig();
      }
      else {
        configObj = (SensorEnrichmentConfig) ENRICHMENT.deserialize(config);
      }
      Type type = Type.valueOf((String) args.get(i++));
      EnrichmentConfig enrichmentConfig = getConfig(configObj, type);

      Map<String, Object> stellarHandler = getStellarHandler(enrichmentConfig);

      Map<String, String> transformsToAdd = (Map<String, String>) args.get(i++);
      String group = null;
      if(i < args.size() ) {
        group = (String) args.get(i++);
      }
      Map<String, Object> baseTransforms = (Map<String, Object>) stellarHandler.get("config");
      Map<String, Object> groupMap = baseTransforms;
      if(group != null) {
        groupMap = (Map<String, Object>) baseTransforms.getOrDefault(group, new LinkedHashMap<>());
        baseTransforms.put(group, groupMap);
      }
      for(Map.Entry<String, String> kv : transformsToAdd.entrySet()) {
        groupMap.put(kv.getKey(), kv.getValue());
      }
      try {
        return JSONUtils.INSTANCE.toJSON(configObj, true);
      } catch (JsonProcessingException e) {
        LOG.error("Unable to convert object to JSON: " + configObj, e);
        return config;
      }
    }

    @Override
    public void initialize(Context context) {

    }

    @Override
    public boolean isInitialized() {
      return true;
    }
  }

  @Stellar(
           namespace = "ENRICHMENT_STELLAR_TRANSFORM"
          ,name = "REMOVE"
          ,description = "Add stellar field transformation."
          ,params = {"sensorConfig - Sensor config to add transformation to."
                    ,"type - ENRICHMENT or THREAT_INTEL"
                    ,"stellarTransforms - A list of removals"
                    ,"group - Group to remove from (optional)"
                    }
          ,returns = "The String representation of the config in zookeeper"
          )
  public static class RemoveStellarTransformation implements StellarFunction{

    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      int i = 0;
      String config = (String) args.get(i++);
      SensorEnrichmentConfig configObj;
      if(config == null || config.isEmpty()) {
        configObj = new SensorEnrichmentConfig();
      }
      else {
        configObj = (SensorEnrichmentConfig) ENRICHMENT.deserialize(config);
      }
      Type type = Type.valueOf((String) args.get(i++));
      EnrichmentConfig enrichmentConfig = getConfig(configObj, type);

      Map<String, Object> stellarHandler = getStellarHandler(enrichmentConfig);

      List<String> removals = (List<String>) args.get(i++);
      String group = null;
      if(i < args.size() ) {
        group = (String) args.get(i++);
      }
      Map<String, Object> baseTransforms = (Map<String, Object>) stellarHandler.get("config");
      Map<String, Object> groupMap = baseTransforms;
      if(group != null) {
        groupMap = (Map<String, Object>) baseTransforms.getOrDefault(group, new LinkedHashMap<>());
        baseTransforms.put(group, groupMap);
      }
      for(String remove : removals) {
        groupMap.remove(remove);
      }
      if(group != null && groupMap.isEmpty()) {
        baseTransforms.remove(group);
      }
      try {
        return JSONUtils.INSTANCE.toJSON(configObj, true);
      } catch (JsonProcessingException e) {
        LOG.error("Unable to convert object to JSON: " + configObj, e);
        return config;
      }
    }

    @Override
    public void initialize(Context context) {

    }

    @Override
    public boolean isInitialized() {
      return true;
    }
  }

  @Stellar(
           namespace = "ENRICHMENT"
          ,name = "SET_BATCH_SIZE"
          ,description = "Add stellar field transformation."
          ,params = {"sensorConfig - Sensor config to add transformation to."
                    ,"size - batch size (integer)"
                    }
          ,returns = "The String representation of the config in zookeeper"
          )
  public static class SetBatchSize implements StellarFunction{

    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      int i = 0;
      String config = (String) args.get(i++);
      SensorEnrichmentConfig configObj;
      if(config == null || config.isEmpty()) {
        configObj = new SensorEnrichmentConfig();
      }
      else {
        configObj = (SensorEnrichmentConfig) ENRICHMENT.deserialize(config);
      }
      int batchSize = 5;
      if(args.size() > 1) {
        batchSize = ConversionUtils.convert(args.get(i++), Integer.class);
      }
      configObj.setBatchSize(batchSize);
      try {
        return JSONUtils.INSTANCE.toJSON(configObj, true);
      } catch (JsonProcessingException e) {
        LOG.error("Unable to convert object to JSON: " + configObj, e);
        return config;
      }
    }

    @Override
    public void initialize(Context context) {

    }

    @Override
    public boolean isInitialized() {
      return true;
    }
  }
}
