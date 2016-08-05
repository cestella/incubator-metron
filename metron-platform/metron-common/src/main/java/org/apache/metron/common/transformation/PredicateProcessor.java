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

package org.apache.metron.common.transformation;


import org.apache.metron.common.dsl.VariableResolver;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class PredicateProcessor extends BaseTransformationProcessor<Boolean> {

  public PredicateProcessor() {
    super(Boolean.class);
  }

  @Override
  public Boolean parse(String rule, VariableResolver resolver) {
    if(rule == null || isEmpty(rule.trim())) {
      return true;
    }
    return super.parse(rule, resolver);
  }
}
