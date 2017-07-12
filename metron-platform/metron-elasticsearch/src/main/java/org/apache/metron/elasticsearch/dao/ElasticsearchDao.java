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

package org.apache.metron.elasticsearch.dao;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.metron.common.Constants;
import org.apache.metron.common.configuration.writer.WriterConfiguration;
import org.apache.metron.elasticsearch.utils.ElasticsearchUtils;
import org.apache.metron.indexing.dao.Document;
import org.apache.metron.indexing.dao.IndexDao;
import org.apache.metron.indexing.dao.IndexUpdateCallback;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.Date;

public class ElasticsearchDao extends IndexDao {
  public static final String INDEX_TIMESTAMP = "_timestamp";

  private transient TransportClient client;
  public ElasticsearchDao(TransportClient client) {
    this(client, (x,y) -> {});
  }

  public ElasticsearchDao(TransportClient client, IndexUpdateCallback callback) {
    super(callback);
    this.client = client;
  }

  @Override
  public Document getLatest(String uuid, String sensorType) throws IOException {
    QueryBuilder query =  QueryBuilders.matchQuery(Constants.GUID, uuid);
    SearchRequestBuilder request = client.prepareSearch()
                                         .setTypes(sensorType + "_doc")
                                         .setQuery(query)
                                         .setSource("message")
                                         ;
    MultiSearchResponse response = client.prepareMultiSearch()
                                         .add(request)
                                         .get();
    //TODO: Fix this to
    //      * handle multiple responses
    //      * be more resilient to error
    for(MultiSearchResponse.Item i : response) {
      SearchResponse resp = i.getResponse();
      SearchHits hits = resp.getHits();
      for(SearchHit hit : hits) {
        SearchHitField tsField = hit.field(INDEX_TIMESTAMP);
        Long ts = tsField == null?0L:tsField.getValue();
        String doc = hit.getSourceAsString();
        String sourceType = Iterables.getFirst(Splitter.on("_doc").split(hit.getType()), null);
        Document d = new Document(doc, uuid, sourceType, ts);
        return d;
      }
    }
    return null;
  }

  @Override
  public void update(Document update, WriterConfiguration configurations) throws IOException {
    String indexPostfix = ElasticsearchUtils.getIndexFormat(configurations).format(new Date());
    String sensorType = update.getSensorType();
    String indexName = ElasticsearchUtils.getIndexName(sensorType, indexPostfix, configurations);
    IndexRequestBuilder indexRequestBuilder = client.prepareIndex(indexName,
            sensorType + "_doc");

    indexRequestBuilder = indexRequestBuilder.setSource(update.getDocument());
    Object ts = update.getTimestamp();
    if(ts != null) {
      indexRequestBuilder = indexRequestBuilder.setTimestamp(ts.toString());
    }

    BulkResponse bulkResponse = client.prepareBulk().add(indexRequestBuilder).execute().actionGet();
    if(bulkResponse.hasFailures()) {
      throw new IOException(bulkResponse.buildFailureMessage());
    }
    callback.postUpdate(this, update);
  }
}
