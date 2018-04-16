package org.apache.metron.webrequest;

import com.google.common.collect.ImmutableList;
import org.apache.metron.webrequest.classifier.DatasetUtil;
import org.apache.metron.webrequest.classifier.Evaluators;
import org.apache.metron.webrequest.classifier.PipelineConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Smoketest {
  protected transient JavaSparkContext sc;
  protected transient SQLContext sqlContext;

  @Before
  public void setup() {
    SparkConf conf  = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.set("spark.kryoserializer.buffer.max", "20");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
    sqlContext = new SQLContext(sc);
  }

  @After
  public void shutdown() {
    sc.stop();
  }

  @Test
  public void test() throws Exception {
    Map<String, List<String>> input = new HashMap<String, List<String>>() {{
      String base = "/Users/cstella/Documents/workspace/metron/fork/Fwaf-Machine-Learning-driven-Web-Application-Firewall";
      put("good", ImmutableList.of(base + "/goodqueries.txt"));
      put("bad", ImmutableList.of(base + "/badqueries.txt"));
    }};
    DataFrame allData = DatasetUtil.INSTANCE.mergeDatasets(sc, sqlContext, input);
    EnumMap<PipelineConfig, Optional<Object>> config = PipelineConfig.config(new HashMap<>());
    Pipeline pipeline = PipelineConfig.createPipeline(config, allData);
    DataFrame[] split = DatasetUtil.INSTANCE.split(allData, 0.7);
    PipelineModel model = PipelineConfig.train(split[0].cache(), pipeline);
    EnumMap<Evaluators, Object> evalResults = Evaluators.evaluatePipeline(split[1].cache(), model);
    for(Map.Entry<Evaluators, Object> kv : evalResults.entrySet()) {
      System.out.println(kv.getKey() + " => " + kv.getValue());
    }
  }
}
