package org.apache.metron.maas;

import org.apache.commons.collections.bag.TreeBag;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

public class Dummytest {
  @Test
  public void test() {
    TreeMap<Integer, String> map = new TreeMap<>();
    map.put(128, "128");
    map.put(64, "64");
    System.out.println(map.ceilingEntry(70));
    System.out.println(map.ceilingEntry(64));
    int minContainerSize = 150;
    Assert.assertEquals(getAdjustedSize(120, minContainerSize), 150);
    Assert.assertEquals(getAdjustedSize(170, minContainerSize), 300);
    Assert.assertEquals(getAdjustedSize(150, minContainerSize), 150);
  }
  public int getAdjustedSize(int size, int minSize) {
    return (int)(minSize * Math.ceil(1.0*size/minSize));
  }
}
