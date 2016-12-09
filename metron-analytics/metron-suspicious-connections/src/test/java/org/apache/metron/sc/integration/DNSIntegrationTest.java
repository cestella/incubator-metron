package org.apache.metron.sc.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tdunning.math.stats.AVLTreeDigest;
import org.adrianwalker.multilinestring.Multiline;
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
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
  }

  @After
  public void tearDown() {
    sc.stop();
    sc = null;
  }


  /**
   {
    "state" : {
          "frame_len" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), frame_len)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "timestamp" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), timestamp)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "subdomain_length" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), LENGTH( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ) )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "subdomain_entropy" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), STRING_ENTROPY( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ) )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   },
          "num_periods" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), LENGTH(SPLIT(dns_qry_name, '.')) - 1 )",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   }
              }
    "specialWord" : [  "JOIN( [",
                         "if BLOOM_EXISTS(TOP1M_GET(), DOMAIN_REMOVE_TLD(DOMAIN_REMOVE_SUBDOMAINS(dns_qry_name))) then '1' else '0',",
                         "STATS_BIN(frame_len_state, frame_len, 'DECILE'), " ,
                         "STATS_BIN(timestamp_state, timestamp, 'DECILE'), " ,
                         "STATS_BIN(subdomain_length_state, LENGTH( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ), 'QUINTILE'), " ,
                         "STATS_BIN(subdomain_entropy_state, STRING_ENTROPY( DOMAIN_EXTRACT_SUBDOMAINS(dns_qry_name) ), 'QUINTILE'), " ,
                         "STATS_BIN(num_periods_state, LENGTH(SPLIT(dns_qry_name, '.')) - 1, 'QUINTILE'), " ,
                         "dns_query_type_name, " ,
                         "dns_query_rcode_name " ,
                       "], '_')"
                    ],
    "words" : [
            "ip_src_addr"
           ,"ip_dst_addr"
           ,"STATS_BIN(size_state, size)"
              ]
   }
   */
  @Multiline
  static String wordConfig;
/*topDomain ,
        Quantiles.bin(frameLength.toDouble, frameLengthCuts) ,
        Quantiles.bin(unixTimeStamp.toDouble, timeCuts) ,
        Quantiles.bin(subdomainLength, subdomainLengthCuts) ,
        Quantiles.bin(subdomainEntropy, entropyCuts) ,
        Quantiles.bin(numberPeriods, numberPeriodsCuts) ,
        dnsQueryType ,
        dnsQueryRcode).mkString("_")*/
  /**
   {

   }
   */
  @Multiline
  static String trainingConfig;

  List<String> getMessages() throws IOException {
    File dnsJson = new File("dns_data.json");
    List<String> ret = new ArrayList<>();
    try(BufferedReader br = new BufferedReader(new FileReader(dnsJson))) {
      for (String line = null; (line = br.readLine()) != null; ) {
        ret.add(line);
      }
      return ret;
    }
  }

  @Test
  public void test() throws IOException {
    List<String> messages = getMessages();
    JavaRDD<String> messagesRdd = sc.parallelize(messages);
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(wordConfig, WordConfig.class);
    TrainingConfig trainingConfigObj = JSONUtils.INSTANCE.load(trainingConfig, TrainingConfig.class);
    ClusterModel model = TrainingCLI.createModel(trainingConfigObj, wordConfigObj, messagesRdd, sc);
    List<Map<String, Object>> messagesWithScores = new ArrayList<>();
    for(String messageStr : messages) {
      Map<String, Object> message = JSONUtils.INSTANCE.load(messageStr, new TypeReference<Map<String, Object>>() { });
      double score = model.score(message, (String)message.get("ip_dst"));
      message.put("score", score);
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
    for(int i = 0;i < 10;++i) {
      System.out.println(messagesWithScores.get(i));
    }
  }

}
