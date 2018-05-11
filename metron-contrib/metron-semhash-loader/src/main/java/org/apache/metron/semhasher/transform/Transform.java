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
package org.apache.metron.semhasher.transform;

import com.google.common.base.Function;
import org.apache.metron.semhash.transform.FieldTransformation;
import org.apache.metron.semhasher.config.Config;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Transform implements Function<JSONObject, Map<String, Object>> {
  private Config config;
  public Transform(Config config) {
    this.config = config;
  }

  @Nullable
  @Override
  public Map<String, Object> apply(@Nullable JSONObject message) {
    Map<String, Object> ret = new LinkedHashMap<>();
    for(Map.Entry<String, FieldTransformation> kv : config.getSchema().entrySet()) {
      Object raw = message.get(kv.getKey());
      if(raw == null) {
        continue;
      }
      Object transformed = kv.getValue().getType().typeSpecific(raw);
      if(transformed != null) {
        ret.put(kv.getKey(), transformed);
      }
    }
    return ret;
  }
}
