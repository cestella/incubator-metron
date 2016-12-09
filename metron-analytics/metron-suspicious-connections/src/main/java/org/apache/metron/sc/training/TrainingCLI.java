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
import org.apache.metron.sc.ClusterModel;
import org.apache.metron.sc.preprocessing.Preprocessor;
import org.apache.metron.sc.preprocessing.WordConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TrainingCLI {
  public static void main(String... argv) throws IOException {
    File configFile = new File(argv[0]);
    File trainingConfigFile = new File(argv[1]);
    WordConfig wordConfig = JSONUtils.INSTANCE.load(configFile, WordConfig.class);
    TrainingConfig trainingConfig = JSONUtils.INSTANCE.load(trainingConfigFile, TrainingConfig.class);
    SparkConf conf = new SparkConf().setAppName("metron-suspicious-connections");
    JavaSparkContext context = new JavaSparkContext(conf);
    JavaRDD<String> inputDocs = context.textFile(Joiner.on(",").join(trainingConfig.getInputDocs()));
    ClusterModel model = createModel(trainingConfig, wordConfig, inputDocs, context);
    model.save(new File(trainingConfig.getOutputPath()));
  }


  public static ClusterModel createModel( TrainingConfig trainingConfig
                                        , WordConfig wordConfig
                                        , JavaRDD<String> inputDocs
                                        , JavaSparkContext context
                                        )
  {
    Preprocessor preprocessor = new Preprocessor(context);
    JavaRDD<Map<String, Object>> messages = preprocessor.parseMessages(inputDocs);
    messages.cache();
    Map<String, Object> state = preprocessor.gatherState(wordConfig.getState(), messages);
    Dataset<Row> tokens = preprocessor.tokenize(state, wordConfig, messages);
    CountVectorizerModel vectorizationModel = preprocessor.createVectorizer(trainingConfig, tokens);
    Dataset<Row> vectorized = vectorizationModel.transform(tokens);
    LDAModel model = trainModel(trainingConfig, vectorized);
    return new ClusterModel(state, wordConfig, vectorizationModel.vocabulary(), model);
  }

  public static LDAModel trainModel(TrainingConfig config, Dataset<Row> vectorizedData) {
    LDA lda = new LDA().setFeaturesCol(WordConfig.FEATURES_COL)
                       .setK(config.getK())
                       .setMaxIter(config.getMaxIter())
                       ;
    return lda.fit(vectorizedData);
  }

}
