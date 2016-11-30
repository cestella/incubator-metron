package org.apache.metron.sc.training;

import java.util.List;

public class TrainingConfig {
  private int k;
  private int maxIter;
  private List<String> inputDocs;
  private String outputPath;
  private int vocabSize;

  public int getK() {
    return k;
  }

  public void setK(int k) {
    this.k = k;
  }

  public int getMaxIter() {
    return maxIter;
  }

  public void setMaxIter(int maxIter) {
    this.maxIter = maxIter;
  }

  public List<String> getInputDocs() {
    return inputDocs;
  }

  public void setInputDocs(List<String> inputDocs) {
    this.inputDocs = inputDocs;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public int getVocabSize() {
    return vocabSize;
  }

  public void setVocabSize(int vocabSize) {
    this.vocabSize = vocabSize;
  }

}
