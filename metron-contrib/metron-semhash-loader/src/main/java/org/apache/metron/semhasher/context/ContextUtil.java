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
package org.apache.metron.semhasher.context;

import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhash.transform.FieldTransformation;
import org.apache.metron.semhasher.config.Config;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum ContextUtil {
  INSTANCE;

  public Context generateContext(JavaRDD<Map<String, Object>> input, Config config) {
    JavaPairRDD<String, KV> contextualized = input.flatMapToPair(new ContextMapper(config));
    JavaPairRDD<String, KV> ret = contextualized.reduceByKey(new ContextReducer(config));
    Context context = new Context(new HashMap<>());
    for(Tuple2<String, KV> t : ret.collect()) {
      context.getContext().put(t._1, t._2.getValue());
    }
    return context;
  }

  private static class ContextMapper implements PairFlatMapFunction<Map<String,Object>, String, KV > {
    private Config config;
    public ContextMapper(Config config) {
      this.config = config;
    }
    @Override
    public Iterable<Tuple2<String, KV >> call(Map<String, Object> message) throws Exception {
      List<Tuple2<String, KV>> ret = new ArrayList<>();
      for(Map.Entry<String, FieldTransformation> kv : config.getSchema().entrySet() ) {
        String field = kv.getKey();
        Object value = message.get(field);
        if(value != null) {
          FieldTransformation transform = kv.getValue();
          Map<String, Object> mapped = transform.getType().map(field, value, config.getSchema(), message);
          if(mapped != null) {
            for(Map.Entry<String, Object> mappedKv : mapped.entrySet()) {
              ret.add(new Tuple2<>(mappedKv.getKey(), new KV(field, mappedKv.getValue(), mappedKv.getKey())));
            }
          }
        }
      }
      return ret;
    }
  }

  private static class KV {
    private String field;
    private String contextField;
    private Object value;

    public KV(String field, Object value, String contextField) {
      this.field = field;
      this.value = value;
      this.contextField = contextField;
    }

    public String getField() {
      return field;
    }

    public Object getValue() {
      return value;
    }

    public String getContextField() {
      return contextField;
    }
  }

  private static class ContextReducer implements Function2<KV, KV, KV> {
    private Config config;
    public ContextReducer(Config config) {
      this.config = config;
    }

    @Override
    public KV call(KV left, KV right) throws Exception {
      String field = left.getField();
      FieldTransformation transform = config.getSchema().get(field);
      Object reduction = transform.getType().reduce(left.getValue(), right.getValue());
      return new KV(left.getField(), reduction, left.getContextField());
    }
  }
}
