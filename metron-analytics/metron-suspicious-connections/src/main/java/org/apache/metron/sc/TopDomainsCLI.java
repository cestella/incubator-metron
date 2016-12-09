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
