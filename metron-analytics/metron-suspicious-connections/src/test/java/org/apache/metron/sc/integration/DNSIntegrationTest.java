package org.apache.metron.sc.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.tdunning.math.stats.AVLTreeDigest;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.ClusterModel;
import org.apache.metron.sc.preprocessing.Preprocessor;
import org.apache.metron.sc.preprocessing.WordConfig;
import org.apache.metron.sc.training.TrainingCLI;
import org.apache.metron.sc.training.TrainingConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class DNSIntegrationTest {
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
    File chptDir = new File("checkpoint");
    chptDir.deleteOnExit();
    if(chptDir.exists()) {
      chptDir.delete();
    }
    else {
      chptDir.mkdirs();
    }
    //sc.setCheckpointDir(chptDir.getAbsolutePath());
  }

  @After
  public void tearDown() {
    sc.stop();
    sc = null;
  }
/**
   {
    "state" : {
          "frame_len_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), frame_len)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "timestamp_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), timestamp)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "subdomain_length_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), LENGTH( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ) )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "subdomain_entropy_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), STRING_ENTROPY( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ) )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "num_periods_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), LENGTH(SPLIT(dns_qry_name, '.')) - 1 )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   }
              }
    ,"specialWord" : "special_word"
    ,"words" : [ "ip_dst" ]
   }
   */
  @Multiline
  static String wordConfigOrig;


  /**
   {
    "state" : {
          "frame_len_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), frame_len)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "timestamp_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), timestamp)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "subdomain_length_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), LENGTH( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ) )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "subdomain_entropy_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), STRING_ENTROPY( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ) )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "num_periods_state" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), LENGTH(SPLIT(dns_qry_name, '.')) - 1 )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   }
              }
    ,"specialWord" : [  "JOIN( [",
                         "if BLOOM_EXISTS(TOP1M_GET(), DOMAIN_REMOVE_TLD(DOMAIN_REMOVE_SUBDOMAINS(dns_qry_name))) then '1' else '0',",
                         "STATS_BIN(frame_len_state, frame_len, 'DECILE'), " ,
                         "STATS_BIN(timestamp_state, timestamp, 'DECILE'), " ,
                         "STATS_BIN(subdomain_length_state, LENGTH( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ), 'QUINTILE'), " ,
                         "STATS_BIN(subdomain_entropy_state, STRING_ENTROPY( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ), 'QUINTILE'), " ,
                         "STATS_BIN(num_periods_state, LENGTH(SPLIT(dns_qry_name, '.')) - 1, 'QUINTILE'), " ,
                         "dns_qry_type_name, " ,
                         "dns_qry_rcode_name " ,
                       "], '_')"
                    ]
    ,"words" : [ "ip_dst" ]
   }
   */
  @Multiline
  static String wordConfig;

  /**
   {
    "k" : 100
   }
   */
  @Multiline
  static String trainingConfig;

  List<String> getMessages() throws IOException {
    File dnsJson = new File("src/test/resources/dns_data.json");
    List<String> ret = new ArrayList<>();
    try(BufferedReader br = new BufferedReader(new FileReader(dnsJson))) {
      for (String line = null; (line = br.readLine()) != null; ) {
        ret.add(line);
      }
      return ret;
    }
  }

  public Map<String, Map<String, Object>> createIndex(List<String> messages) throws IOException {
    Map<String, Map<String, Object>> ret = new HashMap<>();
    for (String line : messages) {
      Map<String, Object> message = JSONUtils.INSTANCE.load(line, new TypeReference<Map<String, Object>>() { });
      ret.put(getKey(message), message);
    }
    return ret;
  }

  public String getKey(Map<String, Object> message) {

    return message.get("ip_dst") + "_" + message.get("dns_qry_name");
  }

  @Test
  public void test() throws IOException {
    List<String> messages = getMessages();
    Map<String, Map<String, Object>> index = createIndex(messages);
    JavaRDD<String> messagesRdd = sc.parallelize(messages);
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(wordConfigOrig, WordConfig.class);
    TrainingConfig trainingConfigObj = JSONUtils.INSTANCE.load(trainingConfig, TrainingConfig.class);
    ClusterModel model = TrainingCLI.createModel(trainingConfigObj, wordConfigObj, messagesRdd, sc);
    List<Map<String, Object>> messagesWithScores = new ArrayList<>();
    for(String messageStr : messages) {
      Map<String, Object> message = JSONUtils.INSTANCE.load(messageStr, new TypeReference<Map<String, Object>>() { });
      String specialWord = model.computeSpecialWord(message);
      double score = model.score(message, (String)message.get("ip_dst"));
      message.put("score", score);
      message.put("special_word", specialWord);
      messagesWithScores.add(message);
    }
    Collections.sort(messagesWithScores, new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        double l = (double) o1.get("score");
        double r = (double) o2.get("score");
        if(l == r) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        }
        return Double.compare(l, r);
      }
    });
    DescriptiveStatistics stats = new DescriptiveStatistics();
    int idx = 0;
    for(Map<String, Object> message : messagesWithScores) {
      Map<String, Object> origMessage = index.get(getKey(message));
      if(origMessage != null) {
        int diffInRank = (Integer) origMessage.get("rank") - idx;
        String origSpecialWord = (String) origMessage.get("special_word");
        if(idx < 30) {
          stats.addValue(Math.abs(diffInRank));
        }
        message.put("diff_in_rank", diffInRank);
        message.put("orig_rank", origMessage.get("rank"));
        message.put("orig_special_word", origSpecialWord);
      }
      message.put("rank", idx);
      idx++;
    }
    File outFile = new File("src/test/resources/dns_data_scored.json");
    try(PrintWriter pw = new PrintWriter(outFile)) {
      for (Map<String, Object> msg : messagesWithScores) {
        String ip = (String) msg.get("ip_dst");
        String domain = (String) msg.get("dns_qry_name");
        String specialWord = (String) msg.get("special_word");
        String origSpecialWord = "" + msg.get("orig_special_word");
        String origRank = "" + msg.get("orig_rank");
        String rank = "" + msg.get("rank");
        String diffInRank= "" + msg.get("diff_in_rank");
        pw.println(Joiner.on(",").join(ip, domain, specialWord, origSpecialWord, rank, origRank, diffInRank));
      }
    }
    for(double p : ImmutableList.of(10.0, 25.0, 50.0, 75.0, 95.0, 99.0)) {
      System.out.println("Percentile: " + p + " => " + stats.getPercentile(p));
    }
  }

}
