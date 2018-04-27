package org.apache.metron.index;

import com.google.common.base.Splitter;
import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.webrequest.classifier.LabeledDocument;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public enum SchemaUtil {
  INSTANCE;
  public static enum Type implements Function<String, Object> {
    NUMERIC(x -> ConversionUtils.convert(x, Double.class)),
    TEXT(x -> x);
    Function<String, Object> func;
    Type(Function<String, Object> func) {
      this.func = func;
    }
    @Override
    public Object apply(String in) {
      return func.apply(in);
    }
  }

  public JavaRDD<List<Object>> readData( JavaSparkContext sc
                                       , String input
                                       , Predicate<List<String>> filter
                                       , Map<String, Type> schema
                                       ) {
    JavaRDD<String> base = sc.textFile(input);
    final List<Type> schemaTypes = new ArrayList<>();
    for(Map.Entry<String, Type> kv : schema.entrySet()) {
      schemaTypes.add(kv.getValue());
    }
    return base.flatMap(new FlatMapFunction<String, List<Object>>() {
      @Override
      public Iterable<List<Object>> call(String s) throws Exception {
        List<List<Object>> ret = new ArrayList<>(1);
        if(s == null) {
          return ret;
        }
        List<String> val = new ArrayList<String>();
        for(String part : Splitter.on(",").split(s)) {
          val.add(part);
        }
        if(filter.test(val)) {
          List<Object> r = new ArrayList<>();
          for(int i = 0;i < val.size();++i) {
            r.add(schemaTypes.get(i).apply(val.get(i)));
          }
        }
        return ret;
      }
    });
  }

  public Map<String, OnlineStatisticsProvider> generateContext( JavaRDD<List<Object>> data
                                                              , Map<String, Type> schema
                                                              ) {
    final List<Type> schemaTypes = new ArrayList<>();
    for(Map.Entry<String, Type> kv : schema.entrySet()) {
      schemaTypes.add(kv.getValue());
    }
    JavaPairRDD<String, OnlineStatisticsProvider> rdd =
    data.flatMapToPair(new PairFlatMapFunction<List<Object>, String, OnlineStatisticsProvider>() {
      @Override
      public Iterable<Tuple2<String, OnlineStatisticsProvider>> call(List<Object> d) throws Exception {
        List<Tuple2<String, OnlineStatisticsProvider>> ret = new ArrayList<>();
        int i = 0;
        for(Map.Entry<String, Type> kv : schema.entrySet()) {
          if(kv.getValue() == Type.NUMERIC) {
            Object o = d.get(i);
            if(o != null) {
              Double x = (Double)o;
              OnlineStatisticsProvider s = new OnlineStatisticsProvider();
              s.addValue(x);
              ret.add(new Tuple2<>(kv.getKey(), s));
            }
          }
          i++;
        }
        return ret;
      }
    }).reduceByKey((Function2<OnlineStatisticsProvider, OnlineStatisticsProvider, OnlineStatisticsProvider>) (onlineStatisticsProvider, onlineStatisticsProvider2) -> (OnlineStatisticsProvider)onlineStatisticsProvider.merge(onlineStatisticsProvider2));
    return rdd.collectAsMap();
  }

  public JavaRDD<LabeledDocument> toDocument(JavaRDD<List<Object>> data
                                            , Map<String, Type> schema
                                            , Map<String, OnlineStatisticsProvider> context
                                            ) {
    Map<Integer, String> positionToColumn = new LinkedHashMap<>();
    int i = 0;
    for(Map.Entry<String, Type> kv : schema.entrySet()) {
      positionToColumn.put(i++, kv.getKey());
    }
  }

}
