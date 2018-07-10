package org.apache.metron.elasticsearch.utils;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Closeable;
import java.io.IOException;

public class ElasticsearchClient implements AutoCloseable{
  private RestClient lowLevelClient;
  private RestHighLevelClient highLevelClient;

  ElasticsearchClient(RestClient lowLevelClient, RestHighLevelClient highLevelClient) {
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
}
