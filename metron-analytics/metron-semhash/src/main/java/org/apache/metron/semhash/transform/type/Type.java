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
package org.apache.metron.semhash.transform.type;

import java.util.Map;
import java.util.Optional;

public enum Type implements TypeTransformer {
  NUMERIC(new NumericTypeTransformer()),
  TEXT(new TextTypeTransformer())
  ;
  TypeTransformer func;
  Type(TypeTransformer func) {
    this.func = func;
  }

  @Override
  public Object typeSpecific(Object o) {
    return func.typeSpecific(o);
  }

  @Override
  public Optional<String> toWord(String field, Object o, Object context, Map<String, Object> wordConfig) {
    return func.toWord(field, o, context, wordConfig);
  }

  @Override
  public Optional<Object> init() {
    return func.init();
  }

  @Override
  public Object map(Object datum, Object context) {
    return func.map(datum, context);
  }

  @Override
  public Object reduce(Object left, Object right) {
    return func.reduce(left, right);
  }
}
