package org.apache.metron.common.stellar.maas;

import com.google.common.collect.ImmutableMap;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.StellarFunctions;
import org.apache.metron.common.stellar.StellarTest;
import org.apache.metron.maas.config.Endpoint;
import org.apache.metron.maas.config.ModelEndpoint;
import org.apache.metron.maas.discovery.ServiceDiscoverer;
import org.apache.metron.maas.util.RESTUtil;
import org.junit.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StellarMaaSIntegrationTest {
  private static Context context;
  private static TestingServer testZkServer;
  private static String zookeeperUrl;
  private static CuratorFramework client;
  private static ServiceDiscoverer discoverer;
  private static URL endpointUrl;

  @BeforeClass
  public static void setup() throws Exception {
    MockDGAModel.start(8282);
    testZkServer = new TestingServer(true);
    zookeeperUrl = testZkServer.getConnectString();
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    client = CuratorFrameworkFactory.newClient(zookeeperUrl, retryPolicy);
    client.start();
    context = new Context.Builder()
            .with(Context.Capabilities.ZOOKEEPER_CLIENT, () -> client)
            .build();
    StellarFunctions.FUNCTION_RESOLVER().initializeFunctions(context);
    discoverer = (ServiceDiscoverer) context.getCapability(Context.Capabilities.SERVICE_DISCOVERER).get();
    endpointUrl = new URL("http://localhost:8282");
    ModelEndpoint endpoint = new ModelEndpoint();
    {
      endpoint.setName("dga");
      endpoint.setContainerId("0");
      Endpoint ep = new Endpoint();
      ep.setUrl(endpointUrl.toString());
      endpoint.setEndpoint(ep);
      endpoint.setVersion("1.0");
    }
    ;

    ServiceInstanceBuilder<ModelEndpoint> builder = ServiceInstance.<ModelEndpoint>builder()
            .address(endpointUrl.getHost())
            .id("0")
            .name("dga")
            .port(endpointUrl.getPort())
            .registrationTimeUTC(System.currentTimeMillis())
            .serviceType(ServiceType.STATIC)
            .payload(endpoint);
    final ServiceInstance<ModelEndpoint> instance = builder.build();
    discoverer.getServiceDiscovery().registerService(instance);
    //wait til the endpoint is installed...
    for(int i = 0;i < 10;++i) {
      try {
        Object o = discoverer.getEndpoint("dga");
        if(o != null) {
          break;
        }
      }
      catch(Exception e) {

      }
      Thread.sleep(1000);
    }
  }

  @Test
  public void testGetEndpointWithoutVersion() throws Exception {
    String stellar = "MAAS_GET_ENDPOINT('dga')";
    Object result = StellarTest.run(stellar, new HashMap<>(), context);
    Assert.assertTrue(result instanceof Map);
    Map<String, String> resMap = (Map<String, String>)result;
    Assert.assertEquals(resMap.get("url"), "http://localhost:8282");
    Assert.assertEquals(resMap.get("name"), "dga");
    Assert.assertEquals(resMap.get("version"), "1.0");
    Assert.assertEquals(resMap.get("endpoint:apply"), "apply");

  }

  @Test
  public void testGetEndpointWithVersion() throws Exception {
    String stellar = "MAAS_GET_ENDPOINT('dga', '1.0')";
    Object result = StellarTest.run(stellar, new HashMap<>(), context);
    Assert.assertTrue(result instanceof Map);
    Map<String, String> resMap = (Map<String, String>)result;
    Assert.assertEquals(resMap.get("url"), "http://localhost:8282");
    Assert.assertEquals(resMap.get("name"), "dga");
    Assert.assertEquals(resMap.get("version"), "1.0");
    Assert.assertEquals(resMap.get("endpoint:apply"), "apply");
  }

  @Test
  public void testGetEndpointWithWrongVersion() throws Exception {
    String stellar = "MAAS_GET_ENDPOINT('dga', '2.0')";
    Object result = StellarTest.run(stellar, new HashMap<>(), context);
    Assert.assertNull(result);
  }

  @Test
  public void testModelApply() throws Exception {
    {
      String stellar = "MAP_GET('is_malicious', MODEL_APPLY(MAAS_GET_ENDPOINT('dga'), {'host': host}))";
      Object result = StellarTest.run(stellar, ImmutableMap.of("host", "badguy.com"), context);
      Assert.assertTrue((Boolean) result);
    }
    {
      String stellar = "MAP_GET('is_malicious', MODEL_APPLY(MAAS_GET_ENDPOINT('dga'), {'host': host}))";
      Object result = StellarTest.run(stellar, ImmutableMap.of("host", "youtube.com"), context);
      Assert.assertFalse((Boolean) result);
    }
  }
  @AfterClass
  public static void teardown() {
    MockDGAModel.shutdown();
    if(discoverer != null) {
      CloseableUtils.closeQuietly(discoverer);
    }
    if(client != null) {
      CloseableUtils.closeQuietly(client);
    }
    if(testZkServer != null) {
      CloseableUtils.closeQuietly(testZkServer);
    }
  }
}
