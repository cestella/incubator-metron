package org.apache.metron.index;

import org.apache.metron.webrequest.classifier.LabeledDocument;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;



public enum Word2VecUtil {
  INSTANCE;

  public Pipeline createPipeline(int vectorSize, int minCount) throws Exception {
    Tokenizer tokenizer = new Tokenizer()
            .setInputCol("text")
            .setOutputCol("words")
            ;
    Word2Vec w2v =  new Word2Vec().setInputCol(tokenizer.getOutputCol())
            .setOutputCol("features")
            .setVectorSize(vectorSize)
            .setMinCount(minCount)
            ;
    return new Pipeline().setStages(new PipelineStage[]{ tokenizer , w2v });

  }

  public PipelineModel train(Pipeline pipeline, DataFrame dataset) {
    return pipeline.fit(dataset);
  }

  public DataFrame score(PipelineModel model, DataFrame input) {
    return model.transform(input);
  }


}
