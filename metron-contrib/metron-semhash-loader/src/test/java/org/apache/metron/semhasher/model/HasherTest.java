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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import info.debatty.java.lsh.LSHSuperBit;
import jodd.util.collection.SortedArrayList;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.semhash.SemanticHasher;
import org.apache.metron.semhash.bin.LSHBinner;
import org.apache.metron.semhash.vector.VectorizerModel;
import org.apache.metron.stellar.common.utils.SerDeUtils;
import org.apache.metron.stellar.common.utils.StellarProcessorUtils;
import org.apache.metron.stellar.common.utils.hashing.semantic.DelegatingSemanticHasher;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class HasherTest {
  private static final String HASHER_LOC = "/tmp/binner.ser";
  @Test
  public void test() throws IOException {
    try(FileInputStream fis = new FileInputStream(HASHER_LOC)) {
      Map<String, Object> variables = new HashMap<String, Object>() {{
        put("hasher", SerDeUtils.fromBytes(IOUtils.toByteArray(fis), Object.class));
        put("time", 1179268L);
        put("duration", new Integer(0));
        put("source_computer", "C17693");
        put("source_port", "N302");
        put("desination_computer", "C9006");
        put("desination_port", "N17478");
        put("protocol", new Integer(6));
        put("packet_count", new Integer(6));
        put("byte_count", new Integer(595));
      }};
      Object o = StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher })", variables);
      System.out.println(o);
    }
  }

  /**
   *{"destination_port":"N24219","source_computer":"C17693","byte_count":546,"duration":60,"breakin_seconds_since":115,"protocol":"6","destination_computer":"C12682","breakin_user":"U3635@DOM1","packet_count":6,"breakin_source_computer":"C17693","source_port":"N618","breakin_destination_computer":"C12682","breakin_timestamp":762568,"timestamp":762453}
   */
  @Multiline
  public static String bingo;

  /**
   * {"destination_port":"N24812","source_computer":"C17693","byte_count":595,"duration":0,"breakin_seconds_since":847,"protocol":"6","destination_computer":"C11178","breakin_user":"U9763@DOM1","packet_count":6,"breakin_source_computer":"C17693","source_port":"443","breakin_destination_computer":"C11178","breakin_timestamp":1526925461383,"timestamp":1526924614383,"is_potential_alert":true}
   */
  @Multiline
  public static String bingo_U9763;
  @Test
  public void dump() throws Exception {
    Object hasher = null;
    try(FileInputStream fis = new FileInputStream(HASHER_LOC)) {
      //hasher = SerDeUtils.fromBytes(IOUtils.toByteArray(fis), Object.class);
    }
    /*Map<String, Object> bingoMessage= JSONUtils.INSTANCE.load(bingo, JSONUtils.MAP_SUPPLIER);
    Map<String, Object> targetMap = (Map<String, Object>) bin(bingoMessage, hasher);
    double[] targetVector = (double[]) targetMap.get(SemanticHasher.VECTOR_KEY);
    Object targetBin = targetMap.get(SemanticHasher.HASH_KEY);*/
    try(BufferedReader br = new BufferedReader(new FileReader("/Volumes/Samsung_T1/data/lanl/reflow.json"))) {
      int numMatched = 0;
      int total = 0;
      int numFalsePositives = 0;
      long offset = System.currentTimeMillis();
      List<String> nonFalsePositives = new ArrayList<>();
      try(PrintWriter pw = new PrintWriter(new File("/Users/cstella/Documents/workspace/metron/demo_2018/lanl/reflow.json"))) {
        pw.println(transformLine(bingo.trim(), offset));
        for (String line = null; (line = br.readLine()) != null; ) {
          Map<String, Object> message = JSONUtils.INSTANCE.load(line, JSONUtils.MAP_SUPPLIER);
          int timestamp = (int) message.get("timestamp");
          if (timestamp < 760000) {
            continue;
          }
          if (timestamp > 771000) {
            break;
          }
          String transformedLine = transformLine(line, offset);
          pw.println(transformedLine);
          total++;
          /*Map<String, Object> map = (Map<String, Object>) bin(message, hasher);
          Object bin = map.get(SemanticHasher.HASH_KEY);
          double[] vector = (double[]) map.get(SemanticHasher.VECTOR_KEY);
          if (bin != null && bin.equals(targetBin)) {
            //System.out.println(line);
            boolean isFalsePositive = Math.abs(new ArrayRealVector(vector).cosine(new ArrayRealVector(targetVector))) < 0.9;
            if (!isFalsePositive) {
              nonFalsePositives.add(line);
            }
            System.out.print(".");
            numMatched++;
            numFalsePositives += isFalsePositive ? 1 : 0;
            if (numMatched % 80 == 0) {
              System.out.println(" - " + numMatched + " / " + total + " = " + (100.0 * numMatched) / total + "% at " + timestamp + " false positives: " + numFalsePositives);
            }
          }*/
        }
      }
      System.out.println("Matched " + numMatched + " / " + total + " = " + (100.0*numMatched)/total);
      for(String l : nonFalsePositives) {
        System.out.println(l);
      }
    }
  }

  /**
   * {"destination_port":"N14060","source_computer":"C17693","byte_count":595,"duration":0,"breakin_seconds_since":583,"protocol":"6","destination_computer":"C1555","breakin_user":"U4448@DOM1","packet_count":6,"breakin_source_computer":"C17693","source_port":"N10002","breakin_destination_computer":"C1555","breakin_timestamp":1526926373383,"timestamp":1526925790383,"is_potential_alert":true}
   */
  @Multiline
  public static String bingo_U4448;
  @Test
  public void investigate() throws Exception {
    investigate(bingo);
    //investigate(bingo_U4448);
    //investigate(bingo_U9763);
  }
  public void investigate(String bingo) throws Exception {
    Object hasher = getHasher();
    /*try(FileInputStream fis = new FileInputStream(HASHER_LOC)) {
      hasher = SerDeUtils.fromBytes(IOUtils.toByteArray(fis), Object.class);
    }*/
    Map<String, Object> bingoMessage= JSONUtils.INSTANCE.load(bingo, JSONUtils.MAP_SUPPLIER);
    Map<String, Object> targetMap = (Map<String, Object>) bin(bingoMessage, hasher);
    double[] targetVector = (double[]) targetMap.get(DelegatingSemanticHasher.VECTOR_KEY);
    Object targetBin = targetMap.get(DelegatingSemanticHasher.HASH_KEY);
    //try(BufferedReader br = new BufferedReader(new FileReader("/Volumes/Samsung_T1/data/lanl/reflow.json"))) {
    try(BufferedReader br = new BufferedReader(new FileReader("/Users/cstella/Documents/workspace/metron/demo_2018/lanl/reflow.json"))) {
      int numMatched = 0;
      int total = 0;
      int numFalsePositives = 0;
      long offset = System.currentTimeMillis();
      List<Map.Entry<Double, String>> nonFalsePositives = new SortedArrayList<>(new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
          Map.Entry<Double, String> e1 = (Map.Entry<Double, String>) o1;
          Map.Entry<Double, String> e2 = (Map.Entry<Double, String>) o2;
          if(e1.getKey() == e2.getKey()) {
            return e1.getValue().compareTo(e2.getValue());
          }
          return -1*Double.compare(e1.getKey(), e2.getKey());
        }
      });
      for (String line = null; (line = br.readLine()) != null; ) {
        Map<String, Object> message = JSONUtils.INSTANCE.load(line, JSONUtils.MAP_SUPPLIER);
        long timestamp = ((Number) message.get("timestamp")).longValue();
        /*if (timestamp < 760000) {
          continue;
        }
        if (timestamp > 771000) {
          break;
        }*/
        total++;
        if(total % 100000 == 0) {
          System.out.println("Processed " + total);
        }
        Map<String, Object> map = (Map<String, Object>) bin(message, hasher);
        Object bin = map.get(DelegatingSemanticHasher.HASH_KEY);
        Object rank = map.get(DelegatingSemanticHasher.RANK_KEY);
        double[] vector = (double[]) map.get(DelegatingSemanticHasher.VECTOR_KEY);
        double dist = new ArrayRealVector(vector).cosine(new ArrayRealVector(targetVector));
        if (bin != null && bin.equals(targetBin)) {
        //if (dist > 0.95) {
          //System.out.println(line);
          boolean isFalsePositive = dist < 0.95;
          /*if (!isFalsePositive) {
            nonFalsePositives.add(line);
          }*/
          nonFalsePositives.add(new AbstractMap.SimpleEntry<>((double)rank, dist + " - " + line));
          System.out.print(".");
          numMatched++;
          numFalsePositives += isFalsePositive ? 1 : 0;
          if (numMatched % 80 == 0) {
            System.out.println(" - " + numMatched + " / " + total + " = " + (100.0 * numMatched) / total + "% at " + timestamp + " false positives: " + numFalsePositives);
          }
        }
        else if(dist > 0.96) {
          //numMatched++;
          //nonFalsePositives.add(line);
        }
      }

      System.out.println();
      for(Map.Entry<Double, String> l : nonFalsePositives) {
        System.out.println(l.getKey() + " => " + l.getValue());
      }
      System.out.println("Matched " + numMatched + " / " + total + " = " + (100.0*numMatched)/total);
    }
  }

  private String transformLine(String line, long offset) throws IOException {
    Map<String, Object> message = JSONUtils.INSTANCE.load(line, JSONUtils.MAP_SUPPLIER);
    int timestamp = (int) message.get("timestamp");
    message.put("timestamp", 1000*timestamp + offset);
    if(message.containsKey("breakin_timestamp"))
    {
      int breakin_timestamp = (int) message.get("breakin_timestamp");
      message.put("breakin_timestamp", 1000*breakin_timestamp + offset);
      message.put("is_potential_alert", true);
    }
    return new String(JSONUtils.INSTANCE.toJSON(message, false));
  }

  private Object bin(Map<String, Object> variables, Object hasher) throws IOException {
    variables.put("hasher", hasher);
    return StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'ALL', 'ignore' : [ 'destination_computer', 'source_computer'] })", variables);
    //return StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'ALL' })", variables);
  }

  private Object bin(Map<String, Object> variables, Object hasher, List<String> ignores) throws IOException {
    variables.put("hasher", hasher);
    variables.put("ignore", ignores);
//    return StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'ALL', 'ignore' : [ 'source_port', 'source_computer', 'destination_computer'] })", variables);
    return StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'VECTOR', 'ignore' : ignore , 'bucketsInHash' : 1})", variables);
  }

  private Map.Entry<Double, String> dist(Object hasher, List<String> ignores) throws Exception {

    Map<String, Object> bingoMessage1 = JSONUtils.INSTANCE.load(bingo, JSONUtils.MAP_SUPPLIER);
    //Map<String, Object> bingoMessage2 = JSONUtils.INSTANCE.load(bingo_U4448, JSONUtils.MAP_SUPPLIER);
    Map<String, Object> bingoMessage2 = JSONUtils.INSTANCE.load(bingo_U9763, JSONUtils.MAP_SUPPLIER);
    bingoMessage1.put("hasher", hasher);
    bingoMessage1.put("ignore", ignores);
    bingoMessage2.put("hasher", hasher);
    bingoMessage2.put("ignore", ignores);
    Map<String, Object> res1 = (Map<String, Object>) StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'ALL', 'ignore' : ignore })", bingoMessage1);
    Map<String, Object> res2 = (Map<String, Object>) StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'ALL', 'ignore' : ignore })", bingoMessage2);
    double[] vec1 = (double[]) res1.get(DelegatingSemanticHasher.VECTOR_KEY);
    String bin1 = (String) res1.get(DelegatingSemanticHasher.HASH_KEY);
    double[] vec2 = (double[]) res2.get(DelegatingSemanticHasher.VECTOR_KEY);
    String bin2 = (String) res2.get(DelegatingSemanticHasher.HASH_KEY);
    if(vec1 == null || vec2 == null ) {
      return null;
    }
    return new AbstractMap.SimpleEntry<>(new ArrayRealVector(vec1).cosine(new ArrayRealVector(vec2)), bin1 + " vs " + bin2);
  }

  @Test
  public void testGridsearch() throws Exception {
    Object hasher = null;
    try(FileInputStream fis = new FileInputStream(HASHER_LOC)) {
      hasher = SerDeUtils.fromBytes(IOUtils.toByteArray(fis), Object.class);
    }
    SemanticHasher h = (SemanticHasher)hasher;
    VectorizerModel vModel = h.getVectorizerModel();
    Optional<LSHSuperBit> binner = LSHBinner.gridsearch(ImmutableList.of(3 )
                        ,ImmutableList.of(10, 50, 100, 150, 200, 250, 300, 350, 400)
                        ,0.96
                        ,vModel.getSample()
                        ,0.03
                        ,1e-7
                        ,1
                        );
    Assert.assertTrue(binner.isPresent());
  }

  private static SemanticHasher getHasher() throws Exception {
    Object hasher = null;
    try(FileInputStream fis = new FileInputStream(HASHER_LOC)) {
      hasher = SerDeUtils.fromBytes(IOUtils.toByteArray(fis), Object.class);
    }
    SemanticHasher h = (SemanticHasher)hasher;
    VectorizerModel vModel = h.getVectorizerModel();
    LSHBinner binner = new LSHBinner(new LSHSuperBit(10, 10, vModel.getDimension(), 0L), 7);
    return new SemanticHasher(vModel, binner);
  }


  @Test
  public void testDistance() throws Exception {
    Object hasher = getHasher();
    List<String> fields = new ArrayList<String>() {{
      add("duration");
      add("source_computer");
      add("source_port");
      add("destination_computer");
      add("protocol");
      add("packet_count");
      add("byte_count");
    }};
    System.out.println(dist(hasher, new ArrayList<>()));
    TreeMap<Double, String> results = new TreeMap<>();
    //1
    for(int i = 0;i < fields.size();++i) {
      List<String> ignores = new ArrayList<>();
      ignores.add(fields.get(i));
      Map.Entry<Double, String> dist = dist(hasher, ignores );
      results.put(dist.getKey(), Joiner.on(",").join(ignores) + " " + dist.getValue());
    }
    //2
    for(int i = 0;i < fields.size();++i) {
      for(int j = i+1;j < fields.size();++j) {
        List<String> ignores = new ArrayList<>();
        ignores.add(fields.get(i));
        ignores.add(fields.get(j));
        Map.Entry<Double, String> dist = dist(hasher, ignores );
        if(dist != null) {
          results.put(dist.getKey(), Joiner.on(",").join(ignores) + " " + dist.getValue());
        }
      }
    }
    //3
    for(int i = 0;i < fields.size();++i) {
      for(int j = i+1;j < fields.size();++j) {
        for(int k = j+1;k < fields.size();++k) {
          List<String> ignores = new ArrayList<>();
          ignores.add(fields.get(i));
          ignores.add(fields.get(j));
          ignores.add(fields.get(k));Map.Entry<Double, String> dist = dist(hasher, ignores );
          if(dist != null) {
            results.put(dist.getKey(), Joiner.on(",").join(ignores) + " " + dist.getValue());
          }
        }
      }
    }
    //4
    for(int i = 0;i < fields.size();++i) {
      for(int j = i+1;j < fields.size();++j) {
        for(int k = j+1;k < fields.size();++k) {
          for(int l = k+1;l < fields.size();++l) {
            List<String> ignores = new ArrayList<>();
            ignores.add(fields.get(j));
            ignores.add(fields.get(k));
            ignores.add(fields.get(i));
            ignores.add(fields.get(l));Map.Entry<Double, String> dist = dist(hasher, ignores );
            if(dist != null) {
              results.put(dist.getKey(), Joiner.on(",").join(ignores) + " " + dist.getValue());
            }
          }
        }
      }
    }
    //5
    for(int i = 0;i < fields.size();++i) {
      for(int j = i+1;j < fields.size();++j) {
        for(int k = j+1;k < fields.size();++k) {
          for(int l = k+1;l < fields.size();++l) {
            for(int m = l+1;m < fields.size();++m) {
              List<String> ignores = new ArrayList<>();
              ignores.add(fields.get(j));
              ignores.add(fields.get(k));
              ignores.add(fields.get(i));
              ignores.add(fields.get(l));
              ignores.add(fields.get(m));Map.Entry<Double, String> dist = dist(hasher, ignores );
              if(dist != null) {
                results.put(dist.getKey(), Joiner.on(",").join(ignores) + " " + dist.getValue());
              }
            }
          }
        }
      }
    }
    //6
    for(int i = 0;i < fields.size();++i) {
      for(int j = i+1;j < fields.size();++j) {
        for(int k = j+1;k < fields.size();++k) {
          for(int l = k+1;l < fields.size();++l) {
            for(int m = l+1;m < fields.size();++m) {
              for(int n = m+1;n < fields.size();++n) {
                List<String> ignores = new ArrayList<>();
                ignores.add(fields.get(j));
                ignores.add(fields.get(k));
                ignores.add(fields.get(i));
                ignores.add(fields.get(l));
                ignores.add(fields.get(m));
                ignores.add(fields.get(n));Map.Entry<Double, String> dist = dist(hasher, ignores );
                if(dist == null) {
                  continue;
                }
        results.put(dist.getKey(), Joiner.on(",").join(ignores) + " " + dist.getValue());
              }
            }
          }
        }
      }
    }
    int z = 0;
    for(Map.Entry<Double, String> kv : results.descendingMap().entrySet()) {
      System.out.println(kv);
      if(z++ > 20) {
        break;
      }
    }
  }

  /**
   *
   {"duration":0,"protocol":"6","destination_computer":"C17640","destination_port":"N2405","packet_count":6,"source_port":"445","source_computer":"C25466","timestamp":1526925945383,"byte_count":889}
   */
  @Multiline
  public static String testMessage;

  @Test
  public void testBin() throws Exception {
    Object hasher = null;
    hasher = getHasher();
    double[] v1, v2;
    {
      Map<String, Object> message = JSONUtils.INSTANCE.load(bingo_U9763, JSONUtils.MAP_SUPPLIER);
      v1 = (double[]) ((Map<String, Object>)bin(message, hasher)).get("vector");
    }
    {
      Map<String, Object> message = JSONUtils.INSTANCE.load(testMessage, JSONUtils.MAP_SUPPLIER);
      v2 = (double[]) ((Map<String, Object>)bin(message, hasher)).get("vector");
    }

  }


}
