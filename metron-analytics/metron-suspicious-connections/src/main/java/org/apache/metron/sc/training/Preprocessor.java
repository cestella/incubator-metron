package org.apache.metron.sc.training;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.map.HashedMap;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.FunctionResolverSingleton;
import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.stellar.StellarProcessor;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.word.Config;
import org.apache.metron.sc.word.State;
import org.apache.metron.sc.word.WordTransformer;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.linalg.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.*;
import scala.Tuple2;

import java.util.*;

public class Preprocessor {
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
        Object o = processor.parse(kv.getValue().getStateProjection(), resolver, FunctionResolverSingleton.getInstance(), Context.EMPTY_CONTEXT());
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
        Object o = processor.parse(stellarStatement, resolver, FunctionResolverSingleton.getInstance(), Context.EMPTY_CONTEXT());
        return new KeyedState(leftState.key, o);
      }
    }
  }
  public static class Tokenizer implements Function<Map<String,Object>, Row> {
    private WordTransformer transformer;
    public Tokenizer(Map<String, Object> state, List<String> words) {
      HashMap<String, Object> adjustedState= new HashMap<>();
      for(Map.Entry<String, Object> kv : state.entrySet()) {
        state.put(kv.getKey() + "_state", kv.getValue());
      }
      transformer = new WordTransformer(adjustedState, words);
    }

    @Override
    public Row call(Map<String, Object> message) throws Exception {
      List<String> words = transformer.transform(message, Context.EMPTY_CONTEXT());
      return RowFactory.create(words);
    }
  }

  public static Map<String, Object> gatherState(Map<String, State> config, JavaRDD<Map<String, Object>> messages) {
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

  public static JavaRDD<Map<String, Object>> parseMessages(JavaRDD<String> messages) {
    return
    messages.map( s -> JSONUtils.INSTANCE.load(s, new TypeReference<Map<String, Object>>() { }) );
  }

  public static Dataset<Row> tokenize(final Map<String, Object> state, Config config, JavaRDD<Map<String, Object>> messages, SQLContext sqlContext) {
    StructType schema = new StructType(new StructField [] {
            new StructField("tokens", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
    });
    JavaRDD<Row> rows = messages.map(new Tokenizer(state, config.getWords()));
    return sqlContext.createDataFrame(rows, schema);
  }

  public static CountVectorizerModel createVectorizer(Config config, Dataset<Row> df) {
    CountVectorizer countVectorizer = new CountVectorizer()
            .setVocabSize(config.getVocabSize())
            .setInputCol("tokens")
            .setOutputCol(Config.FEATURES_COL);
    CountVectorizerModel vectorizerModel = countVectorizer.fit(df);
    return vectorizerModel;
  }


}
