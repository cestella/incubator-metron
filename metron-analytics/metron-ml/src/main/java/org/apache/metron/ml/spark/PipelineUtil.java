package org.apache.metron.ml.spark;

import org.apache.spark.ml.PipelineModel;

import java.util.Map;

public enum PipelineUtil {
  INSTANCE;

  public Map<String, Object> score(PipelineModel model, Map<String, Object> input) {
    model.transform()
  }
}
