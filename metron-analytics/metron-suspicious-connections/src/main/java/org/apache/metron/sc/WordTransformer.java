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

package org.apache.metron.sc;

import org.apache.commons.lang3.StringUtils;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.MapVariableResolver;
import org.apache.metron.common.dsl.functions.resolver.SingletonFunctionResolver;
import org.apache.metron.common.stellar.StellarProcessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordTransformer implements Serializable {
  private List<String> wordTransformers;
  public WordTransformer( List<String> wordTransformers ) {
    this.wordTransformers = wordTransformers;
  }

  public List<String> transform(Map<String, Object> state, Map<String, Object> message, Context context) {
    MapVariableResolver resolver = new MapVariableResolver(message, state);
    StellarProcessor processor = new StellarProcessor();
    List<String> ret = new ArrayList<>();
    for(String stellarTransforms : wordTransformers) {
      Object o = processor.parse(stellarTransforms, resolver, SingletonFunctionResolver.getInstance(), context);
      if(o == null) {
        continue;
      }
      if(o instanceof List) {
        for(Object listO : (List<Object>)o) {
          if(listO != null && !StringUtils.isEmpty(listO.toString()))
          ret.add(listO.toString());
        }
      }
      else if(o instanceof Map) {
        for(Map.Entry<Object, Object> kv : ((Map<Object, Object>)o).entrySet()) {
          if(kv.getValue() != null && !StringUtils.isEmpty(kv.getValue() + "")) {
            ret.add(kv.getKey() + "_" + kv.getValue());
          }
        }
      }
      else {
        if(o != null && !StringUtils.isEmpty(o.toString())) {
          ret.add(o.toString());
        }
      }
    }
    return ret;
  }

}
