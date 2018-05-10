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
package org.apache.metron.semhasher.model.vectorization;

import org.apache.metron.common.utils.SerDeUtils;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.semhasher.config.Config;
import org.apache.metron.semhasher.context.Context;
import org.apache.metron.semhasher.model.SentenceUtil;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.feature.Word2Vec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Word2VecModel implements VectorizerModel {

  public enum W2VParams {
    DIMENSION("dimension", Optional.of(100)),
    SEED("seed", Optional.of(0)),
    LEARNING_RATE("learningRate", Optional.of(0.025d)),
    ITERATIONS("iterations", Optional.of(1)),
    MIN_COUNT("minCount", Optional.of(5)),
    SAMPLE_SIZE("sampleSize", Optional.of(500)),
    ;
    String key;
    Optional<Object> defaultVal;
    W2VParams(String key, Optional<Object> defaultVal) {
      this.defaultVal = defaultVal;
      this.key = key;
    }

    public <T> T get(Map<String, Object> config, Class<T> clazz) {
      if(defaultVal.isPresent()) {
        return ConversionUtils.convert(config.getOrDefault(key, defaultVal.get()), clazz);
      }
      else {
        Object o = config.get(key);
        return o == null?null:ConversionUtils.convert(o, clazz);
      }
    }
  }

  private Map<String, float[]> model;
  private Config config;
  private Context context;
  private int dimension;
  private List<double[]> sample;

  public Word2VecModel() { }

  public void setDimension(int dimension) {
    this.dimension = dimension;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  private static class ToSentenceMapper implements org.apache.spark.api.java.function.Function<Map<String, Object>, Iterable<String>> {
    SerDeUtils.SerializationContainer<Context> context;
    Config config;
    public ToSentenceMapper(Context context, Config config) {
      this.context = new SerDeUtils.SerializationContainer<>(context, Context.class);
      this.config = config;
    }
    @Override
    public Iterable<String> call(Map<String, Object> message) throws Exception {
      return SentenceUtil.INSTANCE.toSentence(message, context.get(), config, false);
    }
  }

  @Override
  public void train(Map<String, Object> config, JavaRDD<Map<String, Object>> messagesRdd) {
    JavaRDD<Iterable<String>> rdd = messagesRdd.map(new ToSentenceMapper(this.context, this.config)).cache();
    int sampleSize = W2VParams.SAMPLE_SIZE.get(config, Integer.class);
    List<Iterable<String>> s = rdd.takeSample(false, sampleSize);
    Word2Vec w2v = configure(config);
    dimension = W2VParams.DIMENSION.get(config, Integer.class);

    org.apache.spark.mllib.feature.Word2VecModel m = w2v.fit(rdd);
    model = new HashMap<>();
    scala.collection.immutable.Map<String, float[]> vecs = m.getVectors();
    for(scala.collection.Iterator<String> it = vecs.keysIterator(); it.hasNext();) {
      String word = it.next();
      float[] vec = vecs.get(word).get();
      model.put(word, vec);
    }
    this.sample = new ArrayList<>();
    for(Iterable<String> sentence : s) {
      this.sample.add(apply(sentence));
    }
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public List<double[]> getSample() {
    return sample;
  }

  private Word2Vec configure(Map<String, Object> config) {
    Integer dimension = W2VParams.DIMENSION.get(config, Integer.class);
    Long seed = W2VParams.SEED.get(config, Long.class);
    return new Word2Vec().setVectorSize(dimension)
                         .setSeed(seed)
                         .setMinCount(W2VParams.MIN_COUNT.get(config, Integer.class))
                         .setLearningRate(W2VParams.LEARNING_RATE.get(config, Double.class))
                         .setNumIterations(W2VParams.ITERATIONS.get(config, Integer.class))
           ;
  }

  private double[] apply(Iterable<String> sentence) {
    double[] v = null;
    int n = 0;
    for(String word : sentence) {
      float[] wordV = model.get(word);
      if(wordV == null) {
        continue;
      }
      if(v == null) {
        v = new double[wordV.length];
        for(int i = 0;i < v.length;++i) {
          v[i] = wordV[i];
        }
      }
      else {
        for(int i = 0;i < v.length;++i) {
          v[i] += wordV[i];
        }
      }
      n++;
    }
    if(v == null) {
      return null;
    }
    for(int i = 0;i < v.length;++i) {
      v[i] /= n;
    }
    return v;
  }

  @Override
  public double[] apply(Map<String, Object> input) {
    Iterable<String> sentence = SentenceUtil.INSTANCE.toSentence(input, context, config, true);
    return apply(sentence);
  }

}
