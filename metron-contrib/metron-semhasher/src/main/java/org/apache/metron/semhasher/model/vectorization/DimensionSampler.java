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
package org.apache.metron.semhasher.model.vectorization;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class DimensionSampler implements Function<Random, Double> {
  private double min = Long.MAX_VALUE;
  private double max = Long.MIN_VALUE;
  public DimensionSampler() {
  }

  public void update(double d) {
    min = Math.min(d, min);
    max = Math.max(d, max);
  }

  @Override
  public Double apply(Random random) {
    double range = max - min;
    return range* random.nextDouble() + min;
  }

  public static double[] random(Random r, List<DimensionSampler> convexHull) {
    double[] vec = new double[convexHull.size()];
    for(int d = 0;d < vec.length;++d) {
      vec[d] = convexHull.get(d).apply(r);
    }
    RealVector rVec = new ArrayRealVector(vec);
    return rVec.unitVector().toArray();
  }

}