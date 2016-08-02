package org.apache.metron.maas.config;

import org.apache.metron.maas.service.ContainerTracker;

public class Model {
  private String name;
  private String version;
  public Model(String name, String version) {
    setName(name);
    setVersion(version);
  }
  public Model(ModelRequest r) {
    setName(r.getName());
    setVersion(r.getVersion());
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

    Model model = (Model) o;

    if (getName() != null ? !getName().equals(model.getName()) : model.getName() != null) return false;
    return getVersion() != null ? getVersion().equals(model.getVersion()) : model.getVersion() == null;

  }

  @Override
  public int hashCode() {
    int result = getName() != null ? getName().hashCode() : 0;
    result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
    return result;
  }
}
