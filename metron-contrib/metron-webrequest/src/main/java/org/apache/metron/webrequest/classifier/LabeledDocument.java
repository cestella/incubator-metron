package org.apache.metron.webrequest.classifier;

import java.io.Serializable;

public class LabeledDocument implements Serializable {
  private String text;
  private String label;

  public LabeledDocument(String text, String label) {
    this.text = text;
    this.label = label;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
