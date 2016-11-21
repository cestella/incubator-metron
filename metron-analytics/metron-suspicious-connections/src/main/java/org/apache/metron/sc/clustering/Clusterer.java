package org.apache.metron.sc.clustering;

import org.apache.metron.sc.word.Config;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Map;

public class Clusterer {
  public static LDAModel trainModel(Config config, Dataset<Row> vectorizedData) {
    LDA lda = new LDA().setFeaturesCol(Config.FEATURES_COL)
                       .setK(config.getK())
                       .setMaxIter(config.getMaxIter())
                       ;
    return lda.fit(vectorizedData);
  }

  public static double score(Map<String, Object> message, LDAModel model) {
    /*
      class SuspiciousConnectsScoreFunction(topicCount: Int,
  8                                       ipToTopicMixBC: Broadcast[Map[String, Array[Double]]],
  9                                       wordToPerTopicProbBC: Broadcast[Map[String, Array[Double]]]) extends Serializable {
 10
 11   def score(ip: String, word: String): Double = {
 12
 13     val uniformProb = Array.fill(topicCount) {
 14       1.0d / topicCount
 15     }
 16
 17     val topicGivenDocProbs = ipToTopicMixBC.value.getOrElse(ip, uniformProb)
 18     val wordGivenTopicProbs = wordToPerTopicProbBC.value.getOrElse(word, uniformProb)
 19
 20     topicGivenDocProbs.zip(wordGivenTopicProbs)
 21       .map({ case (pWordGivenTopic, pTopicGivenDoc) => pWordGivenTopic * pTopicGivenDoc })
 22       .sum
 23   }
 24
 25 }
     */
    return Double.NaN;
  }


}
