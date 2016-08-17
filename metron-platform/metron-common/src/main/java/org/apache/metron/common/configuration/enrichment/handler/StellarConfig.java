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
package org.apache.metron.common.configuration.enrichment.handler;

import org.apache.metron.common.stellar.StellarProcessor;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.function.Function;

public class StellarConfig implements Config {


  @Override
  public JSONObject splitByFields( JSONObject message
                                 , Object fields
                                 , Function<String, String> fieldToEnrichmentKey
                                 , Map<String, Object> config
                                 )
  {
    JSONObject ret = new JSONObject();
    StellarProcessor processor = new StellarProcessor();
    Set<String> stellarFields = new HashSet<>();
    for(Object value : config.values()) {
      String stellarStatement = (String) value;
      Set<String> variables = processor.variablesUsed(stellarStatement);
      if(variables != null) {
        stellarFields.addAll(variables);
      }
    }
    Map<String, Object> messageSegment = new HashMap<>();
    for(String variable : stellarFields) {
      messageSegment.put(variable, message.get(variable));
    }
    ret.put("", messageSegment);
    return ret;
  }
}
