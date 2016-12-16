package org.apache.metron.sc.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tdunning.math.stats.AVLTreeDigest;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.ClusterModel;
import org.apache.metron.sc.preprocessing.WordConfig;
import org.apache.metron.sc.training.TrainingCLI;
import org.apache.metron.sc.training.TrainingConfig;
import org.apache.metron.statistics.StellarStatisticsFunctions;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.apache.metron.sc.integration.DNSIntegrationTest.*;

public class SpecialWordTest {

  private transient JavaSparkContext sc;

  @Before
  public void setUp() {
    SparkConf conf = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.registerKryoClasses(new Class<?>[] {AVLTreeDigest.class});
    conf.set("spark.kryo.classesToRegister", AVLTreeDigest.class.getName());
    conf.set("spark.executor.extraJavaOptions", "-Xss16m");
    conf.set("spark.driver.extraJavaOptions", "-Xss16m");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
    /*File chptDir = new File("checkpoint");
    chptDir.deleteOnExit();
    if(chptDir.exists()) {
      chptDir.delete();
    }
    else {
      chptDir.mkdirs();
    }*/
    //sc.setCheckpointDir(chptDir.getAbsolutePath());
  }


  @Test
  public void test() throws IOException {
    List<String> messages = getMessages();
    DescriptiveStatistics stats = getStats(messages, msg -> ((Number)msg.get("frame_len")).doubleValue());
    JavaRDD<String> messagesRdd = sc.parallelize(messages);
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(wordConfig, WordConfig.class);
    TrainingConfig trainingConfigObj = JSONUtils.INSTANCE.load(trainingConfig, TrainingConfig.class);
    ClusterModel model = TrainingCLI.createModel(trainingConfigObj, wordConfigObj, messagesRdd, sc);
    try(PrintWriter pw = new PrintWriter(new File("/tmp/vals.txt"))) {
      for(String message : messages) {
        Map<String, Object> msg = JSONUtils.INSTANCE.load(message, new TypeReference<Map<String, Object>>() { });
        pw.println(msg.get("frame_len"));
      }
    }
    for(Double p : StellarStatisticsFunctions.Bin.BinSplits.DECILE.split) {
      System.out.println(p + " => " + stats.getPercentile(p));
    }
    {
      Map<String, Object> msg = JSONUtils.INSTANCE.load(messages.get(0), new TypeReference<Map<String, Object>>() { });
      String computedSpecialWord = model.computeSpecialWord(msg);
      Assert.assertEquals(computedSpecialWord, msg.get("special_word"));
    }
  }

  DescriptiveStatistics getStats(List<String> messages, Function<Map<String, Object>, Double> picker) throws IOException {
    DescriptiveStatistics ret = new DescriptiveStatistics();
    for(String message : messages) {
      Map<String, Object> msg = JSONUtils.INSTANCE.load(message, new TypeReference<Map<String, Object>>() { });
      Number n = (Number) msg.get("timestamp");

      if(n == null || n.longValue() == 0) {
        System.out.println("nope.");
      }
      ret.addValue(picker.apply(msg));
    }
    return ret;
  }

  private double getPercentile(DescriptiveStatistics stats, double p) {
    int idx = (int)(p*stats.getN());
    return stats.getSortedValues()[idx];
  }

}
