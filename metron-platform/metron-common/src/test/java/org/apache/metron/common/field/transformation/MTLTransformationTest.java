package org.apache.metron.common.field.transformation;

import com.google.common.collect.Iterables;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.metron.common.configuration.FieldTransformer;
import org.apache.metron.common.configuration.SensorParserConfig;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class MTLTransformationTest {
  /**
   {
    "fieldTransformations" : [
          {
           "transformation" : "MTL"
          ,"output" : "utc_timestamp"
          ,"config" : {
            "utc_timestamp" : "TO_TIMESTAMP(timestamp, 'yyyy-MM-dd HH:mm:ss', 'UTC')"
                      }
          }
                      ]
   }
   */
  @Multiline
  public static String mtlConfig;

  @Test
  public void testMTL() throws Exception {
    SensorParserConfig c = SensorParserConfig.fromBytes(Bytes.toBytes(mtlConfig));
    FieldTransformer handler = Iterables.getFirst(c.getFieldTransformations(), null);
    JSONObject input = new JSONObject(new HashMap<String, Object>() {{
      put("timestamp", "2016-01-05 17:02:30");
    }});
    handler.transformAndUpdate(input, new HashMap<>());
    long expected = 1452013350000L;
    Assert.assertEquals(expected, input.get("utc_timestamp"));
    Assert.assertTrue(input.containsKey("timestamp"));
  }
  @Test
  public void testMTL_negative() throws Exception {
    SensorParserConfig c = SensorParserConfig.fromBytes(Bytes.toBytes(mtlConfig));
    FieldTransformer handler = Iterables.getFirst(c.getFieldTransformations(), null);
    JSONObject input = new JSONObject(new HashMap<String, Object>() {{
    }});
    handler.transformAndUpdate(input, new HashMap<>());
    Assert.assertFalse(input.containsKey("utc_timestamp"));
    Assert.assertTrue(input.isEmpty());
  }

  /**
   {
    "fieldTransformations" : [
          {
           "transformation" : "MTL"
          ,"output" : [ "utc_timestamp", "url_host", "url_protocol" ]
          ,"config" : {
            "utc_timestamp" : "TO_TIMESTAMP(timestamp, 'yyyy-MM-dd HH:mm:ss', 'UTC')"
           ,"url_host" : "URL_TO_HOST(url)"
           ,"url_protocol" : "URL_TO_PROTOCOL(url)"
                      }
          }
                      ]
   }
   */
  @Multiline
  public static String mtlConfig_multi;

  @Test
  public void testMTL_multi() throws Exception {
    SensorParserConfig c = SensorParserConfig.fromBytes(Bytes.toBytes(mtlConfig_multi));
    FieldTransformer handler = Iterables.getFirst(c.getFieldTransformations(), null);
    JSONObject input = new JSONObject(new HashMap<String, Object>() {{
      put("timestamp", "2016-01-05 17:02:30");
      put("url", "https://caseystella.com/blog");
    }});
    handler.transformAndUpdate(input, new HashMap<>());
    long expected = 1452013350000L;
    Assert.assertEquals(expected, input.get("utc_timestamp"));
    Assert.assertEquals("caseystella.com", input.get("url_host"));
    Assert.assertEquals("https", input.get("url_protocol"));
    Assert.assertTrue(input.containsKey("timestamp"));
    Assert.assertTrue(input.containsKey("url"));
  }
}
