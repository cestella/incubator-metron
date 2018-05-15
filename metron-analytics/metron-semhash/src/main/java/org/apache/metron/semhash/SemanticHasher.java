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
package org.apache.metron.semhash;

import org.apache.metron.semhash.vector.VectorizerModel;
import org.apache.metron.semhash.bin.BinningModel;
import org.apache.metron.stellar.common.utils.hashing.semantic.DelegatingSemanticHasher;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SemanticHasher implements Function<Map<String, Object>, Map<String, Object>> {
  private VectorizerModel vectorizerModel;
  private BinningModel binningModel;
  public SemanticHasher(VectorizerModel vectorizerModel, BinningModel binningModel) {
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
    Map.Entry<double[], Double> vector = vectorizerModel.apply(message);
    if(vector == null) {
      return new HashMap<>();
    }
    String bin = binningModel.bin(vector.getKey());
    Map<String, Object> ret = new HashMap<String, Object>();
    ret.put(DelegatingSemanticHasher.VECTOR_KEY, vector.getKey());
    ret.put(DelegatingSemanticHasher.HASH_KEY, bin);
    ret.put(DelegatingSemanticHasher.RANK_KEY, vector.getValue());
    return ret;
  }



}
