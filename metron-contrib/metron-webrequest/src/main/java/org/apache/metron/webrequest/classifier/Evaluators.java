package org.apache.metron.webrequest.classifier;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.sql.DataFrame;

import java.util.EnumMap;

public enum Evaluators {
  F1("f1"),
  PRECISION("precision"),
  RECALL("recall"),
  WEIGHTED_PRECISION("weightedPrecision"),
  WEIGHTED_RECALL("weightedRecall")
  ;
  String metricName;
  Evaluators(String metricName) {
    this.metricName = metricName;
  }
  public MulticlassClassificationEvaluator create(String labelCol, String predictorCol) {
    MulticlassClassificationEvaluator ret = new MulticlassClassificationEvaluator()
                                            .setLabelCol(labelCol)
                                            .setPredictionCol(predictorCol)
                                            .setMetricName(metricName);
    return ret;
  }

  public static EnumMap<Evaluators, Object> evaluatePipeline(DataFrame test, PipelineModel model) {
    DataFrame predictions = model.transform(test);
    EnumMap<Evaluators, Object> ret = new EnumMap<>(Evaluators.class);
    for(Evaluators e : Evaluators.values()) {
      Object o = e.create(PipelineConfig.LABEL_COLUMN, PipelineConfig.PREDICTION_COLUMN).evaluate(predictions);
      ret.put(e, o);
    }
    return ret;
  }

}
