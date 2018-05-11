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
package org.apache.metron.semhasher.model;

import com.google.common.collect.ImmutableList;
import info.debatty.java.lsh.LSHSuperBit;
import org.apache.metron.semhash.bin.LSHBinner;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BinnerTest {
  @Test
  public void gridsearchTest() throws Exception {
    int count = 100;
    int n = 100;
    Random r = new Random(0);
    List<double[]> vectors = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      vectors.add(new double[n]);

      for (int j = 0; j < n; j++) {
        vectors.get(i)[j] = r.nextGaussian();
      }
    }
    Optional<LSHSuperBit> model = LSHBinner.gridsearch(ImmutableList.of( 3, 25, 50, 75, 100, 125, 150, 175, 200)
                           ,ImmutableList.of( 10, 50, 100, 150, 200)
                           ,0.9
                           , vectors
                           , 0.04
                           , 1e-6
                           , 3
                           );
    Assert.assertTrue(model.isPresent());
  }
}
