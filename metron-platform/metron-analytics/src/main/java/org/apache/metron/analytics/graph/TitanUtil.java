package org.apache.metron.analytics.graph;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TitanUtil {
  INSTANCE;
  static Map<String, TitanGraph> connectionPool = new HashMap<>();

  public TitanGraph getGraph(String graph) {
    synchronized (connectionPool) {
      TitanGraph ret = connectionPool.get(graph);
      if(ret == null) {
        ret = createConnection(graph);
        connectionPool.put(graph, ret);
      }
      return ret;
    }
  }

  private static TitanGraph createConnection(String graph) {
    Configuration config = HBaseConfiguration.create();
    TitanGraph ret = TitanFactory.build().set("storage.backend", "hbase")
                                         .set("storage.hostname", config.get(HConstants.ZOOKEEPER_QUORUM))
                                         .set("tablename", graph)
                                         .open();
    return ret;
  }
}
