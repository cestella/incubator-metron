package org.apache.metron.vectorizer.model;

import com.google.common.collect.ImmutableList;
import info.debatty.java.lsh.LSHSuperBit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.metron.common.utils.SerDeUtils;
import org.apache.metron.vectorizer.model.binning.LSHBinner;
import org.apache.metron.vectorizer.model.vectorization.VectorizerModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
