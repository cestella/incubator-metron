package org.apache.metron.webrequest.classifier;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DatasetUtil implements Serializable {
  INSTANCE;

  public DataFrame mergeDatasets(JavaSparkContext sc, SQLContext sqlContext, Map<String, List<String>> inputsByLabel) {
    Map<Long, JavaRDD<LabeledDocument>> datasetMap = new HashMap<>();
    long minSize = Long.MAX_VALUE;
    for(Map.Entry<String, List<String>> kv : inputsByLabel.entrySet()) {
      final String label = kv.getKey();
      JavaRDD<LabeledDocument> union = null;
      for(String i : kv.getValue()) {
        JavaRDD<LabeledDocument> f = sc.textFile(i).map(
                (Function<String, LabeledDocument>) s -> new LabeledDocument(s, label)
        );
        if(union == null) {
          union = f;
        }
        else {
          union = union.union(f);
        }
      }
      long size = union.count();
      minSize = Math.min(union.count(), minSize);
      datasetMap.put(size, union);
    }
    JavaRDD<LabeledDocument> dataset = null;
    for(Map.Entry<Long, JavaRDD<LabeledDocument>> rdd : datasetMap.entrySet()) {
      JavaRDD<LabeledDocument> labelDataset = rdd.getValue();
      if(minSize != rdd.getKey()) {
        double fraction = (1.0*minSize) / rdd.getKey();
        labelDataset = labelDataset.sample(false, fraction);
      }
      System.out.println("Dataset: " + labelDataset.count());
      if(dataset == null) {
        dataset = labelDataset;
      }
      else {
        dataset = dataset.union(labelDataset);
      }
    }
    return sqlContext.createDataFrame(dataset.rdd(), LabeledDocument.class);
  }

  public DataFrame[] split(DataFrame data, double trainingPct) {
    return data.randomSplit(new double[] { trainingPct, 1.0-trainingPct});
  }
}
