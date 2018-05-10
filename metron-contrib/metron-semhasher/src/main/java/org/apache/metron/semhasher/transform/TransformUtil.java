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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.metron.semhasher.config.Config;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public enum TransformUtil {
  INSTANCE;



  public List<Map<String, Object>> transform(Config config, byte[] message) {
    Parse parse = new Parse(config);
    Transform transform = new Transform(config);
    List<JSONObject> parsed = parse.apply(message);
    return Lists.newArrayList(Iterables.transform(parsed, transform));
  }

  private static class TransformMapper implements FlatMapFunction<byte[], Map<String, Object>> {
    Config config;
    transient boolean initialized = false;
    public TransformMapper(Config config) {
      this.config = config;
    }

    private synchronized Config getConfig() {
      if(!initialized) {
        config.initialize();
        initialized = true;
      }
      return config;
    }

    @Override
    public Iterable<Map<String, Object>> call(byte[] b) throws Exception {
      return TransformUtil.INSTANCE.transform(getConfig(), b);
    }
  }

  public JavaRDD<Map<String, Object>> transform(Config config, JavaRDD<byte[]> messages) {
    return messages.flatMap(new TransformMapper(config));
  }

}
