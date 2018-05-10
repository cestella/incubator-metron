package org.apache.metron.webrequest;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.metron.common.utils.SerDeUtils;
import org.apache.metron.index.SchemaUtil;
import org.apache.metron.index.VectorModel;
import org.apache.metron.index.Word2VecUtil;
import org.apache.metron.statistics.OnlineStatisticsProvider;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.metron.stellar.common.utils.JSONUtils;
import org.apache.metron.webrequest.classifier.DatasetUtil;
import org.apache.metron.webrequest.classifier.Evaluators;
import org.apache.metron.webrequest.classifier.LabeledDocument;
import org.apache.metron.webrequest.classifier.PipelineConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.collection.mutable.WrappedArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Smoketest implements Serializable {
  protected transient JavaSparkContext sc;
  protected transient SQLContext sqlContext;

  @Before
  public void setup() {
    SparkConf conf  = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.set("spark.kryoserializer.buffer.max", "500m");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
    sqlContext = new SQLContext(sc);
  }

  @After
  public void shutdown() {
    sc.stop();
  }

  Map<String, SchemaUtil.Type>  schema = new HashMap<String, SchemaUtil.Type>() {{
      put("time", SchemaUtil.Type.IGNORE);
      put("duration", SchemaUtil.Type.NUMERIC);
      put("source_computer", SchemaUtil.Type.TEXT);
      put("source_port", SchemaUtil.Type.TEXT);
      put("destination_computer", SchemaUtil.Type.TEXT);
      put("destination_port", SchemaUtil.Type.TEXT);
      put("protocol", SchemaUtil.Type.TEXT);
      put("packet_count", SchemaUtil.Type.NUMERIC);
      put("byte_count", SchemaUtil.Type.NUMERIC);
    }};
    List<Number> bins = new ArrayList<Number>() {{
      for(double d : new double[]{10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0}) {
        add(d);
      }
    }};
  public static final String basePath = "/Users/cstella/Documents/workspace/metron/demo_2018/lanl";
  public static final String modelOutput = basePath + "/model/w2v_model";
  public static final File contextOutput = new File(basePath + "/model/context");
  public static final String flows = basePath + "/flows_supersubset.txt";

  @Test
  public void testW2vWrite() throws Exception {

    JavaRDD<List<Object>> rawData = SchemaUtil.INSTANCE.readData(sc, flows, schema);
    Map<String, OnlineStatisticsProvider> context = SchemaUtil.INSTANCE.generateContext(rawData, schema);
    System.out.println("Generated Context for " + Joiner.on(",").join(context.keySet()));
    JavaRDD<LabeledDocument> docs = SchemaUtil.INSTANCE.toDocument(rawData, schema, context, bins);
    Pipeline pipeline = Word2VecUtil.INSTANCE.createPipeline(200, 10);
    DataFrame inputData = SchemaUtil.INSTANCE.toDF(sqlContext, docs);
    inputData.cache();
    PipelineModel model = Word2VecUtil.INSTANCE.train(pipeline, inputData);
    model.save(modelOutput);
    if(!contextOutput.exists()) {
      contextOutput.mkdirs();
    }
    for(Map.Entry<String, OnlineStatisticsProvider> kv : context.entrySet()) {
      try(FileOutputStream fs = new FileOutputStream(new File(contextOutput, kv.getKey()))) {
        byte[] contextBytes = SerDeUtils.toBytes(kv.getValue());
        IOUtils.write(contextBytes, fs);
      }
    }
  }

  @Test
  public void dumpMatching() throws Exception{
    int deltaS = 15*60;
    File redteamFile = new File("/Volumes/Samsung_T1/data/lanl/redteam.txt");
    File flows = new File("/Volumes/Samsung_T1/data/lanl/flows.txt");
    File out = new File("/Volumes/Samsung_T1/data/lanl/reflow.json");
    long startTime = 734601L;
    long endTime = 772000;
    Map<Map.Entry<String, String>, List<String>> redteamMatches = new HashMap<>();
    try(BufferedReader br = new BufferedReader(new FileReader(redteamFile))) {
      for(String line = null;(line = br.readLine()) != null;) {
        Iterable tokens = Splitter.on(",").split(line);
        String from = Iterables.get(tokens, 2).toString();
        String to = Iterables.get(tokens, 3).toString();
        Map.Entry<String, String> key = new AbstractMap.SimpleEntry<>(from, to);
        List<String> container = redteamMatches.getOrDefault(key, new ArrayList<>());
        container.add(line);
        redteamMatches.put(key, container);
      }
    }
    try(BufferedReader br = new BufferedReader(new FileReader(flows))) {
      try(PrintWriter pw = new PrintWriter(out)) {
        for (String line = null; (line = br.readLine()) != null; ) {
          final Iterable tokens = Splitter.on(",").split(line);
          Map<String, Object> message = new HashMap<String, Object>() {{
            int i = 0;
            put("timestamp", ConversionUtils.convert(Iterables.get(tokens, i++), Long.class));
            put("duration", ConversionUtils.convert(Iterables.get(tokens, i++), Integer.class));
            put("source_computer", ConversionUtils.convert(Iterables.get(tokens, i++), String.class));
            put("source_port", ConversionUtils.convert(Iterables.get(tokens, i++), String.class));
            put("destination_computer", ConversionUtils.convert(Iterables.get(tokens, i++), String.class));
            put("destination_port", ConversionUtils.convert(Iterables.get(tokens, i++), String.class));
            put("protocol", ConversionUtils.convert(Iterables.get(tokens, i++), String.class));
            put("packet_count", ConversionUtils.convert(Iterables.get(tokens, i++), Integer.class));
            put("byte_count", ConversionUtils.convert(Iterables.get(tokens, i++), Integer.class));
          }};
          long ts = (long) message.get("timestamp");
          if( ts < startTime) {
            continue;
          }
          else if(ts > endTime) {
            break;
          }
          String from = Iterables.get(tokens, 2).toString();
          String to = Iterables.get(tokens, 4).toString();
          List<String> matchingLines = redteamMatches.get(new AbstractMap.SimpleEntry<>(from, to));
          if (matchingLines != null) {
            for(String matchingLine : matchingLines) {
              Iterable<String> redteamTokens = Splitter.on(",").split(matchingLine);
              int redteamTime = Integer.parseInt(Iterables.getFirst(redteamTokens, null));
              int actualTime = Integer.parseInt(Iterables.getFirst(Splitter.on(",").split(line), null));
              if (redteamTime > actualTime && redteamTime - actualTime < deltaS) {
                int i = 0;
                int secondsSinceBreakin = redteamTime - actualTime;
                message.put("breakin_seconds_since", secondsSinceBreakin);
                message.put("breakin_timestamp", ConversionUtils.convert(Iterables.get(redteamTokens, i++), Long.class));
                message.put("breakin_user", ConversionUtils.convert(Iterables.get(redteamTokens, i++), String.class));
                message.put("breakin_source_computer", ConversionUtils.convert(Iterables.get(redteamTokens, i++), String.class));
                message.put("breakin_destination_computer", ConversionUtils.convert(Iterables.get(redteamTokens, i++), String.class));
              }
            }
          }
          pw.println(new String(JSONUtils.INSTANCE.toJSON(message)));
        }
      }
    }
  }

  public static String[] expectedFar= new String[] {
          "113344,3,C16563,N13340,C1685,N5,6,7,2132",
          "113344,3,C1685,N5,C16563,N13340,6,6,3245",
          "113345,0,C4222,389,C16563,N14280,17,1,200",
          "113347,0,C16563,N13324,C1685,N5,6,5,1100",
          "113347,0,C16563,N13326,C1685,N5,6,5,1100",
          "113347,0,C16563,N13329,C1685,N5,6,5,1100",
          "113347,0,C1685,N5,C16563,N13324,6,4,582",
          "113347,0,C1685,N5,C16563,N13326,6,4,588",
          "113347,0,C1685,N5,C16563,N13329,6,4,585",
          "113374,0,C16563,N13653,C1685,N5,6,5,856"
  };

  public static String[] expectedkindaclose = new String[] {
          "734601,1,C17693,N25746,C11178,445,6,8,1168",
          "734711,1,C17693,N4824,C11178,445,6,8,1168",
          "769045,1,C17693,N93,C11178,N4,1,2,168",
          "769082,3,C17693,N26703,C11178,445,6,76,25801",
          "769084,1,C17693,443,C11178,N8263,6,524,776049",
          "769085,0,C17693,443,C11178,N1288,6,6,595",
          "769085,0,C17693,443,C11178,N1289,6,6,675",
          "769085,0,C17693,443,C11178,N1291,6,6,595",
          "769085,0,C17693,443,C11178,N1298,6,6,595",
          "769085,0,C17693,443,C11178,N1299,6,6,595",
          "769085,0,C17693,443,C11178,N1300,6,6,595",
          "769085,0,C17693,443,C11178,N1309,6,6,595",
          "769085,0,C17693,443,C11178,N1323,6,6,595",
          "769085,0,C17693,443,C11178,N13622,6,6,675",
          "769085,0,C17693,443,C11178,N14128,6,6,595",
          "769085,0,C17693,443,C11178,N14130,6,6,595",
          "769085,0,C17693,443,C11178,N19687,6,6,675",
          "769085,0,C17693,443,C11178,N21663,6,6,595",
          "769085,0,C17693,443,C11178,N21950,6,6,595",
          "769085,0,C17693,443,C11178,N21951,6,6,595"
  };
  public static String[] expectedClose = new String[] {
          "1178532,2,C17693,N17960,C9006,445,6,69,25472",
          "1178534,0,C17693,N302,C9006,N20508,6,525,776031",
          "1178534,0,C17693,N302,C9006,N20522,6,6,595",
          "1178534,1,C17693,N302,C9006,N18162,6,112,161152",
          "1178535,0,C17693,N302,C9006,N10187,6,6,595",
          "1178535,0,C17693,N302,C9006,N11062,6,6,595",
          "1178535,0,C17693,N302,C9006,N11102,6,6,675",
          "1178535,0,C17693,N302,C9006,N11130,6,7,641",
          "1178535,0,C17693,N302,C9006,N11940,6,6,595",
          "1178535,0,C17693,N302,C9006,N12593,6,6,595",
          "1178535,0,C17693,N302,C9006,N12621,6,7,641",
          "1178535,0,C17693,N302,C9006,N12622,6,6,595",
          "1178535,0,C17693,N302,C9006,N12629,6,6,595",
          "1178535,0,C17693,N302,C9006,N12636,6,38,50061",
          "1178535,0,C17693,N302,C9006,N12637,6,6,595",
          "1178535,0,C17693,N302,C9006,N12638,6,7,641",
          "1178535,0,C17693,N302,C9006,N12639,6,6,595",
          "1178535,0,C17693,N302,C9006,N12640,6,6,595",
          "1178535,0,C17693,N302,C9006,N1336,6,6,595",
          "1178535,0,C17693,N302,C9006,N13406,6,6,595",
          "1178535,0,C17693,N302,C9006,N14084,6,6,595",
          "1178535,0,C17693,N302,C9006,N15932,6,6,595",
          "1178535,0,C17693,N302,C9006,N1677,6,7,641",
          "1178535,0,C17693,N302,C9006,N17045,6,6,595",
          "1178535,0,C17693,N302,C9006,N1725,6,6,595",
          "1178535,0,C17693,N302,C9006,N18157,6,8,687",
          "1178535,0,C17693,N302,C9006,N18158,6,6,595",
          "1178535,0,C17693,N302,C9006,N18201,6,6,675",
          "1178535,0,C17693,N302,C9006,N18382,6,6,595",
          "1178535,0,C17693,N302,C9006,N18614,6,6,595",
          "1178535,0,C17693,N302,C9006,N20512,6,6,595",
          "1178535,0,C17693,N302,C9006,N20514,6,6,595",
          "1178535,0,C17693,N302,C9006,N20516,6,6,595",
          "1178535,0,C17693,N302,C9006,N20517,6,6,595",
          "1178535,0,C17693,N302,C9006,N20524,6,6,595",
          "1178535,0,C17693,N302,C9006,N20525,6,6,595",
          "1178535,0,C17693,N302,C9006,N20687,6,6,595",
          "1178535,0,C17693,N302,C9006,N20688,6,6,595",
          "1178535,0,C17693,N302,C9006,N20694,6,6,675",
          "1178535,0,C17693,N302,C9006,N2298,6,6,691",
          "1178535,0,C17693,N302,C9006,N2533,6,6,595",
          "1178535,0,C17693,N302,C9006,N4188,6,7,641",
          "1178535,0,C17693,N302,C9006,N4712,6,6,595",
          "1178535,0,C17693,N302,C9006,N5581,6,6,595",
          "1178535,0,C17693,N302,C9006,N6239,6,6,595",
          "1178535,0,C17693,N302,C9006,N759,6,6,595",
          "1178536,0,C17693,N302,C9006,N13024,6,6,595",
          "1178536,0,C17693,N302,C9006,N6374,6,6,595",
          "1178537,0,C17693,N302,C9006,N19972,6,6,595",
          "1178538,0,C17693,N302,C9006,N5418,6,6,595",
          "1178539,0,C17693,N302,C9006,N6275,6,6,595",
          "1178540,0,C17693,N302,C9006,N13098,6,6,595",
          "1178540,0,C17693,N302,C9006,N13339,6,6,595",
          "1178540,0,C17693,N302,C9006,N13364,6,6,595",
          "1178540,0,C17693,N302,C9006,N20535,6,6,595",
          "1178540,0,C17693,N302,C9006,N5246,6,6,595",
          "1178540,0,C17693,N302,C9006,N5652,6,6,595",
          "1178540,0,C17693,N302,C9006,N864,6,6,675",
          "1178541,0,C17693,N302,C9006,N13353,6,6,595",
          "1178541,0,C17693,N302,C9006,N14389,6,6,595",
          "1178541,0,C17693,N302,C9006,N14395,6,6,595",
          "1178541,0,C17693,N302,C9006,N20526,6,6,595",
          "1178541,0,C17693,N302,C9006,N6763,6,6,595",
          "1178541,0,C17693,N302,C9006,N7459,6,6,595",
          "1178542,0,C17693,N302,C9006,N7747,6,6,595",
          "1178543,0,C17693,N302,C9006,N10660,6,6,595",
          "1178543,0,C17693,N302,C9006,N12047,6,6,595",
          "1178543,0,C17693,N302,C9006,N14809,6,6,595",
          "1178543,0,C17693,N302,C9006,N14810,6,6,595",
          "1178543,0,C17693,N302,C9006,N14905,6,6,595",
          "1178543,0,C17693,N302,C9006,N4332,6,6,595",
          "1178543,0,C17693,N302,C9006,N5226,6,6,595",
          "1178543,0,C17693,N302,C9006,N6287,6,6,595",
          "1178543,0,C17693,N302,C9006,N6292,6,6,675",
          "1178543,0,C17693,N302,C9006,N6293,6,7,641",
          "1178543,0,C17693,N302,C9006,N8397,6,6,595",
          "1178544,0,C17693,N302,C9006,N3704,6,6,595",
          "1178544,0,C17693,N302,C9006,N8058,6,6,595",
          "1178545,0,C17693,N302,C9006,N8065,6,6,595",
          "1178546,0,C17693,N302,C9006,N8555,6,6,595",
          "1178547,0,C17693,N302,C9006,N5272,6,6,595",
          "1178548,0,C17693,N302,C9006,N8556,6,6,595",
          "1178550,0,C17693,N302,C9006,N8552,6,6,595",
          "1178551,0,C17693,N302,C9006,N15158,6,6,595",
          "1178553,0,C17693,N302,C9006,N15140,6,6,595",
          "1178555,0,C17693,N302,C9006,N15145,6,6,595",
          "1178556,0,C17693,N302,C9006,N1310,6,6,595",
          "1178556,0,C17693,N302,C9006,N13401,6,6,595",
          "1178556,0,C17693,N302,C9006,N15157,6,6,595",
          "1178556,0,C17693,N302,C9006,N20538,6,6,675",
          "1178556,0,C17693,N302,C9006,N20725,6,6,595",
          "1178556,0,C17693,N302,C9006,N6961,6,6,595",
          "1178556,0,C17693,N302,C9006,N7299,6,6,595",
          "1178557,0,C17693,N302,C9006,N10501,6,6,595",
          "1178557,0,C17693,N302,C9006,N12401,6,6,595",
          "1178557,0,C17693,N302,C9006,N12852,6,6,595",
          "1178557,0,C17693,N302,C9006,N13676,6,6,595",
          "1178557,0,C17693,N302,C9006,N13989,6,142,205253",
          "1178557,0,C17693,N302,C9006,N15247,6,6,755",
          "1178557,0,C17693,N302,C9006,N15251,6,6,595"
  };



  @Test
  public void testknn() throws Exception {
    PipelineModel model = PipelineModel.load(modelOutput);
    Map<String, OnlineStatisticsProvider> context = new HashMap<>();
    for(String k : contextOutput.list()) {
      try(FileInputStream fis = new FileInputStream(new File(contextOutput, k))) {
        byte[] raw = IOUtils.toByteArray(fis);
        context.put(k, SerDeUtils.fromBytes(raw, OnlineStatisticsProvider.class));
      }
    }
    String bingo = "1179268,0,C17693,N302,C9006,N17478,6,6,595";
    RealVector bingoVec = getVector(bingo, model, context);
    DescriptiveStatistics closeStats = new DescriptiveStatistics();
    for(String close : expectedClose) {
      RealVector v = getVector(close, model, context);
      double dist = bingoVec.cosine(v);
      //System.out.println("close: cos(\n" + v + "\n" + bingoVec + "\n) = \n" + dist);
      closeStats.addValue(bingoVec.cosine(v));
    }
    DescriptiveStatistics kindacloseStats = new DescriptiveStatistics();
    for(String far : expectedkindaclose) {
      RealVector v = getVector(far, model, context);
      double dist = bingoVec.cosine(v);
      //System.out.println("far: cos(\n" + v + "\n" + bingoVec + "\n) = \n" + dist);
      kindacloseStats.addValue(bingoVec.cosine(v));
    }
    DescriptiveStatistics farStats = new DescriptiveStatistics();
    for(String far : expectedFar) {
      RealVector v = getVector(far, model, context);
      double dist = bingoVec.cosine(v);
      //System.out.println("far: cos(\n" + v + "\n" + bingoVec + "\n) = \n" + dist);
      farStats.addValue(bingoVec.cosine(v));
    }

    System.out.println("close stats: " + printStats(closeStats));
    System.out.println("kinda close stats: " + printStats(kindacloseStats));
    System.out.println("far stats: " + printStats(farStats));
  }
  private String printStats(DescriptiveStatistics stats) {
    List<String> buff = new ArrayList<>();
    buff.add("mean: " + stats.getMean());
    buff.add("stddev: " + Math.sqrt(stats.getVariance()));
    buff.add("median: " + stats.getPercentile(50));
    return Joiner.on(",").join(buff);
  }


  private RealVector getVector(String sentence, PipelineModel model, Map<String, OnlineStatisticsProvider> context) {
    LabeledDocument doc = SchemaUtil.INSTANCE.toSentence(sentence, schema, context, bins );
    Row r = model.transform(SchemaUtil.INSTANCE.toDF(sqlContext, doc)).collectAsList().get(0);
    DenseVector arr = (DenseVector)r.get(3);
    return new ArrayRealVector(arr.toArray());
  }


  @Test
  public void testClassifier() throws Exception {
    Map<String, List<String>> input = new HashMap<String, List<String>>() {{
      String base = "/Users/cstella/Documents/workspace/metron/fork/Fwaf-Machine-Learning-driven-Web-Application-Firewall";
      put("good", ImmutableList.of(base + "/goodqueries.txt"));
      put("bad", ImmutableList.of(base + "/badqueries.txt"));
    }};
    DataFrame allData = DatasetUtil.INSTANCE.mergeDatasets(sc, sqlContext, input);
    EnumMap<PipelineConfig, Optional<Object>> config = PipelineConfig.config(new HashMap<>());
    Pipeline pipeline = PipelineConfig.createPipeline(config, allData);
    DataFrame[] split = DatasetUtil.INSTANCE.split(allData, 0.7);
    PipelineModel model = PipelineConfig.train(split[0].cache(), pipeline);
    EnumMap<Evaluators, Object> evalResults = Evaluators.evaluatePipeline(split[1].cache(), model);
    for(Map.Entry<Evaluators, Object> kv : evalResults.entrySet()) {
      System.out.println(kv.getKey() + " => " + kv.getValue());
    }
  }
}
