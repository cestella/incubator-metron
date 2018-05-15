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

import com.google.common.collect.ImmutableList;
import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhash.transform.FieldTransformation;
import org.apache.metron.statistics.BinFunctions;
import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.metron.stellar.common.utils.ConversionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NumericTypeTransformer implements TypeTransformer {
  public static final List<Number> DEFAULT_BINS = ImmutableList.of(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 95.0, 99.0);
  public static final String BIN_CONFIG = "numeric_bins";
  @Override
  public Object typeSpecific(Object x) {
    return ConversionUtils.convert(x, Double.class);
  }

  @Override
  public List<String> toWord( String field
                            , Object o
                            , Context context
                            , Map<String, FieldTransformation> schema
                            , Map<String, Object> message
                            , Map<String, Object> config) {
    List<String> ret = new ArrayList<>();
    if(o == null) {
      return ret;
    }
    List<? extends Number> bins = getBins(config);
    Double d = (Double)o;
    for(String key : getCategoricals(field, schema, message)) {
      Object c = context.getContext().get(key);
      if(c == null) {
        continue;
      }
      OnlineStatisticsProvider s = (OnlineStatisticsProvider)c;
      Integer bin = bin(s, bins, d);
      if (bin != null) {
        ret.add(key + ":" + bin);
      }
    }
    return ret;
  }

  @Override
  public Object reduce(Object left, Object right) {
    OnlineStatisticsProvider sLeft = (OnlineStatisticsProvider) left;
    OnlineStatisticsProvider sRight = (OnlineStatisticsProvider) right;
    return sLeft.merge(sRight);
  }

  @Override
  public boolean isCategorical() {
    return false;
  }

  private Iterable<String> getCategoricals(String fieldName, Map<String, FieldTransformation> schema, Map<String, Object> message) {
    List<String> ret = new ArrayList<>();
    for(Map.Entry<String, FieldTransformation> fieldSchema : schema.entrySet()) {
      if (fieldSchema.getValue().getType().isCategorical()) {
        String catField = fieldSchema.getKey();
        if(catField.equals(fieldName)) {
          continue;
        }
        Object catValue = message.getOrDefault(catField, null);
        if(catValue == null) {
          continue;
        }
        ret.add(fieldName + ":" + catField+ ":" + catValue);
      }
    }
    return ret;
  }

  @Override
  public Map<String, Object> map(String fieldName, Object datum, Map<String, FieldTransformation> schema, Map<String, Object> message) {
    Double d = (Double)datum;
    Map<String, Object> ret = new HashMap<>();
    if(d == null) {
      return ret;
    }
    for(String key : getCategoricals(fieldName, schema, message)) {
      OnlineStatisticsProvider s = new OnlineStatisticsProvider();
      if(d != null) {
        s.addValue(d);
      }
      ret.put(key, s);
    }

    return ret;
  }

  private static List<? extends Number> getBins(Map<String, Object> config) {
    if(config == null) {
      return DEFAULT_BINS;
    }
    Object binsO = config.get(BIN_CONFIG);
    if(binsO == null) {
      return DEFAULT_BINS;
    }
    else {
      return (List<? extends Number>) binsO;
    }
  }

  private static int bin(OnlineStatisticsProvider stats, List<? extends Number> bins, Double value) {
    return BinFunctions.Bin.getBin(value, bins.size(), bin -> stats.getPercentile(bins.get(bin).doubleValue()));
  }
}
