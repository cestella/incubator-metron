package org.apache.metron.index;

import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.spark.ml.PipelineModel;

import java.util.Map;

public class VectorModel {
  PipelineModel model;
  Map<String, OnlineStatisticsProvider>  context;
  public VectorModel() {

  }

  public VectorModel(PipelineModel model, Map<String, OnlineStatisticsProvider> context) {
    setModel(model);
    setContext(context);
  }


  public PipelineModel getModel() {
    return model;
  }

  public void setModel(PipelineModel model) {
    this.model = model;
  }

  public Map<String, OnlineStatisticsProvider> getContext() {
    return context;
  }

  public void setContext(Map<String, OnlineStatisticsProvider> context) {
    this.context = context;
  }
}
