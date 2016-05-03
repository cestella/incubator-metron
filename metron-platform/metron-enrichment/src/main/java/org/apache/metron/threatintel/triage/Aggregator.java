package org.apache.metron.threatintel.triage;

import java.util.List;
import java.util.Map;

public interface Aggregator {
  Double aggregate(List<Number> scores, Map<String, Object> config);
}
