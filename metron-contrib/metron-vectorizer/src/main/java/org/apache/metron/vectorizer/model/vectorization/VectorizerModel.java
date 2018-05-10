package org.apache.metron.vectorizer.model.vectorization;

import org.apache.metron.vectorizer.context.Context;
import org.apache.spark.api.java.JavaRDD;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface VectorizerModel extends Function<Map<String, Object>, double[]> , Serializable {
  void train(Map<String, Object> config, JavaRDD<Map<String, Object>> messages);
  int getDimension();
  List<double[]> getSample();
}
