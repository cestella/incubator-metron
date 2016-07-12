package org.apache.metron.writer.message;

import backtype.storm.tuple.Tuple;
import org.json.simple.JSONObject;

public interface MessageGetter {
  JSONObject getMessage(Tuple t);
}
