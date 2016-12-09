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

package org.apache.metron.sc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.sc.preprocessing.WordConfig;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.Vector;
import scala.collection.Iterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterModel implements Serializable {
  private List<Vector> ldaModel;
  private Map<String, Integer> word2Index;
  private Vector defaultTopicDistribution;
  private WordConfig wordConfig;
  private Map<String, Object> state;

  public ClusterModel(Map<String, Object> state, WordConfig wordConfig, String[] vocabulary, LDAModel ldaModel) {
    this.wordConfig = wordConfig;
    this.state = state;
    this.ldaModel = new ArrayList<>();
    for(Iterator<Vector> it = ldaModel.topicsMatrix().rowIter();it.hasNext();) {
      Vector v  = it.next();
      this.ldaModel.add(v);
    }
    word2Index = new HashMap<>();
    int index = 0;
    for(String word : vocabulary) {
      word2Index.put(word, index++);
    }
    double[] dist = new double[ldaModel.getK()];
    for(int i = 0;i < ldaModel.getK();++i) {
      dist[i] = 1.0/ldaModel.getK();
    }
    defaultTopicDistribution = new DenseVector(dist);
  }

  public Vector getTopicProbabilities(String word) {
    Integer index = word2Index.get(word);
    if(index == null) {
      return defaultTopicDistribution;
    }
    else {
      return ldaModel.get(index);
    }
  }

  public double score(Map<String, Object> message, String ip) {
    String specialWord = computeSpecialWord(message);
    return score(specialWord, ip);
  }

  public double score(String specialWord, String ip) {
    Vector wordProbabilities = getTopicProbabilities(specialWord);
    Vector ipProbabilities = getTopicProbabilities(ip);
    //inner product between the two vectors
    double ret = 0.0;
    for(int i = 0;i < wordProbabilities.size();++i) {
      ret += wordProbabilities.apply(i) * ipProbabilities.apply(i);
    }
    return ret;
  }

  public String computeSpecialWord(   Map<String, Object> message ) {
    return computeSpecialWord( message, state, wordConfig);
  }
  public static String computeSpecialWord( Map<String, Object> message
                                         , Map<String, Object> state
                                         , WordConfig wordConfig
                                         )
  {
    List<String> words = new ArrayList<>();
    words.add(wordConfig.getSpecialWord());
    words.addAll(wordConfig.getWords());
    return new WordTransformer(ImmutableList.of(wordConfig.getSpecialWord())).transform(state, message, Context.EMPTY_CONTEXT()).get(0);
  }
  public static ClusterModel load(File path) throws FileNotFoundException {
    return SerializationUtils.deserialize(new FileInputStream(path));
  }

  public void save(File path) throws FileNotFoundException {
    SerializationUtils.serialize(this, new FileOutputStream(path));
  }
}
