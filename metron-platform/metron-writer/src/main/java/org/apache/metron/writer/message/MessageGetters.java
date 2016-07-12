package org.apache.metron.writer.message;


import backtype.storm.tuple.Tuple;
import org.json.simple.JSONObject;

public enum MessageGetters implements MessageGetter{
   RAW(RawMessageGetter.DEFAULT)
  ,NAMED(NamedMessageGetter.DEFAULT)
  ;
  MessageGetter getter;
  MessageGetters(MessageGetter getter) {
    this.getter = getter;
  }
  @Override
  public JSONObject getMessage(Tuple t) {
    return getter.getMessage(t);
  }
}
