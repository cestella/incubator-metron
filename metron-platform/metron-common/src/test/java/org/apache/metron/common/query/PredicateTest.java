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

package org.apache.metron.common.query;

import org.apache.metron.common.query.PredicateProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PredicateTest {

  @Test
  public void testSimpleOps() throws Exception {
    final Map<String, String> variableMap = new HashMap<String, String>() {{
      put("foo", "casey");
      put("empty", "");
      put("spaced", "metron is great");
      put("foo.bar", "casey");
    }};
    PredicateProcessor processor = new PredicateProcessor();
    Assert.assertTrue(processor.parse("'casey' == foo.bar", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("'casey' == foo", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("'casey' != foo", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("'stella' == 'stella'", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("'stella' == foo", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("foo== foo", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("empty== ''", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("spaced == 'metron is great'", v -> variableMap.get(v)));
  }

  @Test
  public void testBooleanOps() throws Exception {
    final Map<String, String> variableMap = new HashMap<String, String>() {{
      put("foo", "casey");
      put("empty", "");
      put("spaced", "metron is great");
    }};
    PredicateProcessor processor = new PredicateProcessor();
    Assert.assertTrue(processor.parse("('casey' == foo) && ( false != true )", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("('casey' == foo) and (FALSE == TRUE)", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("'casey' == foo and FALSE", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("'casey' == foo and true", v -> variableMap.get(v)));
  }
  @Test
  public void testList() throws Exception {
    final Map<String, String> variableMap = new HashMap<String, String>() {{
      put("foo", "casey");
      put("empty", "");
      put("spaced", "metron is great");
    }};
    PredicateProcessor processor = new PredicateProcessor();
    Assert.assertTrue(processor.parse("foo in [ 'casey', 'david' ]", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("foo in [ foo, 'david' ]", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("foo in [ 'casey', 'david' ] and 'casey' == foo", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("foo in [ 'casey', 'david' ] and foo == 'casey'", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("foo in [ 'casey' ]", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("foo not in [ 'casey', 'david' ]", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("foo not in [ 'casey', 'david' ] and 'casey' == foo", v -> variableMap.get(v)));
  }
  @Test
  public void testExists() throws Exception {
    final Map<String, String> variableMap = new HashMap<String, String>() {{
      put("foo", "casey");
      put("empty", "");
      put("spaced", "metron is great");
    }};
    PredicateProcessor processor = new PredicateProcessor();
    Assert.assertTrue(processor.parse("exists(foo)", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("exists(bar)", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("exists(bar) or true", v -> variableMap.get(v)));
  }

  @Test
  public void testStringFunctions() throws Exception {
    final Map<String, String> variableMap = new HashMap<String, String>() {{
      put("foo", "casey");
      put("empty", "");
      put("spaced", "metron is great");
    }};
    PredicateProcessor processor = new PredicateProcessor();
    Assert.assertTrue(processor.parse("true and TO_UPPER(foo) == 'CASEY'", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("foo in [ TO_LOWER('CASEY'), 'david' ]", v -> variableMap.get(v)));
    Assert.assertTrue(processor.parse("TO_UPPER(foo) in [ TO_UPPER('casey'), 'david' ]", v -> variableMap.get(v)));
    Assert.assertFalse(processor.parse("TO_LOWER(foo) in [ TO_UPPER('casey'), 'david' ]", v -> variableMap.get(v)));
  }
}
