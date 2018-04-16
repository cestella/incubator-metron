package org.apache.metron.webrequest.classifier;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.GBTClassifier;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.NGram;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.sql.DataFrame;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum PipelineConfig {
  VECTOR_SIZE("vectorSize", Optional.of(100), o -> ((Number)o).intValue()),
  MAX_ITER("maxIter", Optional.of(50), o -> ((Number)o).intValue()),
  MIN_COUNT("minCount", Optional.of(20), o -> ((Number)o).intValue()),
  SEED("seed", Optional.of(0), o -> ((Number)o).intValue()),
  //TOKENIZER("tokenizer", Optional.of("(\\s+|\\?|\\&|=|/)"), o -> o.toString())
  TOKENIZER("tokenizer", Optional.of(""), o -> o.toString())
  ;
  public static final String LABEL_COLUMN = "indexedLabel";
  public static final String PREDICTION_COLUMN = "prediction";
  public static final String PREDICTION_LABEL_COLUMN = "predictionLabel";
  private String key;
  private Optional<Object> defaultValue;
  private Function<Object, Object> converter;
  PipelineConfig(String key, Optional<Object> defaultValue, Function<Object, Object> converter) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.converter = converter;
  }

  public static EnumMap<PipelineConfig, Optional<Object>> config(Map<String, Object> config) {
    EnumMap<PipelineConfig, Optional<Object>> ret = new EnumMap<>(PipelineConfig.class);
    for(PipelineConfig c : PipelineConfig.values()) {
      Object o = config.get(c.key);
      Optional<Object> val = c.defaultValue;
      if(o != null) {
        val = Optional.ofNullable(c.converter.apply(o));
      }
      ret.put(c, val);
    }
    return ret;
  }

  public static Pipeline createPipeline(EnumMap<PipelineConfig, Optional<Object>> config, DataFrame allData) {
    StringIndexerModel indexer = new StringIndexer().setInputCol("label")
                                               .setOutputCol("indexedLabel")
                                               .fit(allData);
    RegexTokenizer tokenizer = new RegexTokenizer()
            .setInputCol("text")
            .setOutputCol("words")
            .setPattern(config.get(PipelineConfig.TOKENIZER).get().toString())
            ;
    NGram ngramizer = new NGram().setN(3).setInputCol("words").setOutputCol("ngrams");
    Word2Vec w2vFeatureExtractor = new Word2Vec().setInputCol(tokenizer.getOutputCol())
            .setOutputCol("w2vfeatures")
            .setVectorSize((int)(config.get(PipelineConfig.VECTOR_SIZE).get()))
            .setMinCount((int)(config.get(PipelineConfig.MIN_COUNT).get()))
            ;

    HashingTF tfFeatureExtractor = new HashingTF()
            .setNumFeatures(1500)
            .setInputCol(ngramizer.getOutputCol())
            .setOutputCol("tffeatures");
    IDF idfer = new IDF().setInputCol("tffeatures").setOutputCol("tfidffeatures").setMinDocFreq(20);
    VectorAssembler assembler = new VectorAssembler().setInputCols(new String[] {
            "w2vfeatures",
            "tfidffeatures"})
                                                     .setOutputCol("features");

    /*GBTClassifier classifier = new GBTClassifier()
            .setMaxIter((int)(config.get(PipelineConfig.MAX_ITER).get()))
            .setLabelCol(LABEL_COLUMN)
            .setFeaturesCol("features")
            .setSeed((int)(config.get(PipelineConfig.SEED).get()));*/
    LogisticRegression classifier = new LogisticRegression()
                                    .setMaxIter((int)(config.get(PipelineConfig.MAX_ITER).get()))
                                    .setRegParam(0.0001)
                                    //.setElasticNetParam(0.8)
                                    .setLabelCol(LABEL_COLUMN)
                                    .setFeaturesCol("features")
                                    ;
    IndexToString unindexer = new IndexToString().setInputCol(PREDICTION_COLUMN)
                                                 .setOutputCol(PREDICTION_LABEL_COLUMN)
                                                 .setLabels(indexer.labels());
    Pipeline pipeline = new Pipeline()
            .setStages(new PipelineStage[] {
                      indexer
                    , tokenizer
                    , ngramizer
                    , w2vFeatureExtractor
                    , tfFeatureExtractor
                    , idfer
                    , assembler
                    , classifier
                    , unindexer});
    return pipeline;
  }

  public static PipelineModel train(DataFrame trainingData, Pipeline pipeline) {
    return pipeline.fit(trainingData);
  }

}
