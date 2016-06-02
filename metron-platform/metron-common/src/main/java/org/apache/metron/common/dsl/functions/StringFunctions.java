package org.apache.metron.common.dsl.functions;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StringFunctions {
  public static class JoinFunction implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> args) {
      List<Object> arg1 = (List<Object>) args.get(0);
      String delim = (String) args.get(1);
      return Joiner.on(delim).join(Iterables.filter(arg1, x -> x != null));
    }
  }
  public static class SplitFunction implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> args) {
      List ret = new ArrayList();
      Object o1 = args.get(0);
      if(o1 != null) {
        String arg1 = o1.toString();
        String delim = (String) args.get(1);
        Iterables.addAll(ret, Splitter.on(delim).split(arg1));
      }
      return ret;
    }
  }

  public static class GetLast implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> args) {
      List<Object> arg1 = (List<Object>) args.get(0);
      return Iterables.getLast(arg1, null);
    }
  }
  public static class GetFirst implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> args) {
      List<Object> arg1 = (List<Object>) args.get(0);
      return Iterables.getFirst(arg1, null);
    }
  }

  public static class Get implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> args) {
      List<Object> arg1 = (List<Object>) args.get(0);
      int offset = (Integer) args.get(1);
      if(offset < arg1.size()) {
        return Iterables.get(arg1, offset);
      }
      return null;
    }
  }
}
