package org.apache.metron.threatintel.triage;

import com.google.common.base.Function;
import org.apache.metron.threatintel.triage.aggregator.Aggregators;
import org.apache.metron.threatintel.triage.predicate.PredicateProcessor;
import org.apache.metron.threatintel.triage.predicate.VariableResolver;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor implements Function<Map, Double> {
  private Map<String, Number> riskLevelRules;
  private Aggregator aggregator = Aggregators.MAX;
  private Map<String, Object> aggregationConfig = new HashMap<>();

  public Map<String, Number> getRiskLevelRules() {
    return riskLevelRules;
  }

  public void setRiskLevelRules(Map<String, Number> riskLevelRules) {
    this.riskLevelRules = riskLevelRules;
  }

  public Aggregator getAggregator() {
    return aggregator;
  }

  public void setAggregator(String aggregator) {
    this.aggregator = Aggregators.valueOf(aggregator);
  }

  public Map<String, Object> getAggregationConfig() {
    return aggregationConfig;
  }

  public void setAggregationConfig(Map<String, Object> aggregationConfig) {
    this.aggregationConfig = aggregationConfig;
  }

  @Nullable
  @Override
  public Double apply(@Nullable Map input) {
    List<Number> scores = new ArrayList<>();
    PredicateProcessor predicateProcessor = new PredicateProcessor();
    VariableResolver resolver = new MapVariableResolver(input);
    for(Map.Entry<String, Number> kv : getRiskLevelRules().entrySet()) {
      try {
        if(predicateProcessor.parse(kv.getKey(), resolver)) {
          scores.add(kv.getValue());
        }
      }
      catch(RuntimeException re) {
        //skip if there's a problem
      }
    }
    return getAggregator().aggregate(scores, getAggregationConfig());
  }
}
