package org.apache.metron.elasticsearch.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.metron.common.Constants;
import org.apache.metron.common.configuration.writer.WriterConfiguration;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.elasticsearch.integration.components.ElasticSearchComponent;
import org.apache.metron.elasticsearch.writer.ElasticsearchWriter;
import org.apache.metron.test.mock.MockHTable;
import org.apache.metron.writer.dao.Document;
import org.apache.metron.writer.dao.MultiIndexDao;
import org.apache.metron.writer.dao.NoSqlDao;
import org.apache.metron.writer.mutation.Mutation;
import org.apache.metron.writer.mutation.MutationOperation;
import org.elasticsearch.action.bulk.BulkResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElasticsearchMutationIntegrationTest {
  private static final String SENSOR_NAME= "test";
  private static final String TABLE_NAME = "modifications";
  private static final String CF = "p";
  private static String indexDir = "target/elasticsearch_mutation";
  private static String dateFormat = "yyyy.MM.dd.HH";
  private static String index = SENSOR_NAME + "_index_" + new SimpleDateFormat(dateFormat).format(new Date());
  private static MockHTable table;
  private static MultiIndexDao dao;
  private static WriterConfiguration configurations;
  private static ElasticSearchComponent es;

  @BeforeClass
  public static void startHBase() throws Exception {
    Configuration config = HBaseConfiguration.create();
    MockHTable.Provider tableProvider = new MockHTable.Provider();
    tableProvider.addToCache(TABLE_NAME, CF);
    table = (MockHTable)tableProvider.getTable(config, TABLE_NAME);
    // setup the client
    es = new ElasticSearchComponent.Builder()
            .withHttpPort(9211)
            .withIndexDir(new File(indexDir))
            .build();
    es.start();

    NoSqlDao hbaseDao = new NoSqlDao(table, CF);
    ElasticsearchWriter esDao = new ElasticsearchWriter();
    esDao.init(new HashMap<String, Object>() {{
      put("es.clustername", "metron");
      put("es.port", "9300");
      put("es.ip", "localhost");
      put("es.date.format", dateFormat);
                }}
              );

    dao = new MultiIndexDao(hbaseDao, esDao);
    configurations = mock(WriterConfiguration.class);
    when(configurations.getIndex(any())).thenReturn(SENSOR_NAME);
  }

  @AfterClass
  public static void teardown() {
    es.stop();
  }



  @Test
  public void test() throws Exception {
    List<Map<String, Object>> inputData = new ArrayList<>();
    for(int i = 0; i < 10;++i) {
      final String name = "message" + i;
      inputData.add(
              new HashMap<String, Object>() {{
                put("source:type", SENSOR_NAME);
                put("name" , name);
                put("timestamp", System.currentTimeMillis());
                put(Constants.GUID, name);
              }}
                             );
    }
    BulkResponse response =
    es.add(index, SENSOR_NAME
          , Iterables.transform(inputData,
                    m -> {
                      try {
                        return JSONUtils.INSTANCE.toJSON(m, true);
                      } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                      }
                    }
                    )
    );
    List<Map<String,Object>> docs = null;
    for(int t = 0;t < 10;++t) {
      docs = es.getAllIndexedDocs(index, SENSOR_NAME + "_doc");
      if(docs.size() >= 10) {
        break;
      }
      Thread.sleep(1000);
    }
    Assert.assertEquals(10, docs.size());
    //modify the first message and add a new field
    Map<String, Object> message0 = new HashMap<String, Object>(inputData.get(0)) {{
      put("new-field", "metron");
    }};
    String message0Json = JSONUtils.INSTANCE.toJSON(message0, true);
    String uuid = "" + message0.get(Constants.GUID);
    Mutation mutation = Mutation.of(MutationOperation.REPLACE, message0Json);
    dao.update(uuid, SENSOR_NAME, mutation, Optional.empty(), configurations);
    Assert.assertEquals(1, table.size());
    Document doc = dao.getLatest(uuid, SENSOR_NAME);
    Assert.assertEquals(message0Json, doc.getDocument());
  }

}
