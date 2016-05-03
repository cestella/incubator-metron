package org.apache.metron.common.configuration.enrichment.threatintel;

import org.apache.metron.common.aggregator.Aggregator;
import org.apache.metron.common.aggregator.Aggregators;

import java.util.HashMap;
import java.util.Map;

public class ThreatTriageConfig {
  private Map<String, Number> riskLevelRules = new HashMap<>();
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

  @Override
  public String toString() {
    return "ThreatTriageConfig{" +
            "riskLevelRules=" + riskLevelRules +
            ", aggregator=" + aggregator +
            ", aggregationConfig=" + aggregationConfig +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ThreatTriageConfig that = (ThreatTriageConfig) o;

    if (getRiskLevelRules() != null ? !getRiskLevelRules().equals(that.getRiskLevelRules()) : that.getRiskLevelRules() != null)
      return false;
    if (getAggregator() != null ? !getAggregator().equals(that.getAggregator()) : that.getAggregator() != null)
      return false;
    return getAggregationConfig() != null ? getAggregationConfig().equals(that.getAggregationConfig()) : that.getAggregationConfig() == null;

  }

  @Override
  public int hashCode() {
    int result = getRiskLevelRules() != null ? getRiskLevelRules().hashCode() : 0;
    result = 31 * result + (getAggregator() != null ? getAggregator().hashCode() : 0);
    result = 31 * result + (getAggregationConfig() != null ? getAggregationConfig().hashCode() : 0);
    return result;
  }
}
