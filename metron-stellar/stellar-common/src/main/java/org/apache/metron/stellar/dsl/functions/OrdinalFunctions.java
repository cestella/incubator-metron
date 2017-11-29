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

package org.apache.metron.stellar.dsl.functions;

import com.google.common.collect.Iterables;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.stellar.dsl.BaseStellarFunction;
import org.apache.metron.stellar.dsl.Stellar;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrdinalFunctions {

  /**
   * Stellar Function: MAX
   * <p>
   * Return the maximum value of a list of input values in a Stellar list
   */
  @Stellar(name = "MAX"
          , description = "Returns the maximum value of a list of input values"
          , params = {"list_of_values - Stellar list of values to evaluate. The list may only contain 1 type of object (only strings or only numbers)" +
          " and the objects must be comparable / ordinal"}
          , returns = "The highest value in the list, null if the list is empty or the input values could not be ordered")
  public static class Max extends BaseStellarFunction {

    @Override
    public Object apply(List<Object> args) {
      if (args.size() < 1 || args.get(0) == null) {
        throw new IllegalStateException("MAX function requires at least a Stellar list of values");
      }
      Iterable list = (Iterable<Object>) args.get(0);
      return orderList(list, (ret, val) -> ret.compareTo(val) < 0, "MAX");
    }
  }

  /**
   * Stellar Function: MIN
   * <p>
   * Return the minimum value of a list of input values in a Stellar list
   */
  @Stellar(name = "MIN"
          , description = "Returns the minimum value of a list of input values"
          , params = {"list_of_values - Stellar list of values to evaluate. The list may only contain 1 type of object (only strings or only numbers)" +
          " and the objects must be comparable / ordinal"}
          , returns = "The lowest value in the list, null if the list is empty or the input values could not be ordered")
  public static class Min extends BaseStellarFunction {
    @Override
    public Object apply(List<Object> args) {
      if (args.size() < 1 || args.get(0) == null) {
        throw new IllegalStateException("MIN function requires at least a Stellar list of values");
      }
      List<Comparable> list = (List<Comparable>) args.get(0);
      return orderList(list, (ret, val) -> ret.compareTo(val) > 0, "MIN");
    }
  }

  private static Comparable orderList(Iterable<Comparable> list, BiFunction<Comparable, Comparable, Boolean> eval, String funcName) {
    if (Iterables.isEmpty(list)) {
      return null;
    }
    Comparable ret = null;
    for(Comparable<?> value : list) {
      if(value == null) {
        continue;
      }
      try {
        Comparable convertedRet = ConversionUtils.convert(ret, value.getClass());
        if(convertedRet == null && ret != null) {
         throw new IllegalStateException("Incomparable objects were submitted to " + funcName
                + ": " + ret.getClass() + " is incomparable to " + value.getClass());
        }
        if(ret == null || eval.apply(convertedRet, value) ) {
          ret = value;
        }
      }
      catch(ClassCastException cce) {
        throw new IllegalStateException("Incomparable objects were submitted to " + funcName
                + ": " + cce.getMessage(), cce);
      }
    }
    return ret;
  }

}
