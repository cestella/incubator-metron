package org.apache.metron.sc.training;

import java.util.List;

public class TrainingConfig {
  private Integer k;
  private Integer maxIter;
  private List<String> inputDocs;
  private String outputPath;
  private Integer vocabSize;

  public Integer getK() {
    return k;
  }

  public void setK(Integer k) {
    this.k = k;
  }

  public Integer getMaxIter() {
    return maxIter;
  }

  public void setMaxIter(Integer maxIter) {
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

  public Integer getVocabSize() {
    return vocabSize;
  }

  public void setVocabSize(int vocabSize) {
    this.vocabSize = vocabSize;
  }

}
