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
package org.apache.metron.common.dsl;

import java.util.List;

public class StellarFunctionInfo {
  String description;
  String name;
  String[] params;
  StellarFunction function;
  String returns;
  public StellarFunctionInfo(String description, String name, String[] params, String returns, StellarFunction function) {
    this.description = description;
    this.name = name;
    this.params = params;
    this.function = function;
    this.returns = returns;
  }

  public String getReturns() { return returns;}

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public String[] getParams() {
    return params;
  }

  public StellarFunction getFunction() {
    return function;
  }
}
