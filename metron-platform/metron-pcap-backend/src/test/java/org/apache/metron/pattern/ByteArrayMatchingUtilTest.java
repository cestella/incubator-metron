package org.apache.metron.pattern;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class ByteArrayMatchingUtilTest {
  public static byte[] DEADBEEF = new byte[] {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef};
  public static byte[] DEADBEEF_DONUTHOLE = new byte[] {(byte) 0xde, (byte) 0xad, (byte)0x00, (byte)0x00, (byte) 0xbe, (byte) 0xef};
  public static byte[] ALLFS = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

  @Test
  public void testStringMatch() throws ExecutionException {
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("'casey'", "casey".getBytes()));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("'casey'", "casey stella".getBytes()));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("'casey'", "edward casey stella".getBytes()));
    Assert.assertFalse(ByteArrayMatchingUtil.INSTANCE.match("'casey'", "james".getBytes()));
  }

  @Test
  public void testBytesMatch() throws ExecutionException {
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("deadbeef", join(DEADBEEF, "casey".getBytes())));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("deadbeef 'casey'", join(DEADBEEF, "casey".getBytes())));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("deadbeef 'casey'", join(DEADBEEF, "caseyjones".getBytes())));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("deadbeef `casey`", join(DEADBEEF, "caseyjones".getBytes(), DEADBEEF)));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("([ff]){4}", ALLFS));
    Assert.assertFalse(ByteArrayMatchingUtil.INSTANCE.match("([ff]){6}", ALLFS));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("[^ff]", new byte[] { (byte)0x00 }));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("&01", new byte[] { (byte)0x07 }));
    Assert.assertFalse(ByteArrayMatchingUtil.INSTANCE.match("&01", new byte[] { (byte)0x00 }));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("&01", new byte[] { (byte)0x00, (byte)0x01 }));
    Assert.assertTrue(ByteArrayMatchingUtil.INSTANCE.match("(dead).{2}(beef)", DEADBEEF_DONUTHOLE));
  }

  public byte[] join(byte[]... array) {
    byte[] ret;
    int size = 0;
    for(int i = 0;i < array.length;++i) {
      size += array[i].length;
    }
    ret = new byte[size];
    int j = 0;
    for(int i = 0;i < array.length;++i) {
      for(int k = 0;k < array[i].length;++k,++j) {
        ret[j] = array[i][k];
      }
    }
    return ret;
  }
}
