package org.apache.metron.sc.training;

import com.google.common.base.Joiner;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.clustering.ClusterModel;
import org.apache.metron.sc.clustering.Clusterer;
import org.apache.metron.sc.word.Config;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TrainingCLI {
  public static void main(String... argv) throws IOException {
    File configFile = new File(argv[0]);
    Config config = JSONUtils.INSTANCE.load(configFile, Config.class);
    SparkConf conf = new SparkConf().setAppName("metron-suspicious-connections");
    JavaSparkContext context = new JavaSparkContext(conf);
    ClusterModel model = createModel(config, context);
    model.save(new File(config.getOutputPath()));
    //todo figure out how to save
  }

  public static ClusterModel createModel(Config config, JavaSparkContext context) {
    SQLContext sqlContext = new SQLContext(context);
    JavaRDD<String> inputDocs = context.textFile(Joiner.on(",").join(config.getInputDocs()));
    JavaRDD<Map<String, Object>> messages = Preprocessor.parseMessages(inputDocs);
    messages.cache();
    Map<String, Object> state = Preprocessor.gatherState(config.getState(), messages);
    Dataset<Row> tokens = Preprocessor.tokenize(state, config, messages, sqlContext);
    CountVectorizerModel vectorizationModel = Preprocessor.createVectorizer(config, tokens);
    Dataset<Row> vectorized = vectorizationModel.transform(tokens);
    LDAModel model = Clusterer.trainModel(config, vectorized);
    return new ClusterModel(vectorizationModel, model);
  }

}
