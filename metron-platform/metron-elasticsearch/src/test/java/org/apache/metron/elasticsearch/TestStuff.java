package org.apache.metron.elasticsearch;

import org.apache.metron.elasticsearch.utils.ElasticsearchClient;
import org.apache.metron.elasticsearch.utils.ElasticsearchUtils;
import org.apache.metron.elasticsearch.utils.FieldMapping;
import org.apache.metron.elasticsearch.utils.FieldProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestStuff {
  @Test
  public void test() throws Exception {
    ElasticsearchClient client = ElasticsearchUtils.getClient(new HashMap<String, Object>() {{
     put("es.ip", "node1:9200");
    }});
    for(String i : client.getIndices()) {
      System.out.println(i);
    }
  }

  @Test
  public void testOther() throws Exception {
    ElasticsearchClient client = ElasticsearchUtils.getClient(new HashMap<String, Object>() {{
     put("es.ip", "node1:9200");
    }});
    Map<String, FieldMapping> mappings = client.getMappings(new String[] { "bro_index_2018.07.11.18"});
    for(Map.Entry<String, FieldMapping> mkv : mappings.entrySet()) {
      System.out.println(mkv.getKey());
      for(Map.Entry<String, FieldProperties> field2Props : mkv.getValue().entrySet()) {
        System.out.println("\t" + field2Props.getKey());
        for(Map.Entry<String, Object> propKv : field2Props.getValue().entrySet()) {
          System.out.println("\t\t" + propKv.getKey() + " = " + propKv.getValue());
        }
      }
    }

  }
}
