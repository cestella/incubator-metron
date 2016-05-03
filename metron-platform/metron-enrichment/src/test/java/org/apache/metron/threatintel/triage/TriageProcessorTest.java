/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
       ,"asset.type == 'web'" : 5
       ,"user.type == 'normal'  and asset.type == 'web'" : 0
                          }
   ,"aggregator" : "MAX"
   }
   */
  @Multiline
  public static String smokeTestProcessorConfig;

  @Test
  public void smokeTest() throws Exception {
    Processor processor = JSONUtils.INSTANCE.load(smokeTestProcessorConfig, Processor.class);
    Assert.assertEquals("Expected a score of 10"
                       , 10d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "admin");
                          put("asset.type", "web");
                                        }}
                                        )
                       , 1e-10
                       );
    Assert.assertEquals("Expected a score of 5"
                       , 5d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "normal");
                          put("asset.type", "web");
                                        }}
                                        )
                       , 1e-10
                       );
    Assert.assertEquals("Expected a score of 0"
                       , 0d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "foo");
                          put("asset.type", "bar");
                                        }}
                                        )
                       , 1e-10
                       );
  }

  /**
   {
    "riskLevelRules" : {
        "user.type in [ 'admin', 'power' ] and asset.type == 'web'" : 10
       ,"asset.type == 'web'" : 5
       ,"user.type == 'normal'  and asset.type == 'web'" : 0
                          }
   ,"aggregator" : "POSITIVE_MEAN"
   }
   */
  @Multiline
  public static String positiveMeanProcessorConfig;

  @Test
  public void positiveMeanAggregationTest() throws Exception {

    Processor processor = JSONUtils.INSTANCE.load(positiveMeanProcessorConfig, Processor.class);
    Assert.assertEquals("Expected a score of 0"
                       , 5d
                       , processor.apply( new HashMap<Object, Object>() {{
                          put("user.type", "normal");
                          put("asset.type", "web");
                                        }}
                                        )
                       , 1e-10
                       );
    Assert.assertEquals("Expected a score of 7.5"
                       , (10 + 5)/2.0
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
                          put("user.type", "foo");
                          put("asset.type", "bar");
                                        }}
                                        )
                       , 1e-10
                       );
  }
}
