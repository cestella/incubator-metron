/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
