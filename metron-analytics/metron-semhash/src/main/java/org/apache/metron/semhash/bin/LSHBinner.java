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
package org.apache.metron.semhash.bin;

import com.google.common.base.Joiner;
import info.debatty.java.lsh.LSHSuperBit;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.semhash.vector.VectorizerModel;
import org.apache.metron.stellar.common.utils.HexUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class LSHBinner implements BinningModel {
  public static final String STAGES_CONF = "stages";
  public static final String BUCKETS_CONF = "buckets";
  public static final String DISTANCE_CONF = "distance";
  public static final double DISTANCE_DEFAULT = 0.9;
  public static final String TARGET_ERROR_CONF = "error";
  public static final double TARGET_ERROR_DEFAULT = 0.04;
  public static final String TARGET_FALSE_POSITIVE_CONF = "falsePositives";
  public static final double TARGET_FALSE_POSITIVE_DEFAULT = 1e-6;
  public static final int NUM_BUCKETS_IN_HASH_DEFAULT = 1;
  public static final String NUM_BUCKETS_IN_HASH_CONF = "bucketsInHash";

  private LSHSuperBit lsh;
  private int numBucketsInHash = NUM_BUCKETS_IN_HASH_DEFAULT;
  public LSHBinner(LSHSuperBit lsh, int numBucketsInHash) {
    this.lsh = lsh;
    this.numBucketsInHash = numBucketsInHash;
  }

  @Override
  public String bin(double[] vector) {
    return bin(lsh, vector, numBucketsInHash);
  }

  private static String bin(LSHSuperBit lsh, double[] vector, int numBucketsInHash) {
    int[] hashes = lsh.hash(vector);
    String ret = "";
    for(int i = 0;i < numBucketsInHash;++i) {
      ret += (i == 0?"":",") + hashes[i];
    }
    return ret;
  }

  public static LSHBinner create(VectorizerModel model, Map<String, Object> config) {
    int dimension = model.getDimension();
    Object stage = config.get(STAGES_CONF);
    List<Integer> stages = new ArrayList<>();
    if(stage instanceof Iterable) {
      for(Object o : (Iterable)stage) {
        stages.add(ConversionUtils.convert(o, Integer.class));
      }
    }
    else {
      stages.add(ConversionUtils.convert(stage, Integer.class));
    }
    Object bucket = config.get(BUCKETS_CONF);
    List<Integer> buckets = new ArrayList<>();
    if(bucket instanceof Iterable) {
      for(Object o : (Iterable)bucket) {
        buckets.add(ConversionUtils.convert(o, Integer.class));
      }
    }
    else {
      buckets.add(ConversionUtils.convert(bucket, Integer.class));
    }
    int numBucketsInHash = ConversionUtils.convert(config.getOrDefault(NUM_BUCKETS_IN_HASH_CONF, NUM_BUCKETS_IN_HASH_DEFAULT), Integer.class);
    if(stages.size() == 1 && buckets.size() == 1) {
      return new LSHBinner(new LSHSuperBit(stages.get(0), buckets.get(0), dimension, 0), numBucketsInHash);
    }
    else {
      double distance = ConversionUtils.convert(config.getOrDefault(DISTANCE_CONF, DISTANCE_DEFAULT), Double.class);
      double targetError = ConversionUtils.convert(config.getOrDefault(TARGET_ERROR_CONF, TARGET_ERROR_DEFAULT), Double.class);
      double targetFalsePositive = ConversionUtils.convert(config.getOrDefault(TARGET_FALSE_POSITIVE_CONF, TARGET_FALSE_POSITIVE_DEFAULT), Double.class);
      Optional<LSHSuperBit> lsh = gridsearch(stages, buckets, distance,  model.getSample(), targetError, targetFalsePositive, numBucketsInHash);
      if(lsh.isPresent()) {
        return new LSHBinner(lsh.get(), numBucketsInHash);
      }
      else {
        throw new IllegalStateException("Unable to find an acceptable bucketing scheme.  Please expand grid search parameters.");
      }
    }
  }


  public static Optional<LSHSuperBit> gridsearch( List<Integer> stages
                                         , List<Integer> buckets
                                         , double deltaNeighborhood
                                         , List<double[]> vectors
                                         , double acceptableError
                                         , double acceptableFalsePositive
                                         , int numBucketsInHash
                                         ) {
    System.out.println("Sample size: " + vectors.size());
    System.out.println("Delta neighborhood: " + deltaNeighborhood);
    System.out.println("Acceptable Error: " + acceptableError);
    for(int stage : stages) {
      for(int bucket : buckets) {
        LSHSuperBit lsh = new LSHSuperBit(stage, bucket, vectors.get(0).length, 0L);
        Results result = evaluate(vectors, lsh, deltaNeighborhood, numBucketsInHash);
        System.out.println("Stage: " + stage + ", bucket: " + bucket + " => " + result);
        if(result.error <= acceptableError && result.falsePositive <= acceptableFalsePositive) {
          return Optional.of(lsh);
        }
      }
    }
    return Optional.empty();
  }

  private static class Results {
    public double error;
    public double falsePositive;

    public Results(double error, double falsePositive) {
      this.error = error;
      this.falsePositive = falsePositive;
    }

    @Override
    public String toString() {
      return "error=" + error + ", falsePositive=" + falsePositive;
    }
  }

  private static Results evaluate(List<double[]> vectors, LSHSuperBit lsh, double deltaNeighborhood, int numBucketsInHash) {
    Map<Map.Entry<Integer, Integer>, Boolean> baseTruth = evaluate(vectors, (l,r) -> {
      RealVector leftV = new ArrayRealVector(l);
      RealVector rightV = new ArrayRealVector(r);
      return Math.abs(leftV.cosine(rightV)) >= deltaNeighborhood;
    });
    Map<Map.Entry<Integer, Integer>, Boolean> lshEval = evaluate(vectors, (l,r) -> {
      String leftBin = bin(lsh, l, numBucketsInHash);
      String rightBin = bin(lsh, r, numBucketsInHash);
      return leftBin.equals(rightBin);
    });
    double accuracy = 0.0d;
    int n = 0;
    int numInNeighborhood = 0;
    int falsePositives = 0;
    for(Map.Entry<Integer, Integer> entry : baseTruth.keySet()) {
      boolean truth = baseTruth.get(entry);
      boolean evaluated = lshEval.get(entry);
      if(truth == evaluated) {
        accuracy = accuracy + 1;
      }
      if(truth) {
        numInNeighborhood++;

      }
      if(evaluated && !truth) {
        falsePositives++;
      }
      n++;
    }
    accuracy /= n;
    System.out.println(" Num in Neighborhood: " + numInNeighborhood);
    System.out.println(" False Positives: " + falsePositives + " ( " + (1.0*falsePositives)/n + ")");
    return new Results(1.0 - accuracy, (1.0*falsePositives)/n);
  }

  private static Map<Map.Entry<Integer, Integer>, Boolean>
  evaluate(List<double[]> vectors, BiFunction<double[], double[], Boolean> scorer) {
    Map<Map.Entry<Integer, Integer>, Boolean> upperTriangle = new HashMap<>();
    for(int i = 0;i < vectors.size();++i) {
      double[] left = vectors.get(i);
      for(int j = i+1;j < vectors.size();++j) {
        double[] right = vectors.get(j);
        upperTriangle.put(new AbstractMap.SimpleEntry<>(i, j), scorer.apply(left, right));
      }
    }
    return upperTriangle;
  }
}
