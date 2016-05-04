package org.apache.metron.common.query;

import com.google.common.base.Function;
import org.apache.commons.net.util.SubnetUtils;

import javax.annotation.Nullable;
import java.util.List;

public enum LogicalFunctions implements Function<List<String>, Boolean> {
   NOOP(list -> true)
  ,IS_EMPTY ( list -> {
    if(list.size() == 0) {
      throw new IllegalStateException("IS_EMPTY expects one string arg");
    }
    String val = list.get(0);
    return val == null || val.isEmpty() ? true:false;
  })
  ,IN_SUBNET( list -> {
    if(list.size() < 2) {
      throw new IllegalStateException("IN_SUBNET expects two args: [ip, cidr] where cidr is the subnet mask in cidr form");
    }
    String cidr = list.get(1);
    String ip = list.get(0);
    if(cidr == null || ip == null) {
      return false;
    }
    return new SubnetUtils(cidr).getInfo().isInRange(ip);
  })
  ,STARTS_WITH( list -> {
    if(list.size() < 2) {
      throw new IllegalStateException("STARTS_WITH expects two args: [string, prefix] where prefix is the string fragment that the string should start with");
    }
    String prefix = list.get(1);
    String str = list.get(0);
    if(str == null || prefix == null) {
      return false;
    }
    return str.startsWith(prefix);
  })
  ,ENDS_WITH( list -> {
    if(list.size() < 2) {
      throw new IllegalStateException("ENDS_WITH expects two args: [string, suffix] where suffix is the string fragment that the string should end with");
    }
    String prefix = list.get(1);
    String str = list.get(0);
    if(str == null || prefix == null) {
      return false;
    }
    return str.endsWith(prefix);
  })
  ,REGEXP_MATCH( list -> {
     if(list.size() < 2) {
      throw new IllegalStateException("REGEXP_MATCH expects two args: [string, pattern] where pattern is a regexp pattern");
    }
    String pattern = list.get(1);
    String str = list.get(0);
    if(str == null || pattern == null) {
      return false;
    }
    return str.matches(pattern);
  })
  ;
  Function<List<String>, Boolean> func;
  LogicalFunctions(Function<List<String>, Boolean> func) {
    this.func = func;
  }
  @Nullable
  @Override
  public Boolean apply(@Nullable List<String> input) {
    return func.apply(input);
  }
}
