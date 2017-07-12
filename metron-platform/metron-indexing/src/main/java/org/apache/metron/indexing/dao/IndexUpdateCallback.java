package org.apache.metron.indexing.dao;

import java.io.IOException;

public interface IndexUpdateCallback {
  void postUpdate(IndexDao dao, Document doc) throws IOException;
}
