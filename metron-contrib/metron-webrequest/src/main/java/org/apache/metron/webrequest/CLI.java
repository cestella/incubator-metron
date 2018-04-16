package org.apache.metron.webrequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.metron.webrequest.classifier.DatasetUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CLI {
  public static class Config {
    private Map<String, List<String>> labeledInput;
    private String output;
    private Map<String, Object> pipelineConfig;

    public Map<String, List<String>> getLabeledInput() {
      return labeledInput;
    }

    public void setLabeledInput(Map<String, List<String>> labeledInput) {
      this.labeledInput = labeledInput;
    }

    public Map<String, Object> getPipelineConfig() {
      return pipelineConfig;
    }

    public void setPipelineConfig(Map<String, Object> pipelineConfig) {
      this.pipelineConfig = pipelineConfig;
    }

    public String getOutput() {
      return output;
    }

    public void setOutput(String output) {
      this.output = output;
    }
  }
  private static JavaSparkContext createContext(String name) {
    SparkConf conf = new SparkConf().setAppName(name);
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    return new JavaSparkContext(conf);
  }
  private static ThreadLocal<ObjectMapper> _mapper = ThreadLocal.withInitial(
          () -> new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
  );
  public static void main(String... argv) throws IOException {
    File configFile = new File(argv[0]);
    Config c = _mapper.get().readValue(configFile, Config.class);
    JavaSparkContext sc = createContext("webrequestclassifier");
    SQLContext sqlContext = new SQLContext(sc);
    DataFrame df = DatasetUtil.INSTANCE.mergeDatasets(sc, sqlContext, c.getLabeledInput());
  }
}
