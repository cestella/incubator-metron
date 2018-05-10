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

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.stellar.common.utils.SerDeUtils;
import org.apache.metron.stellar.common.utils.StellarProcessorUtils;
import org.apache.metron.stellar.common.utils.hashing.semantic.SemanticHasher;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  @Test
  public void dump() throws Exception {
    Object hasher = null;
    try(FileInputStream fis = new FileInputStream(HASHER_LOC)) {
      hasher = SerDeUtils.fromBytes(IOUtils.toByteArray(fis), Object.class);
    }
    Map<String, Object> bingoMessage= JSONUtils.INSTANCE.load(bingo, JSONUtils.MAP_SUPPLIER);
    Map<String, Object> targetMap = (Map<String, Object>) bin(bingoMessage, hasher);
    double[] targetVector = (double[]) targetMap.get(SemanticHasher.VECTOR_KEY);
    Object targetBin = targetMap.get(SemanticHasher.HASH_KEY);
    try(BufferedReader br = new BufferedReader(new FileReader("/Volumes/Samsung_T1/data/lanl/reflow.json"))) {
      int numMatched = 0;
      int total = 0;
      int numFalsePositives = 0;
      List<String> nonFalsePositives = new ArrayList<>();
      for(String line = null; (line = br.readLine()) != null;) {
        Map<String, Object> message = JSONUtils.INSTANCE.load(line, JSONUtils.MAP_SUPPLIER);
        int timestamp = (int)message.get("timestamp");
        if(timestamp < 760000) {
          continue;
        }
        if(timestamp > 771000) {
          break;
        }
        total++;
        Map<String, Object> map = (Map<String, Object>) bin(message, hasher);
        Object bin = map.get(SemanticHasher.HASH_KEY);
        double[] vector = (double[]) map.get(SemanticHasher.VECTOR_KEY);
        if(bin != null && bin.equals(targetBin)) {
          //System.out.println(line);
          boolean isFalsePositive = Math.abs(new ArrayRealVector(vector).cosine(new ArrayRealVector(targetVector))) < 0.9;
          if(!isFalsePositive) {
            nonFalsePositives.add(line);
          }
          System.out.print(".");
          numMatched++;
          numFalsePositives += isFalsePositive?1:0;
          if(numMatched % 80 == 0) {
            System.out.println(" - " + numMatched + " / " + total + " = " + (100.0*numMatched)/total + "% at " + timestamp + " false positives: " + numFalsePositives);
          }
        }
      }
      System.out.println("Matched " + numMatched + " / " + total + " = " + (100.0*numMatched)/total);
      for(String l : nonFalsePositives) {
        System.out.println(l);
      }
    }
  }

  private Object bin(Map<String, Object> variables, Object hasher) throws IOException {
    variables.put("hasher", hasher);
    return StellarProcessorUtils.run("HASH( _, 'SEMHASH', { 'hasher' : hasher, 'return' : 'ALL' })", variables);
  }
}
