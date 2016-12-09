package org.apache.metron.sc.integration;

import com.google.common.collect.ImmutableMap;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.StellarFunctions;
import org.apache.metron.common.stellar.StellarProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BFTest {
  public static Object run(String rule, Map<String, Object> variables) {
    Context context = Context.EMPTY_CONTEXT();
    StellarProcessor processor = new StellarProcessor();
    return processor.parse(rule, x -> variables.get(x), StellarFunctions.FUNCTION_RESOLVER(), context);
  }

  @Test
  public void testBFGetSuccess() {
    String query = "BLOOM_EXISTS(TOP1M_GET(), 'google')";
    Assert.assertTrue((Boolean) run(query, new HashMap<>()));
  }

  @Test
  public void testBFGetFail() {
    String query = "BLOOM_EXISTS(TOP1M_GET(), 'caseystella')";
    Assert.assertFalse((Boolean) run(query, new HashMap<>()));
  }
}
