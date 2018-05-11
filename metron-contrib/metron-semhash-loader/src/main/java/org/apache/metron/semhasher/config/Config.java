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
package org.apache.metron.semhasher.config;

import org.apache.metron.common.utils.ReflectionUtils;
import org.apache.metron.parsers.interfaces.MessageParser;
import org.apache.metron.parsers.json.JSONMapParser;
import org.apache.metron.semhash.transform.FieldTransformation;
import org.apache.metron.semhash.transform.type.Type;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Config implements Serializable{
  private Map<String, FieldTransformation> schema;
  private MessageParser<JSONObject> parser = new JSONMapParser();
  private Map<String, Object> parserConfig;
  private Map<String, Object> vectorizerConfig;
  private Map<String, Object> binningConfig;

  public Map<String, Object> getBinningConfig() {
    return binningConfig;
  }

  public void setBinningConfig(Map<String, Object> binningConfig) {
    this.binningConfig = binningConfig;
  }

  public Map<String, Object> getVectorizerConfig() {
    return vectorizerConfig;
  }

  public void setVectorizerConfig(Map<String, Object> vectorizerConfig) {
    this.vectorizerConfig = vectorizerConfig;
  }

  public Map<String, FieldTransformation> getSchema() {
    return schema;
  }

  public void setSchema(Map<String, Object> schema) {
    this.schema = new HashMap<>();
    for(Map.Entry<String, Object> kv : schema.entrySet()) {
      if(kv.getValue() instanceof String) {
        this.schema.put(kv.getKey(), new FieldTransformation(Type.valueOf((String)kv.getValue()), new HashMap<>()));
      }
      else {
        Map<String, Object> transformation = (Map<String, Object>)kv.getValue();
        this.schema.put(kv.getKey(), new FieldTransformation(
                  Type.valueOf((String)transformation.get("type"))
                ,(Map<String, Object>) transformation.getOrDefault("config", new HashMap<>())));
      }
    }
  }

  public Map<String, Object> getParserConfig() {
    return parserConfig;
  }

  public void setParserConfig(Map<String, Object> parserConfig) {
    this.parserConfig = parserConfig;
  }

  public MessageParser<JSONObject> getParser() {
    return parser;
  }

  public void setParser(String parser) {
    this.parser = ReflectionUtils.createInstance(parser);
  }

  public void initialize() {
    this.parser.init();
    this.parser.configure(getParserConfig());
  }

}
