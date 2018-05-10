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
package org.apache.metron.semhasher.model;

import com.google.common.collect.ImmutableList;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.io.IOUtils;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.common.utils.SerDeUtils;
import org.apache.metron.semhasher.SemanticVectorBinner;
import org.apache.metron.semhasher.config.Config;
import org.apache.metron.semhasher.load.LoadUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

public class Word2VecIntegrationTest {
  protected transient JavaSparkContext sc;
  protected transient SQLContext sqlContext;

  public static final String basePath = "/Users/cstella/Documents/workspace/metron/demo_2018/lanl";
  public static final String flows = basePath + "/flows_supersubset.txt";
  public static String[] expectedFar= new String[] {
          "113344,3,C16563,N13340,C1685,N5,6,7,2132",
          "113344,3,C1685,N5,C16563,N13340,6,6,3245",
          "113345,0,C4222,389,C16563,N14280,17,1,200",
          "113347,0,C16563,N13324,C1685,N5,6,5,1100",
          "113347,0,C16563,N13326,C1685,N5,6,5,1100",
          "113347,0,C16563,N13329,C1685,N5,6,5,1100",
          "113347,0,C1685,N5,C16563,N13324,6,4,582",
          "113347,0,C1685,N5,C16563,N13326,6,4,588",
          "113347,0,C1685,N5,C16563,N13329,6,4,585",
          "113374,0,C16563,N13653,C1685,N5,6,5,856"
  };

  public static String[] expectedkindaclose = new String[] {
          "734601,1,C17693,N25746,C11178,445,6,8,1168",
          "734711,1,C17693,N4824,C11178,445,6,8,1168",
          "769045,1,C17693,N93,C11178,N4,1,2,168",
          "769082,3,C17693,N26703,C11178,445,6,76,25801",
          "769084,1,C17693,443,C11178,N8263,6,524,776049",
          "769085,0,C17693,443,C11178,N1288,6,6,595",
          "769085,0,C17693,443,C11178,N1289,6,6,675",
          "769085,0,C17693,443,C11178,N1291,6,6,595",
          "769085,0,C17693,443,C11178,N1298,6,6,595",
          "769085,0,C17693,443,C11178,N1299,6,6,595",
          "769085,0,C17693,443,C11178,N1300,6,6,595",
          "769085,0,C17693,443,C11178,N1309,6,6,595",
          "769085,0,C17693,443,C11178,N1323,6,6,595",
          "769085,0,C17693,443,C11178,N13622,6,6,675",
          "769085,0,C17693,443,C11178,N14128,6,6,595",
          "769085,0,C17693,443,C11178,N14130,6,6,595",
          "769085,0,C17693,443,C11178,N19687,6,6,675",
          "769085,0,C17693,443,C11178,N21663,6,6,595",
          "769085,0,C17693,443,C11178,N21950,6,6,595",
          "769085,0,C17693,443,C11178,N21951,6,6,595"
  };

