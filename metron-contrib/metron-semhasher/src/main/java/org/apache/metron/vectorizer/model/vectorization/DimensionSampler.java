package org.apache.metron.vectorizer.model.vectorization;

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
