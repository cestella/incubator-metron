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

package org.apache.metron.stellar.common.utils.hashing.semantic;

import org.apache.commons.codec.EncoderException;
import org.apache.metron.stellar.common.utils.hashing.Hasher;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class DelegatingSemanticHasher implements Hasher {
  public static final String HASHER_CONF = "hasher";
  public static final String RETURN_CONF = "return";
  public static final String VECTOR_KEY = "vector";
  public static final String HASH_KEY = "hash";
  public enum Return implements Function<Map<String, Object>, Object> {
    VECTOR(m -> m.getOrDefault(VECTOR_KEY, null)),
    HASH(m -> m.getOrDefault(HASH_KEY, null)),
    ALL(m -> m)
    ;
    Function<Map<String, Object>, Object> func;
    Return(Function<Map<String, Object>, Object> func) {
      this.func = func;
    }

    @Override
    public Object apply(Map<String, Object> in) {
      return func.apply(in);
    }

    public static Optional<Return> from(String name) {
      try {
        return Optional.ofNullable(Return.valueOf(name.toUpperCase()));
      }
      catch(Exception e) {
        return Optional.empty();
      }
    }

  }
  private Function<Map<String, Object>, Map<String, Object>> hasher;
  private Return returnStrategy = Return.HASH;

  /**
   * Returns an encoded string representation of the hash value of the input. It is expected that
   * this implementation does throw exceptions when the input is null.
   *
   * @param toHash The value to hash.
   * @return A hash of {@code toHash} that has been encoded.
   * @throws EncoderException         If unable to encode the hash then this exception occurs.
   * @throws NoSuchAlgorithmException If the supplied algorithm is not known.
   */
  @Override
  public Object getHash(Object toHash) throws EncoderException, NoSuchAlgorithmException {
    if(hasher == null) {
      return null;
    }
    if(toHash == null || !(toHash instanceof Map)) {
      throw new IllegalArgumentException("Invalid argument " + toHash + " expected a non-null map.");
    }
    Map<String, Object> input = (Map<String, Object>) toHash;
    return returnStrategy.apply(hasher.apply(input));
  }

  /**
   * Configure the hasher with a string to object map.
   *
   * @param config
   */
  @Override
  public void configure(Optional<Map<String, Object>> config) {
    hasher = (Function<Map<String, Object>, Map<String, Object>>) config.get().get(HASHER_CONF);
    returnStrategy = Return.from((String)config.get().get(RETURN_CONF)).orElse(returnStrategy);
  }

  public static final Set<String> supportedHashes() {
    return new HashSet<String>() {{
      add("SEMHASH");
    }};
  }
}
