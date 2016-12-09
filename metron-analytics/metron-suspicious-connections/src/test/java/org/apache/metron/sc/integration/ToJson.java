package org.apache.metron.sc.integration;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import org.apache.metron.common.utils.JSONUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ToJson {
  private final static SimpleDateFormat TS_FORMAT = new SimpleDateFormat("MMM d yyyy HH:mm:ss.SSSSSSSSS z");
  public static final void main(String... argv) throws Exception {
    //"frame_time","frame_len","ip_dst","ip_src","dns_qry_name","dns_qry_class_name","dns_qry_type_name","dns_qry_rcode_name","dns_a"
    File inFile = new File("/tmp/dns_data.csv");
    File outFile = new File("/tmp/dns_data.json");
    CSVParser parser = new CSVParserBuilder().build();
    BufferedReader br = new BufferedReader(new FileReader(inFile));
    List<Map<String, Object>> messages = new ArrayList<>();
    for(String line = null;(line = br.readLine()) != null;) {
      String[] tokens = parser.parseLine(line);
      Map<String, Object> msg = toMsg(tokens);
      messages.add(msg);
    }
    Collections.sort(messages, new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Long l = (Long)o1.get("timestamp");
        Long r = (Long)o2.get("timestamp");
        if(l == r) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        }
        else {
          return Long.compare(l, r);
        }
      }
    });
    br.close();
    PrintWriter pw = new PrintWriter(outFile);
    for(Map<String, Object> msg : messages) {
      pw.println(new String(JSONUtils.INSTANCE.toJSON(msg)));
    }
    pw.close();
  }
  public static final Map<String, Object> toMsg(String[] tokens) throws ParseException {
    Map<String, Object> msg = new HashMap<>();
    int offset = 0;
    msg.put("timestamp", TS_FORMAT.parse(tokens[offset++]).getTime());
    msg.put("frame_len", Integer.parseInt(tokens[offset++]));
    msg.put("ip_dst", tokens[offset++]);
    msg.put("ip_src", tokens[offset++]);
    msg.put("dns_qry_name", tokens[offset++]);
    msg.put("dns_qry_class_name", tokens[offset++]);
    msg.put("dns_qry_type_name", tokens[offset++]);
    msg.put("dns_qry_rcode_name", tokens[offset++]);
    msg.put("dns_a", tokens[offset++]);
    return msg;
  }
}
