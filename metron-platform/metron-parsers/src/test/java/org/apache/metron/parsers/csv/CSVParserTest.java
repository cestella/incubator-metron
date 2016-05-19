package org.apache.metron.parsers.csv;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.metron.common.configuration.SensorParserConfig;
import org.apache.metron.common.utils.JSONUtils;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CSVParserTest {
  /**
   {
    "parserClassName" : "org.apache.metron.parsers.csv.CSVParser"
   ,"sensorTopic":"dummy"
   ,"parserConfig":
   {
    "columns" : {
                "col1" : 0
               ,"col2" : 1
               ,"col3" : 2
                 }
   }
   }
   */
  @Multiline
  public static String parserConfig;

  @Test
  public void test() throws IOException {
    CSVParser parser = new CSVParser();

    SensorParserConfig config = JSONUtils.INSTANCE.load(parserConfig, SensorParserConfig.class);
    parser.init();
    parser.configure(config.getParserConfig());
    {
      String line = "#foo,bar,grok";
      Assert.assertEquals(0, parser.parse(Bytes.toBytes(line)).size());
    }
    {
      String line = "";
      Assert.assertEquals(0, parser.parse(Bytes.toBytes(line)).size());
    }
    {
      String line = "foo,bar,grok";
      List<JSONObject> results = parser.parse(Bytes.toBytes(line));
      Assert.assertEquals(1, results.size());
      JSONObject o = results.get(0);
      Assert.assertTrue(parser.validate(o));
      Assert.assertEquals(5, o.size());
      Assert.assertEquals("foo", o.get("col1"));
      Assert.assertEquals("bar", o.get("col2"));
      Assert.assertEquals("grok", o.get("col3"));
    }
    {
      String line = "\"foo\", \"bar\",\"grok\"";
      List<JSONObject> results = parser.parse(Bytes.toBytes(line));
      Assert.assertEquals(1, results.size());
      JSONObject o = results.get(0);
      Assert.assertTrue(parser.validate(o));
      Assert.assertEquals(5, o.size());
      Assert.assertEquals("foo", o.get("col1"));
      Assert.assertEquals("bar", o.get("col2"));
      Assert.assertEquals("grok", o.get("col3"));
    }
    {
      String line = "foo, bar, grok";
      List<JSONObject> results = parser.parse(Bytes.toBytes(line));
      Assert.assertEquals(1, results.size());
      JSONObject o = results.get(0);
      Assert.assertTrue(parser.validate(o));
      Assert.assertEquals(5, o.size());
      Assert.assertEquals("foo", o.get("col1"));
      Assert.assertEquals(" bar", o.get("col2"));
      Assert.assertEquals(" grok", o.get("col3"));
    }
  }
}
