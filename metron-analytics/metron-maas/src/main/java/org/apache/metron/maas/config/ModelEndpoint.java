package org.apache.metron.maas.config;

public class ModelEndpoint {
  private String url;
  private String name;
  private String version;
  private String containerId;

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModelEndpoint that = (ModelEndpoint) o;

    if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) return false;
    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
    return getVersion() != null ? getVersion().equals(that.getVersion()) : that.getVersion() == null;

  }

  @Override
  public int hashCode() {
    int result = getUrl() != null ? getUrl().hashCode() : 0;
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
    return result;
  }
}
