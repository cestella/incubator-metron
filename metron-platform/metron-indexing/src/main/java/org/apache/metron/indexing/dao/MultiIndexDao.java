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

package org.apache.metron.indexing.dao;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.metron.common.configuration.writer.WriterConfiguration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MultiIndexDao extends IndexDao {
  private List<IndexDao> indices;

  public MultiIndexDao(IndexDao... composedDao) {
    this((x,y) -> {}, composedDao);
  }

  public MultiIndexDao(IndexUpdateCallback callback, IndexDao... composedDao) {
    super(callback);
    indices = new ArrayList<>();
    for(IndexDao dao: composedDao) {
      indices.add(dao);
    }
  }

  public MultiIndexDao(IndexUpdateCallback callback, List<IndexDao> composedDao) {
    super(callback);
    this.indices = composedDao;
  }

  public MultiIndexDao(List<IndexDao> composedDao) {
    this((x,y) -> {});
  }

  @Override
  public void update(final Document update, WriterConfiguration configurations) throws IOException {
    List<String> exceptions =
    indices.parallelStream().map(dao -> {
      try {
        dao.update(update, configurations);
        return null;
      } catch (Throwable e) {
        return dao.getClass() + ": " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e);
      }
    }).filter(e -> e != null).collect(Collectors.toList());
    if(exceptions.size() > 0) {
      throw new IOException(Joiner.on("\n").join(exceptions));
    }
  }

  private static class DocumentContainer {
    private Optional<Document> d = Optional.empty();
    private Optional<Throwable> t = Optional.empty();
    public DocumentContainer(Document d) {
      this.d = Optional.ofNullable(d);
    }
    public DocumentContainer(Throwable t) {
      this.t = Optional.ofNullable(t);
    }

    public Optional<Document> getDocument() {
      return d;
    }
    public Optional<Throwable> getException() {
      return t;
    }

  }

  @Override
  public Document getLatest(final String uuid, String sensorType) throws IOException {
    Document ret = null;
    List<DocumentContainer> output =
            indices.parallelStream().map(dao -> {
      try {
        return new DocumentContainer(dao.getLatest(uuid, sensorType));
      } catch (Throwable e) {
        return new DocumentContainer(e);
      }
    }).collect(Collectors.toList());

    List<String> error = new ArrayList<>();
    for(DocumentContainer dc : output) {
      if(dc.getException().isPresent()) {
        Throwable e = dc.getException().get();
        error.add(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
      }
      else {
        if(dc.getDocument().isPresent()) {
          Document d = dc.getDocument().get();
          if(ret == null || ret.getTimestamp() < d.getTimestamp()) {
            ret = d;
          }
        }
      }
    }
    if(error.size() > 0) {
      throw new IOException(Joiner.on("\n").join(error));
    }
    return ret;
  }
}
