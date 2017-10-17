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
package org.apache.metron.elasticsearch.matcher;

import org.apache.metron.indexing.dao.search.SortField;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Assert;
import org.mockito.ArgumentMatcher;

public class SearchRequestMatcher extends ArgumentMatcher<SearchRequest> {

  private String[] expectedIndicies;
  private SearchSourceBuilder expectedSource;

  public SearchRequestMatcher(String[] indices, String query, int size, int from, SortField[] sortFields) {
    expectedIndicies = indices;
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .size(size)
            .from(from)
            .query(new QueryStringQueryBuilder(query))
            .fetchSource(true)
            .trackScores(true);
    for(SortField sortField: sortFields) {
      FieldSortBuilder fieldSortBuilder = new FieldSortBuilder(sortField.getField());
      fieldSortBuilder.order(sortField.getSortOrder() == org.apache.metron.indexing.dao.search.SortOrder.DESC ? SortOrder.DESC : SortOrder.ASC);
      searchSourceBuilder = searchSourceBuilder.sort(fieldSortBuilder);
    }
    expectedSource = searchSourceBuilder;
  }

  @Override
  public boolean matches(Object o) {
    SearchRequest searchRequest = (SearchRequest) o;
    Assert.assertArrayEquals("Indices did not match", expectedIndicies, searchRequest.indices());
    Assert.assertEquals("Source did not match", expectedSource, searchRequest.source());
    return true;
  }
}
