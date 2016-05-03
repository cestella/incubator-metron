package org.apache.metron.threatintel.triage;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.metron.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class TriageProcessorTest {
  /**
   {
    "riskLevelRules" : {
        "user.type in [ 'admin', 'power' ] and asset.type == 'web'" : 10
       ,"user.type == 'normal'  and asset.type == 'web'" : 0
                          }
   ,"aggregator" : "MAX"
   }
   */
  @Multiline
  public static String processorConfig;

  @Test
  public void smokeTest() throws Exception {
    Processor processor = JSONUtils.INSTANCE.load(processorConfig, Processor.class);
    Assert.assertEquals("Expected a score of 10"
                       , 10d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "admin");
                          put("asset.type", "web");
                                        }}
                                        )
                       , 1e-10
                       );
    Assert.assertEquals("Expected a score of 0"
                       , 0d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "normal");
                          put("asset.type", "web");
                                        }}
                                        )
                       , 1e-10
                       );
    Assert.assertEquals("Expected a score of -1"
                       , -1d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "foo");
                          put("asset.type", "bar");
                                        }}
                                        )
                       , 1e-10
                       );
  }
}
