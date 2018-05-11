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
package org.apache.metron.semhasher.context;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

public interface TypeTransformer {
  Object typeSpecific(Object o);

  default Optional<String> toWord(String field, Object o, Object context, Map<String, Object> wordConfig) {
    if(o == null) {
      return Optional.empty();
    }
    String s = o.toString();
    return StringUtils.isEmpty(s)?Optional.empty():Optional.of(field + ":" + s);
  }

  default Optional<Object> init() {
    return Optional.empty();
  }

  default Object map(Object datum, Object context) {
    return null;
  }

  default Object reduce(Object left, Object right) {
    return null;
  }
}
