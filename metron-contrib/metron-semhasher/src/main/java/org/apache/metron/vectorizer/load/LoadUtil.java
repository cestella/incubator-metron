package org.apache.metron.vectorizer.load;

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
