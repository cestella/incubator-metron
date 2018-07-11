package org.apache.metron.elasticsearch.utils;

import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

public class ElasticsearchClient implements AutoCloseable{
  private RestClient lowLevelClient;
  private RestHighLevelClient highLevelClient;

  public ElasticsearchClient(RestClient lowLevelClient, RestHighLevelClient highLevelClient) {
    this.lowLevelClient = lowLevelClient;
    this.highLevelClient = highLevelClient;
  }

  public RestClient getLowLevelClient() {
    return lowLevelClient;
  }

  public RestHighLevelClient getHighLevelClient() {
    return highLevelClient;
  }

  @Override
  public void close() throws IOException {
    if(lowLevelClient != null) {
      lowLevelClient.close();
    }
  }

  public void putMapping(String index, String type, String source) {

    /**
     * ((ElasticsearchDao) esDao).getClient().admin().indices().preparePutMapping(INDEX)
            .setType("test_doc")
            .setSource(nestedAlertMapping)
            .get();
     */
  }

  public String[] getIndices() throws IOException {
    Response response = lowLevelClient.performRequest("GET", "/_cat/indices");
    /*
    String[] indices = adminClient
            .indices()
            .prepareGetIndex()
            .setFeatures()
            .get()
            .getIndices();
            */
    return null;
  }

  public ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> getMappings(String[] indices) {
    /*
    ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = adminClient
            .indices()
            .getMappings(new GetMappingsRequest().indices(latestIndices))
            .actionGet()
            .getMappings();
            */
    return null;
  }


}
