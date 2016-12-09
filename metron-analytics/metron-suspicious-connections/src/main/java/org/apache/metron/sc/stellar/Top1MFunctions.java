package org.apache.metron.sc.stellar;

import org.apache.commons.io.IOUtils;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.ParseException;
import org.apache.metron.common.dsl.Stellar;
import org.apache.metron.common.dsl.StellarFunction;
import org.apache.metron.common.utils.BloomFilter;
import org.apache.metron.common.utils.SerDeUtils;

import java.io.IOException;
import java.util.List;

public class Top1MFunctions {
  @Stellar(name="GET"
          ,namespace="TOP1M"
          , description="Load the top1m bloom filter"
          , params= {}
          , returns="The bloom filter for the top1m domains"
          )
  public static class Get implements StellarFunction {
    BloomFilter<Object> bf = null;
    @Override
    public Object apply(List<Object> args, Context context) throws ParseException {
      return bf;
    }

    @Override
    public void initialize(Context context) {
      try {
        byte[] bfBytes = IOUtils.toByteArray(getClass().getResourceAsStream("top-1m.obj"));
        bf = SerDeUtils.fromBytes(bfBytes, BloomFilter.class);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to initialize: " + e.getMessage(), e);
      }
    }

    @Override
    public boolean isInitialized() {
      return bf != null;
    }
  }
}
