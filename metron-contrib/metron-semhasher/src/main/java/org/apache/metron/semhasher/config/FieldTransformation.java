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

import org.apache.metron.semhasher.type.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FieldTransformation implements Serializable {
  private Type type;
  private Map<String, Object> typeConfig = new HashMap<>();

  public FieldTransformation() {

  }

  public FieldTransformation(Type type, Map<String, Object> typeConfig) {
    this.type = type;
    this.typeConfig = typeConfig;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Map<String, Object> getTypeConfig() {
    return typeConfig;
  }

  public void setTypeConfig(Map<String, Object> typeConfig) {
    this.typeConfig = typeConfig;
  }
}