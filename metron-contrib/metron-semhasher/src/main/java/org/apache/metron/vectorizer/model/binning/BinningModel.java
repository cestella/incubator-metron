package org.apache.metron.vectorizer.model.binning;

public interface BinningModel {
  String bin(double[] vector);
}
