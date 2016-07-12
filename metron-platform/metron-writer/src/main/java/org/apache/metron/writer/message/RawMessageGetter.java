package org.apache.metron.writer.message;

import backtype.storm.tuple.Tuple;
import org.apache.metron.common.utils.JSONUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;

public class RawMessageGetter implements MessageGetter {
  public static RawMessageGetter DEFAULT = new RawMessageGetter(0);
  private ThreadLocal<JSONParser> parser = new ThreadLocal<JSONParser>() {
    @Override
    protected JSONParser initialValue() {
      return new JSONParser();
    }
  };
  int position = 0;
  public RawMessageGetter(int position) {
    this.position = position;
  }
  @Override
  public JSONObject getMessage(Tuple t) {
    byte[] data = t.getBinary(position);
    try {
      return (JSONObject) parser.get().parse(new String(data, "UTF8"));
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }
}
