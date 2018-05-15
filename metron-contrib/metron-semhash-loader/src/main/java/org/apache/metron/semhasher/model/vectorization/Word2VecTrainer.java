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
import org.apache.metron.semhash.vector.VectorizerModel;
import org.apache.metron.semhash.vector.word2vec.Word2VecModel;
import org.apache.metron.semhasher.config.Config;
import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhash.vector.word2vec.SentenceUtil;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Word2VecTrainer  implements VectorizerTrainer{
  public enum W2VParams {
    DIMENSION("dimension", Optional.of(100)),
    SEED("seed", Optional.of(0)),
    LEARNING_RATE("learningRate", Optional.of(0.025d)),
    ITERATIONS("iterations", Optional.of(1)),
    WINDOW_SIZE("windowSize", Optional.empty()),
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
        return o == null?null: ConversionUtils.convert(o, clazz);
      }
    }
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
      return SentenceUtil.INSTANCE.toSentence(message, context.get(), config.getSchema(), false);
    }
  }

  private Word2Vec configure(Map<String, Object> config) {
    Integer dimension = W2VParams.DIMENSION.get(config, Integer.class);
    Long seed = W2VParams.SEED.get(config, Long.class);
    Word2Vec ret = new Word2Vec().setVectorSize(dimension)
            .setSeed(seed)
            .setMinCount(W2VParams.MIN_COUNT.get(config, Integer.class))
            .setLearningRate(W2VParams.LEARNING_RATE.get(config, Double.class))
            .setNumIterations(W2VParams.ITERATIONS.get(config, Integer.class))
            ;
    Integer window = W2VParams.WINDOW_SIZE.get(config, Integer.class);
    if(window != null) {
      return ret.setWindowSize(window);
    }
    return ret;
  }
  @Override
  public VectorizerModel train(JavaSparkContext sc, Config config, Context context, JavaRDD<Map<String, Object>> messagesRdd) {
    JavaRDD<Iterable<String>> rdd = messagesRdd.map(new ToSentenceMapper(context, config)).cache();
    int sampleSize = W2VParams.SAMPLE_SIZE.get(config.getVectorizerConfig(), Integer.class);
    List<Iterable<String>> s = rdd.takeSample(false, sampleSize);
    Word2Vec w2v = configure(config.getVectorizerConfig());
    int dimension = W2VParams.DIMENSION.get(config.getVectorizerConfig(), Integer.class);

    org.apache.spark.mllib.feature.Word2VecModel m = w2v.fit(rdd);
    Map<String, Map.Entry<float[], Float>> model = new LinkedHashMap<>();
    scala.collection.immutable.Map<String, float[]> vecs = m.getVectors();
    List<Vector> vecList = new ArrayList<>();
    for(scala.collection.Iterator<String> it = vecs.keysIterator(); it.hasNext();) {
      String word = it.next();
      float[] vec = vecs.get(word).get();
      double[] tmpV = new double[vec.length];
      for(int i = 0;i < vec.length;++i) {
        tmpV[i] = vec[i];
      }
      vecList.add(new DenseVector(tmpV));
    }
    JavaRDD<Vector> vRdd = sc.parallelize(vecList);
    RowMatrix mat = new RowMatrix(vRdd.rdd());
    Matrix comp = mat.computePrincipalComponents(1);
    RowMatrix projection = mat.multiply(comp);
    Object projectionsO = projection.rows().collect();
    Vector[] projections = (Vector[]) projectionsO;
    int idx = 0;
    for(scala.collection.Iterator<String> it = vecs.keysIterator(); it.hasNext();idx++) {
      Vector v = projections[idx];
      String word = it.next();
      float[] vec = vecs.get(word).get();
      float rank = (float)v.apply(0);
      model.put(word, new AbstractMap.SimpleEntry<>(vec, rank));
    }
    Word2VecModel ret = new Word2VecModel(config.getSchema(), context, dimension, model);
    List<double[]> sample = new ArrayList<>();
    for(Iterable<String> sentence : s) {
      sample.add(ret.apply(sentence).getKey());
    }
    ret.setSample(sample);
    return ret;
  }
}
