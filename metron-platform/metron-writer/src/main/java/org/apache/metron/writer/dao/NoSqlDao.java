package org.apache.metron.writer.dao;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.metron.common.configuration.writer.WriterConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

public class NoSqlDao extends IndexDao {
  private HTableInterface tableInterface;
  private byte[] cf;

  public NoSqlDao(HTableInterface tableInterface, String cf, IndexUpdateCallback callback) {
    super(callback);
    this.tableInterface = tableInterface;
    this.cf = cf.getBytes();
  }

  public NoSqlDao(HTableInterface tableInterface, String cf) {
    this(tableInterface, cf, (x,y) -> {});
  }

  @Override
  public Document getLatest(String uuid, String sensorType) throws IOException {
    Get get = new Get(uuid.getBytes());
    get.addFamily(cf);
    Result result = tableInterface.get(get);
    NavigableMap<byte[], byte[]> columns = result.getFamilyMap( cf);
    if(columns == null || columns.size() == 0) {
      return null;
    }
    Map.Entry<byte[], byte[]> entry= columns.lastEntry();
    Long ts = Bytes.toLong(entry.getKey());
    if(entry.getValue()!= null) {
      String json = new String(entry.getValue());
      return new Document(json, uuid, sensorType, ts);
    }
    else {
      return null;
    }
  }

  @Override
  public void update(Document update, WriterConfiguration configurations) throws IOException {
    Put put = new Put(update.getUuid().getBytes());
    long ts = update.getTimestamp() == null?System.currentTimeMillis():update.getTimestamp();
    byte[] columnQualifier = Bytes.toBytes(ts);
    put.addColumn(cf, columnQualifier, Bytes.toBytes(update.getDocument()));
    tableInterface.put(put);
    callback.postUpdate(this, update);
  }
}
