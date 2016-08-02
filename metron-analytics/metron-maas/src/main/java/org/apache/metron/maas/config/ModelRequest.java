package org.apache.metron.maas.config;

public class ModelRequest {

  private String name;
  private String version;
  private int numInstances;
  private int memory;
  private Action action;
  private String path;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
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

  public int getNumInstances() {
    return numInstances;
  }

  public void setNumInstances(int numInstances) {
    this.numInstances = numInstances;
  }

  public int getMemory() {
    return memory;
  }

  public void setMemory(int memory) {
    this.memory = memory;
  }

}
