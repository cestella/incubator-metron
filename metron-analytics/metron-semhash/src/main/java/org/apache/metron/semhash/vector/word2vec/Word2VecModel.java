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

import com.google.common.base.Joiner;
import org.apache.metron.semhash.transform.FieldTransformation;
import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhash.vector.VectorizerModel;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class Word2VecModel implements VectorizerModel {

  private Map<String, Map.Entry<float[], Float>> model;
  private Map<String, FieldTransformation> schema;
  private Context context;
  private int dimension;
  private List<double[]> sample;

  public Word2VecModel( Map<String, FieldTransformation> schema
                      , Context context
                      , int dimension
                      , Map<String, Map.Entry<float[], Float>> model
                      ) {
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

  public Map.Entry<double[], Double> apply(Iterable<String> sentence) {
    //System.out.println(Joiner.on(" ").join(sentence));
    double[] v = null;
    Double proj = null;
    int n = 0;
    for(String word : sentence) {
      Map.Entry<float[], Float> o = model.get(word);
      if(o == null ) {
        continue;
      }
      float[] wordV = o.getKey();
      if(wordV == null ) {
        continue;
      }
      /*System.out.print(word + " => ");
      for(int i = 0;i < wordV.length;++i) {
        System.out.print(wordV[i] + " ");
      }
      System.out.println();*/
      if(v == null) {
        proj = o.getValue() != null?o.getValue().doubleValue():null;
        v = new double[wordV.length];
        for(int i = 0;i < v.length;++i) {
          v[i] = wordV[i];
        }
      }
      else {
        proj = (proj == null?0d:proj) + (o.getValue() != null?o.getValue().doubleValue():null);
        for(int i = 0;i < v.length;++i) {
          v[i] += wordV[i];
        }
      }
      n++;
    }
    if(v == null) {
      return null;
    }

    proj = proj == null?null:proj/n;
    for(int i = 0;i < v.length;++i) {
      v[i] /= n;
    }
    return new AbstractMap.SimpleEntry<>(v, proj);
  }

  @Override
  public Map.Entry<double[], Double> apply(Map<String, Object> input) {
    Iterable<String> sentence = SentenceUtil.INSTANCE.toSentence(input, context, schema, true);
    return apply(sentence);
  }

}
