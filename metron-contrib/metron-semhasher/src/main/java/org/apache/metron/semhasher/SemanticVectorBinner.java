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
package org.apache.metron.semhasher;

import org.apache.metron.stellar.common.utils.hashing.semantic.SemanticHasher;
import org.apache.metron.semhasher.config.Config;
import org.apache.metron.semhasher.context.Context;
import org.apache.metron.semhasher.context.ContextUtil;
import org.apache.metron.semhasher.model.binning.LSHBinner;
import org.apache.metron.semhasher.model.vectorization.VectorizerModel;
import org.apache.metron.semhasher.model.binning.BinningModel;
import org.apache.metron.semhasher.model.vectorization.Word2VecModel;
import org.apache.metron.semhasher.transform.TransformUtil;
import org.apache.spark.api.java.JavaRDD;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SemanticVectorBinner implements Function<Map<String, Object>, Map<String, Object>> {
  private VectorizerModel vectorizerModel;
  private BinningModel binningModel;
  public SemanticVectorBinner(VectorizerModel vectorizerModel, BinningModel binningModel) {
    this.vectorizerModel = vectorizerModel;
    this.binningModel = binningModel;
  }

  public VectorizerModel getVectorizerModel() {
    return vectorizerModel;
  }

  public BinningModel getBinningModel() {
    return binningModel;
  }

  public Map<String, Object> apply(Map<String, Object> message) {
    double[] vector = vectorizerModel.apply(message);
    if(vector == null) {
      return new HashMap<>();
    }
    String bin = binningModel.bin(vector);
    Map<String, Object> ret = new HashMap<String, Object>();
    ret.put(SemanticHasher.VECTOR_KEY, vector);
    ret.put(SemanticHasher.HASH_KEY, bin);
    return ret;
  }

  public static SemanticVectorBinner create(JavaRDD<byte[]> inputData, Config config) {
    JavaRDD<Map<String, Object>> transformedInputData = TransformUtil.INSTANCE.transform(config, inputData);
    transformedInputData.cache();
    Context context = ContextUtil.INSTANCE.generateContext(transformedInputData, config);
    Word2VecModel vecModel = new Word2VecModel();
    vecModel.setConfig(config);
    vecModel.setContext(context);
    vecModel.train(config.getVectorizerConfig(), transformedInputData);

    LSHBinner binningModel = LSHBinner.create(vecModel, config.getBinningConfig());
    SemanticVectorBinner ret = new SemanticVectorBinner(vecModel, binningModel);
    return ret;
  }

}