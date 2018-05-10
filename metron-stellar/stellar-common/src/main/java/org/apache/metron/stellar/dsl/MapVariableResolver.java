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

package org.apache.metron.stellar.dsl;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapVariableResolver implements VariableResolver {

  List<Map> variableMappings = new ArrayList<>();

  public MapVariableResolver(Map variableMappingOne, Map... variableMapping) {
    if (variableMappingOne != null) {
      variableMappings.add(variableMappingOne);
    }
    add(variableMapping);
  }

  public void add(Map... ms) {
    if (ms != null) {
      for (Map m : ms) {
        if (m != null) {
          this.variableMappings.add(m);
        }
      }
    }
  }

  @Override
  public Object resolve(String variable) {
    if(variable.equals(VariableResolver.ALL_FIELDS)) {
      return new ConcatMap();
    }
    for (Map variableMapping : variableMappings) {
      Object o = variableMapping.get(variable);
      if (o != null) {
        return o;
      }
    }
    return null;
  }

  @Override
  public boolean exists(String variable) {
    return true;
  }

  public class ConcatMap implements Map<String, Object>
  {
    @Override
    public int size() {
      int size = 0;
      for(Map m : variableMappings) {
        size += m.size();
      }
      return size;
    }

    @Override
    public boolean isEmpty() {
      boolean isEmpty = true;
      for(Map m : variableMappings) {
        isEmpty &= m.isEmpty();
      }
      return isEmpty;
    }

    @Override
    public boolean containsKey(Object key) {
      for(Map m : variableMappings) {
        if(m.containsKey(key)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      for(Map m : variableMappings) {
        if(m.containsValue(value)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Object get(Object key) {
      Object ret = null;
      for(Map m : variableMappings) {
        ret = m.get(key);
        if(ret != null) {
          break;
        }
      }
      return ret;
    }

    @Override
    public Object put(String key, Object value) {
      throw new UnsupportedOperationException("Merged map is immutable.");
    }

    @Override
    public Object remove(Object key) {
      throw new UnsupportedOperationException("Merged map is immutable.");
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
      throw new UnsupportedOperationException("Merged map is immutable.");
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException("Merged map is immutable.");
    }

    @Override
    public Set<String> keySet() {
      Set<String> ret = null;
      for(Map m : variableMappings) {
        if(ret == null) {
          ret = m.keySet();
        }
        else {
          ret = Sets.union(ret, m.keySet());
        }
      }
      return ret;
    }

    @Override
    public Collection<Object> values() {
      Collection<Object> ret = new ArrayList<>(size());
      for(Map m : variableMappings) {
        ret.addAll(m.values());
      }
      return ret;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      Set<Entry<String, Object>> ret = null;
      for(Map m : variableMappings) {
        if(ret == null) {
          ret = m.entrySet();
        }
        else {
          ret = Sets.union(ret, m.entrySet());
        }
      }
      return ret;
    }
  }
}
