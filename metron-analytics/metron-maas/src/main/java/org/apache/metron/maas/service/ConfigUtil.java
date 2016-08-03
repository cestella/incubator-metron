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
package org.apache.metron.maas.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.metron.maas.config.MaaSConfig;
import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public enum ConfigUtil {
  INSTANCE;
  private static ThreadLocal<ObjectMapper> _mapper = new ThreadLocal<ObjectMapper>() {
    /**
     * Returns the current thread's "initial value" for this
     * thread-local variable.  This method will be invoked the first
     * time a thread accesses the variable with the {@link #get}
     * method, unless the thread previously invoked the {@link #set}
     * method, in which case the {@code initialValue} method will not
     * be invoked for the thread.  Normally, this method is invoked at
     * most once per thread, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     * <p>
     * <p>This implementation simply returns {@code null}; if the
     * programmer desires thread-local variables to have an initial
     * value other than {@code null}, {@code ThreadLocal} must be
     * subclassed, and this method overridden.  Typically, an
     * anonymous inner class will be used.
     *
     * @return the initial value for this thread-local
     */
    @Override
    protected ObjectMapper initialValue() {
      return new ObjectMapper();
    }
  };
  public <T> T read(CuratorFramework client, String root, T def, Class<T> clazz) throws Exception {
    try {
      byte[] data = client.getData().forPath(root);
      return read(data, clazz);
    }
    catch(KeeperException.NoNodeException nne) {
      return def;
    }
  }
  public <T> T read(byte[] data, Class<T> clazz) throws Exception {
      return _mapper.get().readValue(data, clazz);
  }
  public byte[] toBytes(Object o) throws IOException {
    return _mapper.get().writeValueAsBytes(o);
  }
}
