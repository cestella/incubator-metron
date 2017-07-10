package org.apache.metron.writer.dao;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;

import java.io.IOException;

public class NoSqlDao extends IndexDao {
  private HTableInterface tableInterface;
  private byte[] cf;

  public NoSqlDao(HTableInterface tableInterface, String cf) {
    this.tableInterface = tableInterface;
    this.cf = cf.getBytes();
  }
  @Override
  public Document getLatest(String uuid) throws IOException {
    Get get = new Get(uuid.getBytes());
    get.addFamily(cf);
    tableInterface.get()
    return null;
  }

  @Override
  public void update(Document update) {

  }
}
