/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.semhash.vector.word2vec;

import org.apache.metron.semhash.transform.FieldTransformation;
import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhash.vector.VectorizerModel;

import java.util.List;
import java.util.Map;

public class Word2VecModel implements VectorizerModel {

  private Map<String, float[]> model;
  private Map<String, FieldTransformation> schema;
  private Context context;
  private int dimension;
  private List<double[]> sample;

  public Word2VecModel(Map<String, FieldTransformation> schema, Context context, int dimension, Map<String, float[]> model) {
    this.model = model;
    this.dimension = dimension;
    this.schema = schema;
    this.context = context;
  }

  public void setDimension(int dimension) {
    this.dimension = dimension;
  }

  public Context getContext() {
    return context;
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public List<double[]> getSample() {
    return sample;
  }

  public void setSample(List<double[]> sample) {
    this.sample = sample;
  }

  public double[] apply(Iterable<String> sentence) {
    double[] v = null;
    int n = 0;
    for(String word : sentence) {
      float[] wordV = model.get(word);
      if(wordV == null) {
        continue;
      }
      if(v == null) {
        v = new double[wordV.length];
        for(int i = 0;i < v.length;++i) {
          v[i] = wordV[i];
        }
      }
      else {
        for(int i = 0;i < v.length;++i) {
          v[i] += wordV[i];
        }
      }
      n++;
    }
    if(v == null) {
      return null;
    }
    for(int i = 0;i < v.length;++i) {
      v[i] /= n;
    }
    return v;
  }

  @Override
  public double[] apply(Map<String, Object> input) {
    Iterable<String> sentence = SentenceUtil.INSTANCE.toSentence(input, context, schema, true);
    return apply(sentence);
  }

}
