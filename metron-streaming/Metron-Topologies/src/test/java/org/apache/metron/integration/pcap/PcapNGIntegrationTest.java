package org.apache.metron.integration.pcap;

import com.google.common.base.Function;
import kafka.javaapi.producer.Producer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.metron.integration.util.UnitTestHelper;
import org.apache.metron.integration.util.integration.ComponentRunner;
import org.apache.metron.integration.util.integration.Processor;
import org.apache.metron.integration.util.integration.ReadinessState;
import org.apache.metron.integration.util.integration.components.FluxTopologyComponent;
import org.apache.metron.integration.util.integration.components.KafkaWithZKComponent;
import org.apache.metron.integration.util.integration.util.KafkaUtil;
import org.apache.metron.spout.pcap.HDFSWriterCallback;
import org.apache.metron.test.converters.HexStringConverter;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class PcapNGIntegrationTest {
    private String topologiesDir = "src/main/resources/Metron_Configs/topologies";
    private String targetDir = "target";
    private static File getOutDir(String targetDir) {
        File outDir = new File(new File(targetDir), "pcap_ng");
        if (!outDir.exists()) {
            outDir.mkdirs();
            outDir.deleteOnExit();
        }
        return outDir;
    }
    private static void clearOutDir(File outDir) {
        for(File f : outDir.listFiles()) {
            f.delete();
        }
    }
    private static int numFiles(File outDir) {
        return outDir.listFiles().length;
    }

    private static List<Map.Entry<byte[], byte[]>> readPcaps(File pcapFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(pcapFile));
        List<Map.Entry<byte[], byte[]> > ret = new ArrayList<>();
        HexStringConverter converter = new HexStringConverter();
        for(String line = null;(line = br.readLine()) != null;) {
            byte[] pcapWithHeader = converter.convert(line);
            long ts = System.currentTimeMillis();
            byte[] pcapRaw = new byte[pcapWithHeader.length - HDFSWriterCallback.PCAP_GLOBAL_HEADER.length];
            System.arraycopy(pcapWithHeader, HDFSWriterCallback.PCAP_GLOBAL_HEADER.length, pcapRaw, 0, pcapRaw.length);
            ret.add(new AbstractMap.SimpleImmutableEntry<>(Bytes.toBytes(ts), pcapRaw));
        }
        return ret;
    }

    @Test
    public void testTopology() throws Exception {
        if (!new File(topologiesDir).exists()) {
            topologiesDir = UnitTestHelper.findDir("topologies");
        }
        targetDir = UnitTestHelper.findDir("target");
        final String kafkaTopic = "pcap";
        final File outDir = getOutDir(targetDir);
        clearOutDir(outDir);
        Assert.assertEquals(0, numFiles(outDir));
        Assert.assertNotNull(topologiesDir);
        Assert.assertNotNull(targetDir);
        File pcapFile = new File(topologiesDir + "/../../SampleInput/PCAPExampleOutput");
        final List<Map.Entry<byte[], byte[]>> pcapEntries = readPcaps(pcapFile);
        final Properties topologyProperties = new Properties() {{
            setProperty("spout.kafka.topic.pcap", kafkaTopic);
            setProperty("kafka.pcap.out", outDir.getAbsolutePath());
            setProperty("kafka.pcap.numPackets", "2");
            setProperty("kafka.pcap.maxTimeMS", "200000000");
        }};
        final KafkaWithZKComponent kafkaComponent = new KafkaWithZKComponent().withTopics(new ArrayList<KafkaWithZKComponent.Topic>() {{
                                                                                            add(new KafkaWithZKComponent.Topic(kafkaTopic, 1));
                                                                                                                                      }})
                                                                              .withPostStartCallback(new Function<KafkaWithZKComponent, Void>() {
                                                                                  @Nullable
                                                                                  @Override
                                                                                  public Void apply(@Nullable KafkaWithZKComponent kafkaWithZKComponent) {
                                                                                      Producer<byte[], byte[]> producer = kafkaWithZKComponent.createProducer(byte[].class, byte[].class);
                                                                                      KafkaUtil.send(producer, pcapEntries, kafkaTopic);
                                                                                      topologyProperties.setProperty("kafka.zk", kafkaWithZKComponent.getZookeeperConnect());
                                                                                      return null;
                                                                                  }
                                                                              });



        FluxTopologyComponent fluxComponent = new FluxTopologyComponent.Builder()
                                                                       .withTopologyLocation(new File(topologiesDir + "/pcap_ng/remote.yaml"))
                                                                       .withTopologyName("pcap_ng")
                                                                       .withTopologyProperties(topologyProperties)
                                                                       .build();
        UnitTestHelper.verboseLogging();
        ComponentRunner runner = new ComponentRunner.Builder()
                                                    .withComponent("kafka", kafkaComponent)
                                                    .withComponent("storm", fluxComponent)
                                                    .build();
        runner.start();
        System.out.println("Components started...");
        fluxComponent.submitTopology();
        runner.process(new Processor<Void>() {
            @Override
            public ReadinessState process(ComponentRunner runner) {
                int numFiles = numFiles(outDir);
                int expectedNumFiles = pcapEntries.size()/2;
                if(numFiles == expectedNumFiles) {
                    return ReadinessState.READY;
                }
                else {
                    return ReadinessState.NOT_READY;
                }
            }

            @Override
            public Void getResult() {
                return null;
            }
        });
    }
}
