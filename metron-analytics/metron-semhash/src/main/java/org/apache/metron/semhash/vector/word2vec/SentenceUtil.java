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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum SentenceUtil {
  INSTANCE;

  public Iterable<String> toSentence(Map<String, Object> message, Context contexts, Map<String, FieldTransformation> schema, boolean normalize) {
    List<String> ret = new ArrayList<>();
    for(Map.Entry<String, FieldTransformation> kv : schema.entrySet()) {
      Object raw = message.get(kv.getKey());
      if (raw == null) {
        continue;
      }

      FieldTransformation transformation = kv.getValue();
      if(normalize) {
        raw = transformation.getType().typeSpecific(raw);
        if(raw == null) {
          continue;
        }
      }
      List<String> phrase = transformation.getType()
                                          .toWord(kv.getKey(), raw, contexts, schema, message,transformation.getTypeConfig());
      if(!phrase.isEmpty()) {
        ret.addAll(phrase);
      }
    }
    return ret;
  }
}
