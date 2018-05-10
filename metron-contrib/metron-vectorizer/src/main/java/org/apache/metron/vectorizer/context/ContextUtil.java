package org.apache.metron.vectorizer.context;

import org.apache.metron.vectorizer.config.FieldTransformation;
import org.apache.metron.vectorizer.config.Config;
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
    JavaPairRDD<String, Tuple2<String, Object>> contextualized = input.flatMapToPair(new ContextMapper(config));
    JavaPairRDD<String, Object> ret = contextualized.reduceByKey(new ContextReducer(config)).mapValues(x->x._2);
    Context context = new Context(new HashMap<>());
    for(Tuple2<String, Object> t : ret.collect()) {
      context.getContext().put(t._1, t._2);
    }
    return context;
  }

  private static class ContextMapper implements PairFlatMapFunction<Map<String,Object>, String, Tuple2<String, Object> > {
    private Config config;
    public ContextMapper(Config config) {
      this.config = config;
    }
    @Override
    public Iterable<Tuple2<String, Tuple2<String, Object> >> call(Map<String, Object> message) throws Exception {
      List<Tuple2<String, Tuple2<String, Object>>> ret = new ArrayList<>();
      for(Map.Entry<String, FieldTransformation> kv : config.getSchema().entrySet() ) {
        String field = kv.getKey();
        Object value = message.get(field);
        if(value != null) {
          FieldTransformation transform = kv.getValue();
          Optional<Object> context = transform.getType().init();
          if (context.isPresent()) {
            Object mapped = transform.getType().map(value, context.get());
            if(mapped != null) {
              ret.add(new Tuple2<>(field, new Tuple2<>(field, mapped)));
            }
          }
        }
      }
      return ret;
    }
  }

  private static class ContextReducer implements Function2<Tuple2<String, Object>, Tuple2<String, Object>, Tuple2<String, Object>> {
    private Config config;
    public ContextReducer(Config config) {
      this.config = config;
    }

    @Override
    public Tuple2<String, Object> call(Tuple2<String, Object> left, Tuple2<String, Object> right) throws Exception {
      String field = left._1;
      FieldTransformation transform = config.getSchema().get(field);
      Object reduction = transform.getType().reduce(left._2, right._2);
      return new Tuple2<>(field, reduction);
    }
  }
}