  public static String[] expectedClose = new String[] {
          "1178532,2,C17693,N17960,C9006,445,6,69,25472",
          "1178534,0,C17693,N302,C9006,N20508,6,525,776031",
          "1178534,0,C17693,N302,C9006,N20522,6,6,595",
          "1178534,1,C17693,N302,C9006,N18162,6,112,161152",
          "1178535,0,C17693,N302,C9006,N10187,6,6,595",
          "1178535,0,C17693,N302,C9006,N11062,6,6,595",
          "1178535,0,C17693,N302,C9006,N11102,6,6,675",
          "1178535,0,C17693,N302,C9006,N11130,6,7,641",
          "1178535,0,C17693,N302,C9006,N11940,6,6,595",
          "1178535,0,C17693,N302,C9006,N12593,6,6,595",
          "1178535,0,C17693,N302,C9006,N12621,6,7,641",
          "1178535,0,C17693,N302,C9006,N12622,6,6,595",
          "1178535,0,C17693,N302,C9006,N12629,6,6,595",
          "1178535,0,C17693,N302,C9006,N12636,6,38,50061",
          "1178535,0,C17693,N302,C9006,N12637,6,6,595",
          "1178535,0,C17693,N302,C9006,N12638,6,7,641",
          "1178535,0,C17693,N302,C9006,N12639,6,6,595",
          "1178535,0,C17693,N302,C9006,N12640,6,6,595",
          "1178535,0,C17693,N302,C9006,N1336,6,6,595",
          "1178535,0,C17693,N302,C9006,N13406,6,6,595",
          "1178535,0,C17693,N302,C9006,N14084,6,6,595",
          "1178535,0,C17693,N302,C9006,N15932,6,6,595",
          "1178535,0,C17693,N302,C9006,N1677,6,7,641",
          "1178535,0,C17693,N302,C9006,N17045,6,6,595",
          "1178535,0,C17693,N302,C9006,N1725,6,6,595",
          "1178535,0,C17693,N302,C9006,N18157,6,8,687",
          "1178535,0,C17693,N302,C9006,N18158,6,6,595",
          "1178535,0,C17693,N302,C9006,N18201,6,6,675",
          "1178535,0,C17693,N302,C9006,N18382,6,6,595",
          "1178535,0,C17693,N302,C9006,N18614,6,6,595",
          "1178535,0,C17693,N302,C9006,N20512,6,6,595",
          "1178535,0,C17693,N302,C9006,N20514,6,6,595",
          "1178535,0,C17693,N302,C9006,N20516,6,6,595",
          "1178535,0,C17693,N302,C9006,N20517,6,6,595",
          "1178535,0,C17693,N302,C9006,N20524,6,6,595",
          "1178535,0,C17693,N302,C9006,N20525,6,6,595",
          "1178535,0,C17693,N302,C9006,N20687,6,6,595",
          "1178535,0,C17693,N302,C9006,N20688,6,6,595",
          "1178535,0,C17693,N302,C9006,N20694,6,6,675",
          "1178535,0,C17693,N302,C9006,N2298,6,6,691",
          "1178535,0,C17693,N302,C9006,N2533,6,6,595",
          "1178535,0,C17693,N302,C9006,N4188,6,7,641",
          "1178535,0,C17693,N302,C9006,N4712,6,6,595",
          "1178535,0,C17693,N302,C9006,N5581,6,6,595",
          "1178535,0,C17693,N302,C9006,N6239,6,6,595",
          "1178535,0,C17693,N302,C9006,N759,6,6,595",
          "1178536,0,C17693,N302,C9006,N13024,6,6,595",
          "1178536,0,C17693,N302,C9006,N6374,6,6,595",
          "1178537,0,C17693,N302,C9006,N19972,6,6,595",
          "1178538,0,C17693,N302,C9006,N5418,6,6,595",
          "1178539,0,C17693,N302,C9006,N6275,6,6,595",
          "1178540,0,C17693,N302,C9006,N13098,6,6,595",
          "1178540,0,C17693,N302,C9006,N13339,6,6,595",
          "1178540,0,C17693,N302,C9006,N13364,6,6,595",
          "1178540,0,C17693,N302,C9006,N20535,6,6,595",
          "1178540,0,C17693,N302,C9006,N5246,6,6,595",
          "1178540,0,C17693,N302,C9006,N5652,6,6,595",
          "1178540,0,C17693,N302,C9006,N864,6,6,675",
          "1178541,0,C17693,N302,C9006,N13353,6,6,595",
          "1178541,0,C17693,N302,C9006,N14389,6,6,595",
          "1178541,0,C17693,N302,C9006,N14395,6,6,595",
          "1178541,0,C17693,N302,C9006,N20526,6,6,595",
          "1178541,0,C17693,N302,C9006,N6763,6,6,595",
          "1178541,0,C17693,N302,C9006,N7459,6,6,595",
          "1178542,0,C17693,N302,C9006,N7747,6,6,595",
          "1178543,0,C17693,N302,C9006,N10660,6,6,595",
          "1178543,0,C17693,N302,C9006,N12047,6,6,595",
          "1178543,0,C17693,N302,C9006,N14809,6,6,595",
          "1178543,0,C17693,N302,C9006,N14810,6,6,595",
          "1178543,0,C17693,N302,C9006,N14905,6,6,595",
          "1178543,0,C17693,N302,C9006,N4332,6,6,595",
          "1178543,0,C17693,N302,C9006,N5226,6,6,595",
          "1178543,0,C17693,N302,C9006,N6287,6,6,595",
          "1178543,0,C17693,N302,C9006,N6292,6,6,675",
          "1178543,0,C17693,N302,C9006,N6293,6,7,641",
          "1178543,0,C17693,N302,C9006,N8397,6,6,595",
          "1178544,0,C17693,N302,C9006,N3704,6,6,595",
          "1178544,0,C17693,N302,C9006,N8058,6,6,595",
          "1178545,0,C17693,N302,C9006,N8065,6,6,595",
          "1178546,0,C17693,N302,C9006,N8555,6,6,595",
          "1178547,0,C17693,N302,C9006,N5272,6,6,595",
          "1178548,0,C17693,N302,C9006,N8556,6,6,595",
          "1178550,0,C17693,N302,C9006,N8552,6,6,595",
          "1178551,0,C17693,N302,C9006,N15158,6,6,595",
          "1178553,0,C17693,N302,C9006,N15140,6,6,595",
          "1178555,0,C17693,N302,C9006,N15145,6,6,595",
          "1178556,0,C17693,N302,C9006,N1310,6,6,595",
          "1178556,0,C17693,N302,C9006,N13401,6,6,595",
          "1178556,0,C17693,N302,C9006,N15157,6,6,595",
          "1178556,0,C17693,N302,C9006,N20538,6,6,675",
          "1178556,0,C17693,N302,C9006,N20725,6,6,595",
          "1178556,0,C17693,N302,C9006,N6961,6,6,595",
          "1178556,0,C17693,N302,C9006,N7299,6,6,595",
          "1178557,0,C17693,N302,C9006,N10501,6,6,595",
          "1178557,0,C17693,N302,C9006,N12401,6,6,595",
          "1178557,0,C17693,N302,C9006,N12852,6,6,595",
          "1178557,0,C17693,N302,C9006,N13676,6,6,595",
          "1178557,0,C17693,N302,C9006,N13989,6,142,205253",
          "1178557,0,C17693,N302,C9006,N15247,6,6,755",
          "1178557,0,C17693,N302,C9006,N15251,6,6,595"
  };

