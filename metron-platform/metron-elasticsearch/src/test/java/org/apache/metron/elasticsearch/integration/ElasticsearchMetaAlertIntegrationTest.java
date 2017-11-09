/*
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

package org.apache.metron.elasticsearch.integration;

import static org.apache.metron.indexing.dao.MetaAlertDao.ALERT_FIELD;
import static org.apache.metron.indexing.dao.MetaAlertDao.METAALERTS_INDEX;
import static org.apache.metron.indexing.dao.MetaAlertDao.METAALERT_FIELD;
import static org.apache.metron.indexing.dao.MetaAlertDao.METAALERT_TYPE;
import static org.apache.metron.indexing.dao.MetaAlertDao.STATUS_FIELD;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.metron.common.Constants;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.elasticsearch.dao.ElasticsearchDao;
import org.apache.metron.elasticsearch.dao.ElasticsearchMetaAlertDao;
import org.apache.metron.indexing.dao.metaalert.MetaAlertStatus;
import org.apache.metron.elasticsearch.integration.components.ElasticSearchComponent;
import org.apache.metron.indexing.dao.AccessConfig;
import org.apache.metron.indexing.dao.IndexDao;
import org.apache.metron.indexing.dao.MetaAlertDao;
import org.apache.metron.indexing.dao.metaalert.MetaAlertCreateRequest;
import org.apache.metron.indexing.dao.metaalert.MetaAlertCreateResponse;
import org.apache.metron.indexing.dao.search.Group;
import org.apache.metron.indexing.dao.search.GroupRequest;
import org.apache.metron.indexing.dao.search.GroupResponse;
import org.apache.metron.indexing.dao.search.GroupResult;
import org.apache.metron.indexing.dao.search.InvalidSearchException;
import org.apache.metron.indexing.dao.search.SearchRequest;
import org.apache.metron.indexing.dao.search.SearchResponse;
import org.apache.metron.indexing.dao.search.SearchResult;
import org.apache.metron.indexing.dao.search.SortField;
import org.apache.metron.indexing.dao.update.Document;
import org.apache.metron.indexing.dao.update.OriginalNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ElasticsearchMetaAlertIntegrationTest {

  private static final int MAX_RETRIES = 10;
  private static final int SLEEP_MS = 500;
  private static final String SENSOR_NAME = "test";
  private static final String INDEX_DIR = "target/elasticsearch_meta";
  private static final String DATE_FORMAT = "yyyy.MM.dd.HH";
  private static final String INDEX =
      SENSOR_NAME + "_index_" + new SimpleDateFormat(DATE_FORMAT).format(new Date());
  private static final String NEW_FIELD = "new-field";

  private static IndexDao esDao;
  private static MetaAlertDao metaDao;
  private static ElasticSearchComponent es;

  /**
   {
     "properties": {
       "alert": {
         "type": "nested"
       }
     }
   }
   */
  @Multiline
  public static String nestedAlertMapping;

  @BeforeClass
  public static void setupBefore() throws Exception {
    // setup the client
    es = new ElasticSearchComponent.Builder()
        .withHttpPort(9211)
        .withIndexDir(new File(INDEX_DIR))
        .build();
    es.start();

    AccessConfig accessConfig = new AccessConfig();
    Map<String, Object> globalConfig = new HashMap<String, Object>() {
      {
        put("es.clustername", "metron");
        put("es.port", "9300");
        put("es.ip", "localhost");
        put("es.date.format", DATE_FORMAT);
      }
    };
    accessConfig.setMaxSearchResults(1000);
    accessConfig.setGlobalConfigSupplier(() -> globalConfig);
    accessConfig.setMaxSearchGroups(100);

    esDao = new ElasticsearchDao();
    esDao.init(accessConfig);
    metaDao = new ElasticsearchMetaAlertDao(esDao);
  }

  @Before
  public void setup() throws IOException {
    es.createIndexWithMapping(METAALERTS_INDEX, MetaAlertDao.METAALERT_DOC,
        buildMetaMappingSource());
  }

  @AfterClass
  public static void teardown() {
    if (es != null) {
      es.stop();
    }
  }

  @After
  public void reset() {
    es.reset();
  }

  protected static String buildMetaMappingSource() throws IOException {
    return jsonBuilder().prettyPrint()
        .startObject()
        .startObject(MetaAlertDao.METAALERT_DOC)
        .startObject("properties")
        .startObject("guid")
        .field("type", "string")
        .field("index", "not_analyzed")
        .endObject()
        .startObject("score")
        .field("type", "integer")
        .field("index", "not_analyzed")
        .endObject()
        .startObject("alert")
        .field("type", "nested")
        .endObject()
        .endObject()
        .endObject()
        .endObject()
        .string();
  }

  @Test
  public void shouldGetAllMetaAlertsForAlert() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(3);
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Load metaAlerts
    Map<String, Object> activeMetaAlert0 = buildMetaAlert("meta_active_0", MetaAlertStatus.ACTIVE,
        Optional.of(Arrays.asList(alerts.get(0), alerts.get(2))));
    Map<String, Object> activeMetaAlert1 = buildMetaAlert("meta_active_1", MetaAlertStatus.ACTIVE,
        Optional.of(Collections.emptyList()));
    Map<String, Object> inactiveMetaAlert = buildMetaAlert("meta_inactive", MetaAlertStatus.INACTIVE,
        Optional.of(Arrays.asList(alerts.get(0), alerts.get(2))));
    // We pass MetaAlertDao.METAALERT_TYPE, because the "_doc" gets appended automatically.
    elasticsearchAdd(Arrays.asList(activeMetaAlert0, activeMetaAlert1, inactiveMetaAlert), METAALERTS_INDEX, MetaAlertDao.METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "message_2", "meta_active_0", "meta_active_1", "meta_inactive"),
        Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    {
      SearchResponse searchResponse0 = metaDao.getAllMetaAlertsForAlert("message_0");
      List<SearchResult> searchResults0 = searchResponse0.getResults();
      Assert.assertEquals(1, searchResults0.size());
      Assert.assertEquals(activeMetaAlert0, searchResults0.get(0).getSource());

      SearchResponse searchResponse1 = metaDao.getAllMetaAlertsForAlert("message_1");
      List<SearchResult> searchResults1 = searchResponse1.getResults();
      Assert.assertEquals(0, searchResults1.size());

      SearchResponse searchResponse2 = metaDao.getAllMetaAlertsForAlert("message_2");
      List<SearchResult> searchResults2 = searchResponse2.getResults();
      Assert.assertEquals(1, searchResults2.size());
      Assert.assertEquals(activeMetaAlert0, searchResults2.get(0).getSource());
    }
  }

  @Test
  public void getAllMetaAlertsForAlertShouldThrowExceptionForEmtpyGuid() throws Exception {
    try {
      metaDao.getAllMetaAlertsForAlert("");
      Assert.fail("An exception should be thrown for empty guid");
    } catch (InvalidSearchException ise) {
      Assert.assertEquals("Guid cannot be empty", ise.getMessage());
    }
  }

  @Test
  public void shouldCreateMetaAlert() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(3);
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "message_2"),
        Collections.singletonList(SENSOR_NAME));

    {
      MetaAlertCreateRequest metaAlertCreateRequest = new MetaAlertCreateRequest() {{
        setGuidToIndices(new HashMap<String, String>() {{
          put("message_1", INDEX);
          put("message_2", INDEX);
        }});
        setGroups(Collections.singletonList("group"));
      }};
      MetaAlertCreateResponse metaAlertCreateResponse = metaDao.createMetaAlert(metaAlertCreateRequest);
      {
        // Verify metaAlert was created
        findCreatedDoc(metaAlertCreateResponse.getGuid(), MetaAlertDao.METAALERT_TYPE);
      }
      {
        // Verify alert 0 was not updated with metaalert field
        Document alert = metaDao.getLatest("message_0", SENSOR_NAME);
        Assert.assertEquals(4, alert.getDocument().size());
        Assert.assertNull(alert.getDocument().get(METAALERT_FIELD));
      }
      {
        // Verify alert 1 was properly updated with metaalert field
        Document alert = metaDao.getLatest("message_1", SENSOR_NAME);
        Assert.assertEquals(5, alert.getDocument().size());
        Assert.assertEquals(1, ((List) alert.getDocument().get(METAALERT_FIELD)).size());
        Assert.assertEquals(metaAlertCreateResponse.getGuid(), ((List) alert.getDocument().get(METAALERT_FIELD)).get(0));
      }
      {
        // Verify alert 2 was properly updated with metaalert field
        Document alert = metaDao.getLatest("message_2", SENSOR_NAME);
        Assert.assertEquals(5, alert.getDocument().size());
        Assert.assertEquals(1, ((List) alert.getDocument().get(METAALERT_FIELD)).size());
        Assert.assertEquals(metaAlertCreateResponse.getGuid(), ((List) alert.getDocument().get(METAALERT_FIELD)).get(0));
      }
    }
  }

  @Test
  public void shouldAddAlertsToMetaAlert() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(4);
    alerts.get(0).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Load metaAlert
    Map<String, Object> metaAlert = buildMetaAlert("meta_alert", MetaAlertStatus.ACTIVE,
        Optional.of(Collections.singletonList(alerts.get(0))));
    elasticsearchAdd(Collections.singletonList(metaAlert), METAALERTS_INDEX, METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "message_2", "message_3","meta_alert"), Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    // Build expected metaAlert after alerts are added
    Map<String, Object> expectedMetaAlert = new HashMap<>(metaAlert);

    // Verify the proper alerts were added
    List<Map<String, Object>> metaAlertAlerts = new ArrayList<>((List<Map<String, Object>>) expectedMetaAlert.get(ALERT_FIELD));
    Map<String, Object> expectedAlert0 = alerts.get(0);
    Map<String, Object> expectedAlert1 = alerts.get(1);
    expectedAlert1.put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    metaAlertAlerts.add(expectedAlert1);
    Map<String, Object> expectedAlert2 = alerts.get(2);
    expectedAlert2.put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    metaAlertAlerts.add(expectedAlert2);
    expectedMetaAlert.put(ALERT_FIELD, metaAlertAlerts);

    // Verify the counts were properly updated
    expectedMetaAlert.put("average", 1.0d);
    expectedMetaAlert.put("min", 0.0d);
    expectedMetaAlert.put("median", 1.0d);
    expectedMetaAlert.put("max", 2.0d);
    expectedMetaAlert.put("count", 3);
    expectedMetaAlert.put("sum", 3.0d);
    expectedMetaAlert.put("threat:triage:score", 3.0d);

    // Add a list of new alerts
    {
      Assert.assertTrue(metaDao.addAlertsToMetaAlert("meta_alert", Arrays.asList("message_1", "message_2"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

    // Add a list of alerts that are already in the metaAlert
    {
      Assert.assertFalse(metaDao.addAlertsToMetaAlert("meta_alert", Arrays.asList("message_0", "message_1"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

    // Add a list of alerts where one item in the list is present in the metaAlert
    {
      metaAlertAlerts = new ArrayList<>((List<Map<String, Object>>) expectedMetaAlert.get(ALERT_FIELD));
      Map<String, Object> expectedAlert3 = alerts.get(3);
      expectedAlert3.put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
      metaAlertAlerts.add(expectedAlert3);
      expectedMetaAlert.put(ALERT_FIELD, metaAlertAlerts);

      expectedMetaAlert.put("average", 1.5d);
      expectedMetaAlert.put("min", 0.0d);
      expectedMetaAlert.put("median", 1.5d);
      expectedMetaAlert.put("max", 3.0d);
      expectedMetaAlert.put("count", 4);
      expectedMetaAlert.put("sum", 6.0d);
      expectedMetaAlert.put("threat:triage:score", 6.0d);

      Assert.assertTrue(metaDao.addAlertsToMetaAlert("meta_alert", Arrays.asList("message_2", "message_3"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

  }

  @Test
  public void shouldRemoveAlertsFromMetaAlert() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(4);
    alerts.get(0).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    alerts.get(1).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    alerts.get(2).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    alerts.get(3).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Load metaAlert
    Map<String, Object> metaAlert = buildMetaAlert("meta_alert", MetaAlertStatus.ACTIVE,
        Optional.of(Arrays.asList(alerts.get(0), alerts.get(1), alerts.get(2), alerts.get(3))));
    elasticsearchAdd(Collections.singletonList(metaAlert), METAALERTS_INDEX, METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "message_2", "message_3", "meta_alert"), Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    // Build expected metaAlert after alerts are added
    Map<String, Object> expectedMetaAlert = new HashMap<>(metaAlert);

    // Verify the proper alerts were added
    List<Map<String, Object>> metaAlertAlerts = new ArrayList<>((List<Map<String, Object>>) expectedMetaAlert.get(ALERT_FIELD));
    metaAlertAlerts.remove(0);
    metaAlertAlerts.remove(0);
    expectedMetaAlert.put(ALERT_FIELD, metaAlertAlerts);

    // Verify the counts were properly updated
    expectedMetaAlert.put("average", 2.5d);
    expectedMetaAlert.put("min", 2.0d);
    expectedMetaAlert.put("median", 2.5d);
    expectedMetaAlert.put("max", 3.0d);
    expectedMetaAlert.put("count", 2);
    expectedMetaAlert.put("sum", 5.0d);
    expectedMetaAlert.put("threat:triage:score", 5.0d);

    // Remove a list of alerts
    {
      Assert.assertTrue(metaDao.removeAlertsFromMetaAlert("meta_alert", Arrays.asList("message_0", "message_1"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

    // Remove a list of alerts that are not present in the metaAlert
    {
      Assert.assertFalse(metaDao.removeAlertsFromMetaAlert("meta_alert", Arrays.asList("message_0", "message_1"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

    // Remove a list of alerts where one item in the list is not present in the metaAlert
    {
      metaAlertAlerts = new ArrayList<>((List<Map<String, Object>>) expectedMetaAlert.get(ALERT_FIELD));
      metaAlertAlerts.remove(0);
      expectedMetaAlert.put(ALERT_FIELD, metaAlertAlerts);

      expectedMetaAlert.put("average", 3.0d);
      expectedMetaAlert.put("min", 3.0d);
      expectedMetaAlert.put("median", 3.0d);
      expectedMetaAlert.put("max", 3.0d);
      expectedMetaAlert.put("count", 1);
      expectedMetaAlert.put("sum", 3.0d);
      expectedMetaAlert.put("threat:triage:score", 3.0d);

      Assert.assertTrue(metaDao.removeAlertsFromMetaAlert("meta_alert", Arrays.asList("message_0", "message_2"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

    // Remove all alerts from a metaAlert
    {
      metaAlertAlerts = new ArrayList<>((List<Map<String, Object>>) expectedMetaAlert.get(ALERT_FIELD));
      metaAlertAlerts.remove(0);
      expectedMetaAlert.put(ALERT_FIELD, metaAlertAlerts);

      expectedMetaAlert.put("average", 0.0d);
      expectedMetaAlert.put("min", "Infinity");
      expectedMetaAlert.put("median", "NaN");
      expectedMetaAlert.put("max", "-Infinity");
      expectedMetaAlert.put("count", 0);
      expectedMetaAlert.put("sum", 0.0d);
      expectedMetaAlert.put("threat:triage:score", 0.0d);

      Assert.assertTrue(metaDao.removeAlertsFromMetaAlert("meta_alert",
          Collections.singletonList("message_3"), Collections.singleton(SENSOR_NAME)));
      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
    }

  }

  @Test
  public void addRemoveAlertsShouldThrowExceptionForInactiveMetaAlert() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(2);
    alerts.get(0).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Load metaAlert
    Map<String, Object> metaAlert = buildMetaAlert("meta_alert", MetaAlertStatus.INACTIVE,
        Optional.of(Collections.singletonList(alerts.get(0))));
    elasticsearchAdd(Collections.singletonList(metaAlert), METAALERTS_INDEX, METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "meta_alert"), Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    // Verify alerts cannot be added to an INACTIVE meta alert
    {
      try {
        metaDao.addAlertsToMetaAlert("meta_alert",
            Collections.singletonList("message_1"), Collections.singletonList(SENSOR_NAME));
        Assert.fail("Adding alerts to an inactive meta alert should throw an exception");
      } catch (IllegalStateException ise) {
        Assert.assertEquals("Adding alerts to an INACTIVE meta alert is not allowed", ise.getMessage());
      }
    }

    // Verify alerts cannot be removed from an INACTIVE meta alert
    {
      try {
        metaDao.removeAlertsFromMetaAlert("meta_alert",
            Collections.singletonList("message_0"), Collections.singletonList(SENSOR_NAME));
        Assert.fail("Removing alerts from an inactive meta alert should throw an exception");
      } catch (IllegalStateException ise) {
        Assert.assertEquals("Removing alerts from an INACTIVE meta alert is not allowed", ise.getMessage());
      }
    }
  }

  @Test
  public void shouldUpdateMetaAlertStatus() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(3);
    alerts.get(0).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    alerts.get(1).put(METAALERT_FIELD, Collections.singletonList("meta_alert"));
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Load metaAlerts
    Map<String, Object> metaAlert = buildMetaAlert("meta_alert", MetaAlertStatus.ACTIVE,
        Optional.of(Arrays.asList(alerts.get(0), alerts.get(1))));
    // We pass MetaAlertDao.METAALERT_TYPE, because the "_doc" gets appended automatically.
    elasticsearchAdd(Collections.singletonList(metaAlert), METAALERTS_INDEX, MetaAlertDao.METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "message_2", "meta_alert"),
        Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    // Verify status changed to inactive and child alerts are updated
    {
      Assert.assertTrue(metaDao.updateMetaAlertStatus("meta_alert", MetaAlertStatus.INACTIVE));

      Map<String, Object> expectedMetaAlert = new HashMap<>(metaAlert);
      expectedMetaAlert.put(STATUS_FIELD, MetaAlertStatus.INACTIVE.getStatusString());

      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);

      Map<String, Object> expectedAlert0 = new HashMap<>(alerts.get(0));
      expectedAlert0.put("metaalerts", new ArrayList());
      findUpdatedDoc(expectedAlert0, "message_0", SENSOR_NAME);

      Map<String, Object> expectedAlert1 = new HashMap<>(alerts.get(1));
      expectedAlert1.put("metaalerts", new ArrayList());
      findUpdatedDoc(expectedAlert1, "message_1", SENSOR_NAME);

      Map<String, Object> expectedAlert2 = new HashMap<>(alerts.get(2));
      findUpdatedDoc(expectedAlert2, "message_2", SENSOR_NAME);
    }

    // Verify status changed to active and child alerts are updated
    {
      Assert.assertTrue(metaDao.updateMetaAlertStatus("meta_alert", MetaAlertStatus.ACTIVE));

      Map<String, Object> expectedMetaAlert = new HashMap<>(metaAlert);
      expectedMetaAlert.put(STATUS_FIELD, MetaAlertStatus.ACTIVE.getStatusString());

      findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);

      Map<String, Object> expectedAlert0 = new HashMap<>(alerts.get(0));
      expectedAlert0.put("metaalerts", Collections.singletonList("meta_alert"));
      findUpdatedDoc(expectedAlert0, "message_0", SENSOR_NAME);

      Map<String, Object> expectedAlert1 = new HashMap<>(alerts.get(1));
      expectedAlert1.put("metaalerts", Collections.singletonList("meta_alert"));
      findUpdatedDoc(expectedAlert1, "message_1", SENSOR_NAME);

      Map<String, Object> expectedAlert2 = new HashMap<>(alerts.get(2));
      findUpdatedDoc(expectedAlert2, "message_2", SENSOR_NAME);

      // Verify status changed to current status has no effect
      {
        Assert.assertFalse(metaDao.updateMetaAlertStatus("meta_alert", MetaAlertStatus.ACTIVE));

        findUpdatedDoc(expectedMetaAlert, "meta_alert", METAALERT_TYPE);
        findUpdatedDoc(expectedAlert0, "message_0", SENSOR_NAME);
        findUpdatedDoc(expectedAlert1, "message_1", SENSOR_NAME);
        findUpdatedDoc(expectedAlert2, "message_2", SENSOR_NAME);
      }
    }
  }

  @Test
  public void shouldSearchByStatus() throws Exception {
    // Load metaAlerts
    Map<String, Object> activeMetaAlert = buildMetaAlert("meta_active", MetaAlertStatus.ACTIVE,
        Optional.empty());
    Map<String, Object> inactiveMetaAlert = buildMetaAlert("meta_inactive", MetaAlertStatus.INACTIVE,
        Optional.empty());


    // We pass MetaAlertDao.METAALERT_TYPE, because the "_doc" gets appended automatically.
    elasticsearchAdd(Arrays.asList(activeMetaAlert, inactiveMetaAlert), METAALERTS_INDEX, MetaAlertDao.METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("meta_active", "meta_inactive"), Arrays.asList(METAALERT_TYPE));

    SearchResponse searchResponse = metaDao.search(new SearchRequest() {
      {
        setQuery("*");
        setIndices(Collections.singletonList(MetaAlertDao.METAALERT_TYPE));
        setFrom(0);
        setSize(5);
        setSort(Collections.singletonList(new SortField() {{
          setField(Constants.GUID);
        }}));
      }
    });
    Assert.assertEquals(1, searchResponse.getTotal());
    Assert.assertEquals(MetaAlertStatus.ACTIVE.getStatusString(),
        searchResponse.getResults().get(0).getSource().get(MetaAlertDao.STATUS_FIELD));
  }


  @Test
  public void shouldSearchByNestedAlert() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(4);
    alerts.get(0).put(METAALERT_FIELD, Collections.singletonList("meta_active"));
    alerts.get(0).put("ip_src_addr", "192.168.1.1");
    alerts.get(0).put("ip_src_port", 8010);
    alerts.get(1).put(METAALERT_FIELD, Collections.singletonList("meta_active"));
    alerts.get(1).put("ip_src_addr", "192.168.1.2");
    alerts.get(1).put("ip_src_port", 8009);
    alerts.get(2).put("ip_src_addr", "192.168.1.3");
    alerts.get(2).put("ip_src_port", 8008);
    alerts.get(3).put("ip_src_addr", "192.168.1.4");
    alerts.get(3).put("ip_src_port", 8007);
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Put the nested type into the test index, so that it'll match appropriately
    ((ElasticsearchDao) esDao).getClient().admin().indices().preparePutMapping(INDEX)
        .setType("test_doc")
        .setSource(nestedAlertMapping)
        .get();

    // Load metaAlerts
    Map<String, Object> activeMetaAlert = buildMetaAlert("meta_active", MetaAlertStatus.ACTIVE,
        Optional.of(Arrays.asList(alerts.get(0), alerts.get(1))));
    Map<String, Object> inactiveMetaAlert = buildMetaAlert("meta_inactive", MetaAlertStatus.INACTIVE,
        Optional.of(Arrays.asList(alerts.get(2), alerts.get(3))));
    // We pass MetaAlertDao.METAALERT_TYPE, because the "_doc" gets appended automatically.
    elasticsearchAdd(Arrays.asList(activeMetaAlert, inactiveMetaAlert), METAALERTS_INDEX, MetaAlertDao.METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "message_2", "message_3", "meta_active", "meta_inactive"),
        Arrays.asList(SENSOR_NAME, METAALERT_TYPE));


    SearchResponse searchResponse = metaDao.search(new SearchRequest() {
      {
        setQuery(
            "(ip_src_addr:192.168.1.1 AND ip_src_port:8009) OR (alert.ip_src_addr:192.168.1.1 AND alert.ip_src_port:8009)");
        setIndices(Collections.singletonList(MetaAlertDao.METAALERT_TYPE));
        setFrom(0);
        setSize(5);
        setSort(Collections.singletonList(new SortField() {
          {
            setField(Constants.GUID);
          }
        }));
      }
    });
    // Should not have results because nested alerts shouldn't be flattened
    Assert.assertEquals(0, searchResponse.getTotal());

    // Query against all indices. Only the single active meta alert should be returned.
    // The child alerts should be hidden.
    searchResponse = metaDao.search(new SearchRequest() {
      {
        setQuery(
            "(ip_src_addr:192.168.1.1 AND ip_src_port:8010)"
                + " OR (alert.ip_src_addr:192.168.1.1 AND alert.ip_src_port:8010)");
        setIndices(Collections.singletonList("*"));
        setFrom(0);
        setSize(5);
        setSort(Collections.singletonList(new SortField() {
          {
            setField(Constants.GUID);
          }
        }));
      }
    });

    // Nested query should match a nested alert
    Assert.assertEquals(1, searchResponse.getTotal());
    Assert.assertEquals("meta_active",
        searchResponse.getResults().get(0).getSource().get("guid"));

    // Query against all indices. The child alert has no actual attached meta alerts, and should
    // be returned on its own.
    searchResponse = metaDao.search(new SearchRequest() {
      {
        setQuery(
            "(ip_src_addr:192.168.1.3 AND ip_src_port:8008)"
                + " OR (alert.ip_src_addr:192.168.1.3 AND alert.ip_src_port:8008)");
        setIndices(Collections.singletonList("*"));
        setFrom(0);
        setSize(5);
        setSort(Collections.singletonList(new SortField() {
          {
            setField(Constants.GUID);
          }
        }));
      }
    });

    // Nested query should match a plain alert
    Assert.assertEquals(1, searchResponse.getTotal());
    Assert.assertEquals("message_2",
        searchResponse.getResults().get(0).getSource().get("guid"));
  }

  @Test
  public void shouldHidesAlertsOnGroup() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(2);
    alerts.get(0).put(METAALERT_FIELD, Collections.singletonList("meta_active"));
    alerts.get(0).put("ip_src_addr", "192.168.1.1");
    alerts.get(0).put("score_field", 1);
    alerts.get(1).put("ip_src_addr", "192.168.1.1");
    alerts.get(1).put("score_field", 10);
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);


    // Put the nested type into the test index, so that it'll match appropriately
    ((ElasticsearchDao) esDao).getClient().admin().indices().preparePutMapping(INDEX)
        .setType("test_doc")
        .setSource(nestedAlertMapping)
        .get();

    // Don't need any meta alerts to actually exist, since we've populated the field on the alerts.

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1"),
        Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    // Build our group request
    Group searchGroup = new Group();
    searchGroup.setField("ip_src_addr");
    List<Group> groupList = new ArrayList<>();
    groupList.add(searchGroup);
    GroupResponse groupResponse = metaDao.group(new GroupRequest() {
      {
        setQuery("ip_src_addr:192.168.1.1");
        setIndices(Collections.singletonList("*"));
        setScoreField("score_field");
        setGroups(groupList);
    }});

    // Should only return the standalone alert in the group
    GroupResult result = groupResponse.getGroupResults().get(0);
    Assert.assertEquals(1, result.getTotal());
    Assert.assertEquals("192.168.1.1", result.getKey());
    // No delta, since no ops happen
    Assert.assertEquals(10.0d, result.getScore(), 0.0d);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldUpdateMetaAlertOnAlertUpdate() throws Exception {
    // Load alerts
    List<Map<String, Object>> alerts = buildAlerts(2);
    alerts.get(0).put(METAALERT_FIELD, Arrays.asList("meta_active", "meta_inactive"));
    elasticsearchAdd(alerts, INDEX, SENSOR_NAME);

    // Load metaAlerts
    Map<String, Object> activeMetaAlert = buildMetaAlert("meta_active", MetaAlertStatus.ACTIVE,
        Optional.of(Collections.singletonList(alerts.get(0))));
    Map<String, Object> inactiveMetaAlert = buildMetaAlert("meta_inactive", MetaAlertStatus.INACTIVE,
        Optional.of(Collections.singletonList(alerts.get(0))));
    // We pass MetaAlertDao.METAALERT_TYPE, because the "_doc" gets appended automatically.
    elasticsearchAdd(Arrays.asList(activeMetaAlert, inactiveMetaAlert), METAALERTS_INDEX, METAALERT_TYPE);

    // Verify load was successful
    findCreatedDocs(Arrays.asList("message_0", "message_1", "meta_active", "meta_inactive"), Arrays.asList(SENSOR_NAME, METAALERT_TYPE));

    {
      //modify the first message and add a new field
      Map<String, Object> message0 = new HashMap<String, Object>(alerts.get(0)) {
        {
          put(NEW_FIELD, "metron");
          put(MetaAlertDao.THREAT_FIELD_DEFAULT, "10");
        }
      };
      String guid = "" + message0.get(Constants.GUID);
      metaDao.update(new Document(message0, guid, SENSOR_NAME, null), Optional.empty());

      {
        //ensure alerts in ES are up-to-date
        findUpdatedDoc(message0, guid, SENSOR_NAME);
        long cnt = getMatchingAlertCount(NEW_FIELD, message0.get(NEW_FIELD));
        if (cnt == 0) {
          Assert.fail("Elasticsearch alert not updated!");
        }
      }

      {
        //ensure meta alerts in ES are up-to-date
        long cnt = getMatchingMetaAlertCount(NEW_FIELD, "metron");
        if (cnt == 0) {
          Assert.fail("Active metaalert was not updated!");
        }
        if (cnt != 1) {
          Assert.fail("Elasticsearch metaalerts not updated correctly!");
        }
      }
    }
    //modify the same message and modify the new field
    {
      Map<String, Object> message0 = new HashMap<String, Object>(alerts.get(0)) {
        {
          put(NEW_FIELD, "metron2");
        }
      };
      String guid = "" + message0.get(Constants.GUID);
      metaDao.update(new Document(message0, guid, SENSOR_NAME, null), Optional.empty());

      {
        //ensure ES is up-to-date
        findUpdatedDoc(message0, guid, SENSOR_NAME);
        long cnt = getMatchingAlertCount(NEW_FIELD, message0.get(NEW_FIELD));
        if (cnt == 0) {
          Assert.fail("Elasticsearch alert not updated!");
        }
      }
      {
        //ensure meta alerts in ES are up-to-date
        long cnt = getMatchingMetaAlertCount(NEW_FIELD, "metron2");
        if (cnt == 0) {
          Assert.fail("Active metaalert was not updated!");
        }
        if (cnt != 1) {
          Assert.fail("Elasticsearch metaalerts not updated correctly!");
        }
      }
    }
  }

  @Test
  public void shouldThrowExceptionOnMetaAlertUpdate() throws Exception {
    Document metaAlert = new Document(new HashMap<>(), "meta_alert", METAALERT_TYPE, 0L);
    try {
      metaDao.update(metaAlert, Optional.empty());
      Assert.fail("Direct meta alert update should throw an exception");
    } catch (UnsupportedOperationException uoe) {
      Assert.assertEquals("Meta alerts do not direct update", uoe.getMessage());
    }
  }

  protected long getMatchingAlertCount(String fieldName, Object fieldValue) throws IOException, InterruptedException {
    long cnt = 0;
    for (int t = 0; t < MAX_RETRIES && cnt == 0; ++t, Thread.sleep(SLEEP_MS)) {
      List<Map<String, Object>> docs = es.getAllIndexedDocs(INDEX, SENSOR_NAME + "_doc");
      cnt = docs
          .stream()
          .filter(d -> {
            Object newfield = d.get(fieldName);
            return newfield != null && newfield.equals(fieldValue);
          }).count();
    }
    return cnt;
  }

  protected long getMatchingMetaAlertCount(String fieldName, String fieldValue) throws IOException, InterruptedException {
    long cnt = 0;
    for (int t = 0; t < MAX_RETRIES && cnt == 0; ++t, Thread.sleep(SLEEP_MS)) {
      List<Map<String, Object>> docs = es.getAllIndexedDocs(METAALERTS_INDEX, MetaAlertDao.METAALERT_DOC);
      cnt = docs
          .stream()
          .filter(d -> {
            List<Map<String, Object>> alerts = (List<Map<String, Object>>) d
                .get(MetaAlertDao.ALERT_FIELD);

            for (Map<String, Object> alert : alerts) {
              Object newField = alert.get(fieldName);
              if (newField != null && newField.equals(fieldValue)) {
                return true;
              }
            }

            return false;
          }).count();
    }
    return cnt;
  }

  protected void findUpdatedDoc(Map<String, Object> message0, String guid, String sensorType)
      throws InterruptedException, IOException, OriginalNotFoundException {
    for (int t = 0; t < MAX_RETRIES; ++t, Thread.sleep(SLEEP_MS)) {
      Document doc = metaDao.getLatest(guid, sensorType);
      if (doc != null && message0.equals(doc.getDocument())) {
        return;
      }
    }
    throw new OriginalNotFoundException("Count not find " + guid + " after " + MAX_RETRIES + "tries");
  }

  protected boolean findCreatedDoc(String guid, String sensorType)
      throws InterruptedException, IOException, OriginalNotFoundException {
    for (int t = 0; t < MAX_RETRIES; ++t, Thread.sleep(SLEEP_MS)) {
      Document doc = metaDao.getLatest(guid, sensorType);
      if (doc != null) {
        return true;
      }
    }
    throw new OriginalNotFoundException("Count not find " + guid + " after " + MAX_RETRIES + "tries");
  }

  protected boolean findCreatedDocs(Collection<String> guids, Collection<String> sensorTypes)
      throws InterruptedException, IOException, OriginalNotFoundException {
    for (int t = 0; t < MAX_RETRIES; ++t, Thread.sleep(SLEEP_MS)) {
      Iterable<Document> docs = metaDao.getAllLatest(guids, sensorTypes);
      if (docs != null) {
        int docCount = 0;
        for (Document doc: docs) {
          docCount++;
        }
        if (guids.size() == docCount) {
          return true;
        }
      }
    }
    throw new OriginalNotFoundException("Count not find guids after " + MAX_RETRIES + "tries");
  }

  protected List<Map<String, Object>> buildAlerts(int count) {
    List<Map<String, Object>> inputData = new ArrayList<>();
    for (int i = 0; i < count; ++i) {
      final String guid = "message_" + i;
      Map<String, Object> alerts = new HashMap<>();
      alerts.put(Constants.GUID, guid);
      alerts.put("source:type", SENSOR_NAME);
      alerts.put(MetaAlertDao.THREAT_FIELD_DEFAULT, i);
      alerts.put("timestamp", System.currentTimeMillis());
      inputData.add(alerts);
    }
    return inputData;
  }

  protected Map<String, Object> buildMetaAlert(String guid, MetaAlertStatus status, Optional<List<Map<String, Object>>> alerts) {
    Map<String, Object> metaAlert = new HashMap<>();
    metaAlert.put(Constants.GUID, guid);
    metaAlert.put("source:type", METAALERT_TYPE);
    metaAlert.put(MetaAlertDao.STATUS_FIELD, status.getStatusString());
    if (alerts.isPresent()) {
      List<Map<String, Object>> alertsList = alerts.get();
      metaAlert.put(ALERT_FIELD, alertsList);
    }
    return metaAlert;
  }

  protected void elasticsearchAdd(List<Map<String, Object>> inputData, String index, String docType)
      throws IOException {
    es.add(index, docType, inputData.stream().map(m -> {
          try {
            return JSONUtils.INSTANCE.toJSON(m, true);
          } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
          }
        }
        ).collect(Collectors.toList())
    );
  }
}
