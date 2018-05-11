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
import org.apache.metron.statistics.BinFunctions;
import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.semhasher.context.TypeTransformer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NumericTypeTransformer implements TypeTransformer {
  public static final List<Number> DEFAULT_BINS = ImmutableList.of(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0);
  public static final String BIN_CONFIG = "numeric_bins";
  @Override
  public Object typeSpecific(Object x) {
    return ConversionUtils.convert(x, Double.class);
  }

  @Override
  public Optional<String> toWord(String field, Object o, Object context, Map<String, Object> config) {
    OnlineStatisticsProvider s = (OnlineStatisticsProvider)context;
    Double d = (Double)o;
    if(d != null) {
      Integer bin = bin(s, getBins(config), d);
      if (bin != null) {
        return Optional.of(field + ":" + bin);
      }
    }
    return Optional.empty();
  }

  @Override
  public Object reduce(Object left, Object right) {
    OnlineStatisticsProvider sLeft = (OnlineStatisticsProvider) left;
    OnlineStatisticsProvider sRight = (OnlineStatisticsProvider) right;
    return sLeft.merge(sRight);
  }

  @Override
  public Object map(Object datum, Object context) {
    Double d = (Double)datum;
    OnlineStatisticsProvider s = (OnlineStatisticsProvider) context;
    if(d != null) {
      s.addValue(d);
    }
    return s;
  }

  @Override
  public Optional<Object> init() {
    return Optional.of(new OnlineStatisticsProvider());
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
