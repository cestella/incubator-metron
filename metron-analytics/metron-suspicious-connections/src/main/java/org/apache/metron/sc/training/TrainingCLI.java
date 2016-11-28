/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
    Preprocessor preprocessor = new Preprocessor(context);
    JavaRDD<String> inputDocs = context.textFile(Joiner.on(",").join(config.getInputDocs()));
    JavaRDD<Map<String, Object>> messages = preprocessor.parseMessages(inputDocs);
    messages.cache();
    Map<String, Object> state = preprocessor.gatherState(config.getState(), messages);
    Dataset<Row> tokens = preprocessor.tokenize(state, config, messages);
    CountVectorizerModel vectorizationModel = preprocessor.createVectorizer(config, tokens);
    Dataset<Row> vectorized = vectorizationModel.transform(tokens);
    LDAModel model = Clusterer.trainModel(config, vectorized);
    return new ClusterModel(vectorizationModel, model);
  }

}
