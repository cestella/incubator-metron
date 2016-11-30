/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.metron.sc.integration;

import com.tdunning.math.stats.AVLTreeDigest;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.preprocessing.Preprocessor;
import org.apache.metron.sc.preprocessing.WordConfig;
import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;

public class PreprocessingIntegrationTest implements Serializable {
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
          "size" : {
            "stateProjection" : "STATS_ADD(STATS_INIT(), size)",
            "stateUpdate" : "STATS_MERGE([left, right])"
                   }
              },
    "specialWord" : "JOIN([ip_src_addr, ip_dst_addr], ':')",
    "words" : [
            "ip_src_addr"
           ,"ip_dst_addr"
           ,"STATS_BIN(size_state, size)"
              ],
    "vocabSize" : 100,
    "k" : 0.1,
    "maxIter" : 100
   }
   */
  @Multiline
  static String config;

  static String[] ipAddrs = new String[] {
          "10.0.0.1",
          "10.0.0.2",
          "10.0.0.3",
          "10.0.0.4"
  };

  static int[] sizes = new int[] {
          10,
          15,
          20,
          25,
          30,
  };

  List<Map<String, Object>> getMessages() {
    Random r = new Random(0);
    List<Map<String, Object>> messages = new ArrayList<>();
    for(int i = 0;i < 100;++i) {

      String src = ipAddrs[r.nextInt(ipAddrs.length)];
      String dst = ipAddrs[r.nextInt(ipAddrs.length)];
      while(dst.equals(src)) {
        dst = ipAddrs[r.nextInt(ipAddrs.length)];
      }
      int size = sizes[r.nextInt(sizes.length)];
      Map<String, Object> message = new HashMap<>();
      message.put("size", size);
      message.put("ip_src_addr", src);
      message.put("ip_dst_addr", dst);
      messages.add(message);
    }
    return messages;
  }

  @Test
  public void testStateGathering() throws Exception {
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(config, WordConfig.class);
    JavaRDD<Map<String, Object>> messagesRdd = sc.parallelize(getMessages());
    Preprocessor preprocessor = new Preprocessor(sc);
    Map<String, Object> state = preprocessor.gatherState(wordConfigObj.getState(), messagesRdd);
    Assert.assertTrue(state.containsKey("size"));
    OnlineStatisticsProvider stats = (OnlineStatisticsProvider) state.get("size");
    Assert.assertFalse(Double.isNaN(stats.getPercentile(.5)));
  }

  @Test
  public void testWordCreation() throws Exception {
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(config, WordConfig.class);
    Preprocessor preprocessor = new Preprocessor(sc);
    List<Map<String, Object>> messages = getMessages();
    JavaRDD<Map<String, Object>> messagesRdd = sc.parallelize(messages);
    Map<String, Object> state = preprocessor.gatherState(wordConfigObj.getState(), messagesRdd);
    Dataset<Row> tokens = preprocessor.tokenize(state, wordConfigObj, messagesRdd);
    int i = 0;
    for(Row r : tokens.collectAsList()) {
      Assert.assertEquals(messages.get(i).get("ip_src_addr") + ":" + messages.get(i).get("ip_dst_addr"), r.getList(0).get(0));
      i++;
      Assert.assertTrue(Integer.parseInt(r.getList(0).get(3).toString()) >= 0);
    }
  }

  @Test
  public void testSpecialWordComputation() throws Exception {
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(config, WordConfig.class);
    List<Map<String, Object>> messages = getMessages();
    JavaRDD<Map<String, Object>> messagesRdd = sc.parallelize(messages);
    Preprocessor preprocessor = new Preprocessor(sc);
    Map<String, Object> state = preprocessor.gatherState(wordConfigObj.getState(), messagesRdd);
    String s = preprocessor.computeSpecialWord(wordConfigObj, state, messages.get(0));
    Assert.assertEquals(messages.get(0).get("ip_src_addr") + ":" + messages.get(0).get("ip_dst_addr"), s);
  }
}
