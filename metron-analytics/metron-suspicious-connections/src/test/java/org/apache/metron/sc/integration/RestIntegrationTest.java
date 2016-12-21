package org.apache.metron.sc.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.tdunning.math.stats.AVLTreeDigest;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.ClusterModel;
import org.apache.metron.sc.preprocessing.WordConfig;
import org.apache.metron.sc.rest.Runner;
import org.apache.metron.sc.rest.SuspiciousConnectionConfig;
import org.apache.metron.sc.training.TrainingCLI;
import org.apache.metron.sc.training.TrainingConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.metron.sc.integration.DNSIntegrationTest.getMessages;
import static org.apache.metron.sc.integration.DNSIntegrationTest.trainingConfig;
import static org.apache.metron.sc.integration.DNSIntegrationTest.wordConfig;

public class RestIntegrationTest {
  static SuspiciousConnectionConfig config = new SuspiciousConnectionConfig() {{
    setModelLocation("model.out");
  }};

  private String url;

  @ClassRule
  public static final DropwizardAppRule<SuspiciousConnectionConfig> RULE = new DropwizardAppRule<>(
          Runner.class,config
         );

  @BeforeClass
  public static void setUpClass() throws IOException {
    JavaSparkContext sc;
    SparkConf conf = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    conf.registerKryoClasses(new Class<?>[] {AVLTreeDigest.class});
    conf.set("spark.kryo.classesToRegister", AVLTreeDigest.class.getName());
    conf.set("spark.executor.extraJavaOptions", "-Xss16m");
    conf.set("spark.driver.extraJavaOptions", "-Xss16m");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
    //train model and save it off
    messages = getMessages();
    JavaRDD<String> messagesRdd = sc.parallelize(messages);
    WordConfig wordConfigObj = JSONUtils.INSTANCE.load(wordConfig, WordConfig.class);
    TrainingConfig trainingConfigObj = JSONUtils.INSTANCE.load(trainingConfig, TrainingConfig.class);
    ClusterModel model = TrainingCLI.createModel(trainingConfigObj, wordConfigObj, messagesRdd, sc);
    model.save(new File(config.getModelLocation()));
  }

  @Before
  public void setup() throws Exception {
    Map<String, Object> info = JSONUtils.INSTANCE.load("endpoint.dat", new TypeReference<Map<String, Object>>() {
    });
    url = "" + info.get("url");
    client = ClientBuilder.newClient();
  }
  private static List<String>  messages;
  private Client client;
  @Test
  public void test() throws Exception {
    Map<String, Object> message = JSONUtils.INSTANCE.load(messages.get(0), new TypeReference<Map<String, Object>>() {
    });
    final Map<String, Double> ret = client.target(url + "/apply")
            .queryParam("message", message)
            .queryParam("wordFields", ImmutableList.of("dns_qry_name"))
            .request()
            .get(Map.class);

    Assert.assertFalse(Double.isNaN(Double.parseDouble("" + ret.get("dns_qry_name"))));

  }
}
