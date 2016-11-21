package org.apache.metron.sc.word;

public class State {
  private String stateProjection;
  private String stateUpdate;

  public String getStateProjection() {
    return stateProjection;
  }

  public void setStateProjection(String stateProjection) {
    this.stateProjection = stateProjection;
  }

  public String getStateUpdate() {
    return stateUpdate;
  }

  public void setStateUpdate(String stateUpdate) {
    this.stateUpdate = stateUpdate;
  }
}