  @Before
  public void setup() {
    SparkConf conf  = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.set("spark.kryoserializer.buffer.max", "500m");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
    sqlContext = new SQLContext(sc);
  }

  @After
  public void shutdown() {
    sc.stop();
  }

  /**
   * {
   *   "schema" : {
   *    "duration" : "NUMERIC",
   *    "source_computer" : "TEXT",
   *    "source_port" : "TEXT",
   *    "destination_computer" : "TEXT",
   *    "destination_port" : "TEXT",
   *    "protocol" : "TEXT",
   *    "packet_count" : "NUMERIC",
   *    "byte_count" : "NUMERIC"
   *              },
   *   "parser" : "org.apache.metron.parsers.csv.CSVParser",
   *   "parserConfig" : {
   *     "columns" : {
   *      "time" : 0,
   *      "duration" : 1,
   *      "source_computer" : 2,
   *      "source_port" : 3,
   *      "destination_computer" : 4,
   *      "destination_port" : 5,
   *      "protocol" : 6,
   *      "packet_count" : 7,
   *      "byte_count" : 8
   *      }
   *   },
   *   "vectorizerConfig" : {
   *     "dimension" : 200,
   *     "minCount" : 10,
   *     "sampleSize" : 500
   *   },
   *   "binningConfig" : {
   *      "stages" : [3, 25, 50],
   *      "buckets" : [50, 100],
   *      "distance" : 0.9,
   *      "error" : 0.0342
   *   }
   * }
   */
  @Multiline
  public static String configJson;

  @Test
  public void test() throws Exception {
    Config config = JSONUtils.INSTANCE.load(configJson, Config.class);
    config.initialize();
    JavaRDD<byte[]> trainingData = LoadUtil.INSTANCE.rawRead(sc, ImmutableList.of(flows), Optional.of(10));
    SemanticVectorBinner binner = SemanticVectorBinner.create(trainingData, config);
    try(OutputStream os = new FileOutputStream(new File("/tmp/binner.ser"))) {
      IOUtils.write(SerDeUtils.toBytes(binner), os);
    }

    try(OutputStream os = new FileOutputStream(new File("/tmp/vectorizer.ser"))) {
      IOUtils.write(SerDeUtils.toBytes(binner.getVectorizerModel()), os);
    }
    String bingo = "1179268,0,C17693,N302,C9006,N17478,6,6,595";
    Map<String, Object> binned = binner.apply(parse(bingo, config));
    System.out.println(binned);
  }

  private Map<String, Object> parse(String s, Config config) {
    return config.getParser().parse(s.getBytes()).get(0);
  }

}
