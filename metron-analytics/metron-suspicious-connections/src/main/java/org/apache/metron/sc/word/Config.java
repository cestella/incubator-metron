package org.apache.metron.sc.word;

import java.util.List;
import java.util.Map;

public class Config {
  public static final String FEATURES_COL = "features";
  Map<String, State> state;
  List<String> words;
  int vocabSize;
  List<String> inputDocs;
  private String outputPath;
  private int k;
  private int maxIter;

  public List<String> getInputDocs() {
    return inputDocs;
  }

  public void setInputDocs(List<String> inputDocs) {
    this.inputDocs = inputDocs;
  }

  public Map<String, State> getState() {
    return state;
  }

  public void setState(Map<String, State> state) {
    this.state = state;
  }

  public List<String> getWords() {
    return words;
  }

  public void setWords(List<String> words) {
    this.words = words;
  }

  public int getVocabSize() {
    return vocabSize;
  }

  public void setVocabSize(int vocabSize) {
    this.vocabSize = vocabSize;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

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
}
