/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.metron.sc;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.metron.common.utils.BloomFilter;
import org.apache.metron.common.utils.SerDeUtils;

import java.io.*;

public class TopDomainsCLI {
  public static void main(String... argv) throws IOException {
    File csv = new File(argv[0]);
    File out = new File(argv[1]);
    int expectedInsertions = 1000000;
    double falsePositiveRate = 1e-3;
    BloomFilter<Object> filter = new BloomFilter<>(SerDeUtils.SERIALIZER, expectedInsertions, falsePositiveRate);
    try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
      for (String line = null; (line = br.readLine()) != null; ) {
        String fullDomain = Iterables.getLast(Splitter.on(",").split(line));
        String domain = Iterables.getFirst(Splitter.on(".").split(fullDomain), null);
        if (domain != null) {
          filter.add(domain);
        }
      }
    }
    try( OutputStream os = new FileOutputStream(out)) {
      os.write(SerDeUtils.toBytes(filter));
      os.flush();
    }
  }
}
