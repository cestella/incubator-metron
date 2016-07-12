package org.apache.metron.writer.message;

import backtype.storm.tuple.Tuple;
import org.json.simple.JSONObject;

public class NamedMessageGetter implements MessageGetter {
  public static NamedMessageGetter DEFAULT = new NamedMessageGetter("message");
  private String messageName;
  public NamedMessageGetter(String name) {
    this.messageName = name;
  }
  @Override
  public JSONObject getMessage(Tuple tuple) {
    return (JSONObject)tuple.getValueByField(messageName);
  }
}
