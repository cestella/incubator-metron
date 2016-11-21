package org.apache.metron.sc.clustering;

import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizerModel;

import java.io.File;

public class ClusterModel {
  private CountVectorizerModel vectorizerModel;
  private LDAModel ldaModel;
  public ClusterModel(CountVectorizerModel vectorizerModel, LDAModel ldaModel) {
    this.vectorizerModel = vectorizerModel;
    this.ldaModel = ldaModel;
  }

  public static ClusterModel load(File path) {
    return null;
  }

  public void save(File path) {

  }
}
