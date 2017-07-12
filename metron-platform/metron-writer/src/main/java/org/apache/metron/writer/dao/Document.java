package org.apache.metron.writer.dao;

public class Document {
  Long timestamp;
  String document;
  String uuid;
  String sensorType;

  public Document(String document, String uuid, String sensorType, Long timestamp) {
    setDocument(document);
    setUuid(uuid);
    setTimestamp(timestamp);
    setSensorType(sensorType);
  }

  public Document(String document, String uuid, String sensorType) {
    this( document, uuid, sensorType, null);
  }

  public String getSensorType() {
    return sensorType;
  }

  public void setSensorType(String sensorType) {
    this.sensorType = sensorType;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp != null?timestamp:System.currentTimeMillis();
  }

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
