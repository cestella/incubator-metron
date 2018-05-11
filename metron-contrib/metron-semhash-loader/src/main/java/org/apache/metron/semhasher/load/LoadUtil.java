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
package org.apache.metron.semhasher.load;

import com.google.common.collect.Iterables;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Optional;

public enum LoadUtil {
  INSTANCE;

  public JavaRDD<byte[]> rawRead(JavaSparkContext sc, Iterable<String> inputs, Optional<Integer> partitions) {
    if(inputs == null || Iterables.isEmpty(inputs)) {
      return null;
    }
    JavaRDD<byte[]> ret = null;
    Optional<Integer> numPartitions = Optional.empty();
    if(partitions.isPresent()) {
      int p = partitions.get()/Iterables.size(inputs);
      if(p== 0) {
        numPartitions = Optional.empty();
      }
      else {
        numPartitions = Optional.of(p);
      }
    }
    for(String input : inputs) {
      JavaRDD<byte[]> inputRdd = null;
      if(numPartitions.isPresent()) {
        inputRdd = sc.textFile(input, numPartitions.get()).map(x -> x.getBytes());
      }
      else {
        inputRdd = sc.textFile(input).map(x -> x.getBytes());
      }
      if(ret == null) {
        ret = inputRdd;
      }
      else {
        ret = ret.union(inputRdd);
      }
    }
    return ret;
  }
}
