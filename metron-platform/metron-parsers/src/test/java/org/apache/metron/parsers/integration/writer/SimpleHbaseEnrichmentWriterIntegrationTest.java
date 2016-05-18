package org.apache.metron.parsers.integration.writer;

import org.apache.metron.TestConstants;
import org.apache.metron.common.Constants;
import org.apache.metron.integration.*;
import org.apache.metron.integration.components.ConfigUploadComponent;
import org.apache.metron.integration.components.KafkaWithZKComponent;
import org.apache.metron.integration.utils.TestUtils;
import org.apache.metron.parsers.integration.components.ParserTopologyComponent;
import org.apache.metron.test.TestDataType;
import org.apache.metron.test.utils.SampleDataUtils;
import org.apache.metron.test.utils.UnitTestHelper;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SimpleHbaseEnrichmentWriterIntegrationTest extends BaseIntegrationTest {
  @Test
  public void test() throws UnableToStartException, IOException {
    final String sensorType = "yaf";
    final List<byte[]> inputMessages = TestUtils.readSampleData(SampleDataUtils.getSampleDataPath(sensorType, TestDataType.RAW));

    final Properties topologyProperties = new Properties();
    final KafkaWithZKComponent kafkaComponent = getKafkaComponent(topologyProperties, new ArrayList<KafkaWithZKComponent.Topic>() {{
      add(new KafkaWithZKComponent.Topic(sensorType, 1));
    }});
    topologyProperties.setProperty("kafka.broker", kafkaComponent.getBrokerList());

    ConfigUploadComponent configUploadComponent = new ConfigUploadComponent()
            .withTopologyProperties(topologyProperties)
            .withGlobalConfigsPath(TestConstants.SAMPLE_CONFIG_PATH)
            .withParserConfigsPath(TestConstants.PARSER_CONFIGS_PATH);

    ParserTopologyComponent parserTopologyComponent = new ParserTopologyComponent.Builder()
            .withSensorType(sensorType)
            .withTopologyProperties(topologyProperties)
            .withBrokerUrl(kafkaComponent.getBrokerList()).build();

    UnitTestHelper.verboseLogging();
    ComponentRunner runner = new ComponentRunner.Builder()
            .withComponent("kafka", kafkaComponent)
            .withComponent("config", configUploadComponent)
            .withComponent("storm", parserTopologyComponent)
            .withMillisecondsBetweenAttempts(5000)
            .withNumRetries(10)
            .build();
    runner.start();
    kafkaComponent.writeMessages(sensorType, inputMessages);
    List<byte[]> outputMessages =
            runner.process(new Processor<List<byte[]>>() {
              List<byte[]> messages = null;

              public ReadinessState process(ComponentRunner runner) {
                /*KafkaWithZKComponent kafkaWithZKComponent = runner.getComponent("kafka", KafkaWithZKComponent.class);
                List<byte[]> outputMessages = kafkaWithZKComponent.readMessages(Constants.ENRICHMENT_TOPIC);
                if (outputMessages.size() == inputMessages.size()) {
                  messages = outputMessages;
                  return ReadinessState.READY;
                } else {
                  return ReadinessState.NOT_READY;
                }*/
                return ReadinessState.NOT_READY;
              }

              public List<byte[]> getResult() {
                return messages;
              }
            });
  }
}
