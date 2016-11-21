package org.apache.metron.sc.integration;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

public class PreprocessingIntegrationTest implements Serializable {
  private transient JavaSparkContext sc;

  @Before
  public void setUp() {
    SparkConf conf = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
  }

  @After
  public void tearDown() {
    sc.stop();
    sc = null;
  }


  /**
   {
    "" :
   }
   */
  @Multiline
  static String config;

  @Test
  public void testPreprocessing() {

  }

}
