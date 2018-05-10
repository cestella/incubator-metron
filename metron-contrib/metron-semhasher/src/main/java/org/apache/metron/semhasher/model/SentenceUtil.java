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
package org.apache.metron.semhasher.model;

import org.apache.metron.semhasher.config.FieldTransformation;
import org.apache.metron.semhasher.config.Config;
import org.apache.metron.semhasher.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum SentenceUtil {
  INSTANCE, LoadUtil;

  public Iterable<String> toSentence(Map<String, Object> message, Context contexts, Config config, boolean normalize) {
    List<String> ret = new ArrayList<>();
    for(Map.Entry<String, FieldTransformation> kv : config.getSchema().entrySet()) {
      Object raw = message.get(kv.getKey());
      if (raw == null) {
        continue;
      }

      Object context = contexts.getContext().get(kv.getKey());
      FieldTransformation transformation = kv.getValue();
      if(normalize) {
        raw = transformation.getType().typeSpecific(raw);
        if(raw == null) {
          continue;
        }
      }
      Optional<String> word = transformation.getType().toWord(kv.getKey(), raw, context, transformation.getTypeConfig());
      if(word.isPresent()) {
        ret.add(word.get());
      }
    }
    return ret;
  }
}
