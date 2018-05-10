package org.apache.metron.index;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.metron.statistics.BinFunctions;
import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.stellar.common.utils.SerDeUtils;
import org.apache.metron.webrequest.classifier.LabeledDocument;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public enum SchemaUtil {
  INSTANCE;
  public static enum Type implements Function<String, Object> {
    NUMERIC(x -> ConversionUtils.convert(x, Double.class)),
    TEXT(x -> x),
    IGNORE(x -> null);
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
        List<Object> r = new ArrayList<>();
        for(int i = 0;i < val.size();++i) {
          r.add(schemaTypes.get(i).apply(val.get(i)));
        }
        ret.add(r);
        return ret;
      }
    });
  }

  private List<Object> toIntermediateForm(String row, List<Type> schemaTypes) {
    List<String> val = new ArrayList<String>();
    for(String part : Splitter.on(",").split(row)) {
      val.add(part);
    }
    List<Object> r = new ArrayList<>();
    for(int i = 0;i < val.size();++i) {
      r.add(schemaTypes.get(i).apply(val.get(i)));
    }
    return r;
  }

  public LabeledDocument toSentence( String row
                          , Map<String, Type> schema
                          , Map<String, OnlineStatisticsProvider> context
                          , List<Number> bins
  ) {
    final List<Type> schemaTypes = new ArrayList<>();
    Map<Integer, String> positionToColumn = new LinkedHashMap<>();
    {
      int i = 0;
      for (Map.Entry<String, Type> kv : schema.entrySet()) {
        schemaTypes.add(kv.getValue());
        positionToColumn.put(i++, kv.getKey());
      }
    }
    List<Object> objects = toIntermediateForm(row, schemaTypes);
    List<String> doc = new ArrayList<>(objects.size());
    for(int i = 0;i < objects.size();++i) {
      Object o = objects.get(i);
      String column = positionToColumn.get(i);
      OnlineStatisticsProvider s = context.get(column);
      String data = null;
      if(s == null) {
        data = o== null?null:o.toString();
      }
      else {
        Integer bin = getBin(s, bins, (Double)o);
        data = bin == null?null:("" + bin);
      }
      if(data != null) {
        data = column + ":" + data;
      }
      doc.add(data);
    }
    if(doc.size() > 0) {
      return new LabeledDocument(Joiner.on(" ").skipNulls().join(doc), "");
    }
    else {
      return null;
    }
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

  public DataFrame toDF(SQLContext sqlContext, JavaRDD<LabeledDocument> dataset) {
    return sqlContext.createDataFrame(dataset.rdd(), LabeledDocument.class);
  }

  public DataFrame toDF(SQLContext sqlContext, LabeledDocument dataPoint) {
    return sqlContext.createDataFrame(new ArrayList<LabeledDocument>() {{
      add(dataPoint);
    }}, LabeledDocument.class);
  }

  private static class ToDoc implements FlatMapFunction<List<Object>, LabeledDocument> {
    Map<String, byte[]> contextraw;
    transient Map<String, OnlineStatisticsProvider> context;
    List<Number> bins;
    Map<Integer, String> positionToColumn;
    public ToDoc(List<Number> bins, Map<String, byte[]> contextraw, Map<Integer, String> positionToColumn) {
      this.bins = bins;
      this.contextraw = contextraw;
      this.positionToColumn = positionToColumn;
    }

    private synchronized Map<String, OnlineStatisticsProvider> getContext() {
      if(context == null) {
        this.context = new HashMap<>();
        for(Map.Entry<String, byte[]> kv : contextraw.entrySet()) {
          this.context.put(kv.getKey(), SerDeUtils.fromBytes(kv.getValue(), OnlineStatisticsProvider.class));
        }
      }
      return this.context;
    }

    @Override
    public Iterable<LabeledDocument> call(List<Object> objects) throws Exception {
      List<String> doc = new ArrayList<>(objects.size());
        for(int i = 0;i < objects.size();++i) {
          Object o = objects.get(i);
          String column = positionToColumn.get(i);
          OnlineStatisticsProvider s = getContext().get(column);
          String data = null;
          if(s == null) {
            data = o== null?null:o.toString();
          }
          else {
            Integer bin = getBin(s, bins, (Double)o);
            data = bin == null?null:("" + bin);
          }
          if(data != null) {
            data = column + ":" + data;
          }
          doc.add(data);
        }
        return new ArrayList<LabeledDocument>(1) {{
          if(doc.size() > 0) {
            LabeledDocument d = new LabeledDocument(Joiner.on(" ").skipNulls().join(doc), "");
            add(d);
          }
        }};
    }
  }

  public JavaRDD<LabeledDocument> toDocument(JavaRDD<List<Object>> data
                                            , Map<String, Type> schema
                                            , Map<String, OnlineStatisticsProvider> context
                                            , List<Number> bins
                                            ) {
    Map<Integer, String> positionToColumn = new LinkedHashMap<>();
    int i = 0;
    for(Map.Entry<String, Type> kv : schema.entrySet()) {
      positionToColumn.put(i++, kv.getKey());
    }
    Map<String, byte[]> contextraw = new HashMap<>();
    for(Map.Entry<String, OnlineStatisticsProvider> kv : context.entrySet()) {
      contextraw.put(kv.getKey(), SerDeUtils.toBytes(kv.getValue()));
    }

    return data.flatMap( new ToDoc(bins, contextraw, positionToColumn));
  }



  private static int getBin(OnlineStatisticsProvider stats, List<Number> bins, Double value) {
    return BinFunctions.Bin.getBin(value, bins.size(), bin -> stats.getPercentile(bins.get(bin).doubleValue()));
  }

}
