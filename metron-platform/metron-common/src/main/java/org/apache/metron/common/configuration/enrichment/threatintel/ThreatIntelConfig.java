package org.apache.metron.common.configuration.enrichment.threatintel;

import org.apache.metron.common.configuration.enrichment.EnrichmentConfig;

public class ThreatIntelConfig extends EnrichmentConfig {
  private ThreatTriageConfig triageConfig = new ThreatTriageConfig();

  public ThreatTriageConfig getTriageConfig() {
    return triageConfig;
  }

  public void setTriageConfig(ThreatTriageConfig triageConfig) {
    this.triageConfig = triageConfig;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ThreatIntelConfig that = (ThreatIntelConfig) o;

    return getTriageConfig() != null ? getTriageConfig().equals(that.getTriageConfig()) : that.getTriageConfig() == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getTriageConfig() != null ? getTriageConfig().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ThreatIntelConfig{" +
            "triageConfig=" + triageConfig +
            '}';
  }
}
