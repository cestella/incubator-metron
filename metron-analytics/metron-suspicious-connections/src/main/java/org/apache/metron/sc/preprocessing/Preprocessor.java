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

package org.apache.metron.sc.preprocessing;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.dsl.functions.resolver.SingletonFunctionResolver;
import org.apache.metron.common.stellar.StellarProcessor;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.training.TrainingConfig;
import org.apache.metron.sc.WordTransformer;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.*;
import scala.Tuple2;

import java.util.*;

public class Preprocessor {
  JavaSparkContext sc;
  SQLContext sqlContext;
  public Preprocessor(JavaSparkContext sc) {
    this.sc = sc;
    sqlContext = new SQLContext(sc);
  }

  public static class KeyedState {
    private String key;
    private Object state;
    public KeyedState(String key, Object state) {
      this.key = key;
      this.state = state;
    }

  }

  public static class Projection implements PairFlatMapFunction<Map<String, Object>, String, KeyedState> {
    private Map<String, State> config;
    public Projection(Map<String, State> config) {
      this.config = config;
    }

    @Override
    public Iterator<Tuple2<String, KeyedState>> call(Map<String, Object> message) throws Exception {
      List<Tuple2<String, KeyedState>> ret = new ArrayList<>();
      MapVariableResolver resolver = new MapVariableResolver(message);
      StellarProcessor processor = new StellarProcessor();
      for(Map.Entry<String, State> kv : config.entrySet()) {
        Object o = processor.parse(kv.getValue().getStateProjection(), resolver, SingletonFunctionResolver.getInstance(), Context.EMPTY_CONTEXT());
        ret.add(new Tuple2<>(kv.getKey(), new KeyedState(kv.getKey(), o)));
      }
      return ret.iterator();
    }
  }

  public static class Reduction implements Function2<KeyedState, KeyedState, KeyedState> {
    private Map<String, State> config;
    public Reduction(Map<String, State> config) {
      this.config = config;
    }

    @Override
    public KeyedState call(KeyedState leftState, KeyedState rightState) throws Exception {
      Object left = leftState.state;
      Object right = rightState.state;
      KeyedState nullRet = new KeyedState(leftState.key, null);
      if(left == null && right == null) {
        return nullRet;
      }
      else if(left == null && right != null) {
        return rightState;
      }
      else if(left != null && right == null) {
        return leftState;
      }
      else {
        Map<String, Object> variables = new HashMap<>();
        variables.put("left", left);
        variables.put("right", right);
        MapVariableResolver resolver = new MapVariableResolver(variables);
        StellarProcessor processor = new StellarProcessor();
        State s = config.get(leftState.key);
        if(s == null) {
          return nullRet;
        }
        String stellarStatement = s.getStateUpdate();
        if(stellarStatement == null) {
          return nullRet;
        }
        Object o = processor.parse(stellarStatement, resolver, SingletonFunctionResolver.getInstance(), Context.EMPTY_CONTEXT());
        return new KeyedState(leftState.key, o);
      }
    }
  }
  public static class Tokenizer implements Function<Map<String,Object>, Row> {
    private WordTransformer transformer;
    private Broadcast<Map<String, Object>> state;
    public Tokenizer(Broadcast<Map<String, Object>> state, List<String> words) {
      this.state = state;
      transformer = new WordTransformer( words);
    }

    @Override
    public Row call(Map<String, Object> message) throws Exception {
      return tokenize(state.getValue(), message, transformer);
    }

    public static Row tokenize(Map<String, Object> state, Map<String, Object> message, WordTransformer transformer) {
      HashMap<String, Object> adjustedState= new HashMap<>();
      for(Map.Entry<String, Object> kv : state.entrySet()) {
        adjustedState.put(kv.getKey() + "_state", kv.getValue());
      }
      List<String> words = transformer.transform(adjustedState, message, Context.EMPTY_CONTEXT());
      String[] row = new String[words.size()];
      for(int i = 0;i < words.size();++i) {
        row[i] = words.get(i);
      }
      Row ret = new GenericRowWithSchema(new Object[] { row }, SCHEMA);
      return ret;
    }

  }

  public Map<String, Object> gatherState(Map<String, State> config, JavaRDD<Map<String, Object>> messages) {
    Map<String, KeyedState> stateCollection =
    messages.flatMapToPair(new Projection(config))
            .reduceByKey(new Reduction(config))
            .collectAsMap();
    Map<String, Object> ret = new HashMap<>();
    for(Map.Entry<String, KeyedState> kv : stateCollection.entrySet()) {
      ret.put(kv.getKey(), kv.getValue().state);
    }
    return ret;
  }

  public JavaRDD<Map<String, Object>> parseMessages(JavaRDD<String> messages) {
    return
    messages.map( s -> JSONUtils.INSTANCE.load(s, new TypeReference<Map<String, Object>>() { }) );
  }
  public static StructType SCHEMA = new StructType(new StructField [] {
            new StructField("tokens", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
    });
  public Dataset<Row> tokenize(final Map<String, Object> state, WordConfig wordConfig, JavaRDD<Map<String, Object>> messages) {
    Broadcast<Map<String, Object> > stateBc = sc.broadcast(state);
    List<String> wordsWithSpecial = new ArrayList<>();
    wordsWithSpecial.add(wordConfig.getSpecialWord());
    wordsWithSpecial.addAll(wordConfig.getWords());
    JavaRDD<Row> rows = messages.map(new Tokenizer(stateBc, wordsWithSpecial));
    return new SQLContext(sc).createDataFrame(rows, SCHEMA);
  }

  public CountVectorizerModel createVectorizer(TrainingConfig wordConfig, Dataset<Row> df) {
    CountVectorizer countVectorizer = new CountVectorizer()
            .setVocabSize(wordConfig.getVocabSize())
            .setInputCol("tokens")
            .setOutputCol(WordConfig.FEATURES_COL);
    CountVectorizerModel vectorizerModel = countVectorizer.fit(df);
    return vectorizerModel;
  }



}
