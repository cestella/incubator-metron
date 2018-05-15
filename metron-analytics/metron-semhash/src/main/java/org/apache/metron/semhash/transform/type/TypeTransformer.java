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

import org.apache.commons.lang3.StringUtils;
import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhash.transform.FieldTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TypeTransformer {
  Object typeSpecific(Object o);
  default List<String> toWord(String field
                                , Object o
                                , Context context
                                , Map<String, FieldTransformation> schema
                                , Map<String, Object> message
                                , Map<String, Object> config) {
    List<String> words = new ArrayList<>();
    if(o != null) {
      String s = o.toString();
      if(!StringUtils.isEmpty(s)) {
        words.add(field + ":" + s);
      }
    }
    return words;
  }

  default Map<String, Object> map(String fieldName, Object datum, Map<String, FieldTransformation> schema, Map<String, Object> message) {
    return null;
  }

  default Object reduce(Object left, Object right) {
    return null;
  }

  boolean isCategorical();
}
