package org.apache.metron.maas.config;

import java.util.HashMap;
import java.util.Map;

public class Endpoint {
  String url;
  Map<String, String> endpoints = new HashMap<String, String>(){{
    put("apply", "apply");
  }};

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(Map<String, String> endpoints) {
    this.endpoints = endpoints;
  }


  @Override
  public String toString() {
    return "Endpoint{" +
            "url='" + url + '\'' +
            ", endpoints=" + endpoints +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Endpoint endpoint = (Endpoint) o;

    if (getUrl() != null ? !getUrl().equals(endpoint.getUrl()) : endpoint.getUrl() != null) return false;
    return getEndpoints() != null ? getEndpoints().equals(endpoint.getEndpoints()) : endpoint.getEndpoints() == null;

  }

  @Override
  public int hashCode() {
    int result = getUrl() != null ? getUrl().hashCode() : 0;
    result = 31 * result + (getEndpoints() != null ? getEndpoints().hashCode() : 0);
    return result;
  }
}
