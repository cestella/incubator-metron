package org.apache.metron.integration.mock;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.metron.hbase.TableProvider;
import org.apache.metron.test.mock.MockHTable;

import java.io.IOException;
import java.io.Serializable;

public class MockTableProvider implements TableProvider, Serializable {
  static MockHTable.Provider provider = new MockHTable.Provider();
  @Override
  public HTableInterface getTable(Configuration config, String tableName) throws IOException {
    return provider.getTable(config, tableName);
  }
  public static void addTable(String tableName, String... cf) {
    provider.addToCache(tableName, cf);
  }
  public static MockHTable getTable(String tableName) {
    try {
      return (MockHTable) provider.getTable(null, tableName);
    } catch (IOException e) {
      throw new RuntimeException("Unable to get table: " + tableName);
    }
  }
}
