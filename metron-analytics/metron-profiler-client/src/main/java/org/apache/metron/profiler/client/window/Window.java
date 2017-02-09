package org.apache.metron.profiler.client.window;

import com.google.common.collect.Iterables;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Window {
  private Function<Long, Long> startMillis ;
  private Function<Long, Long> endMillis;
  private List<Function<Long, Predicate<Long>>> includes = new ArrayList<>();
  private List<Function<Long, Predicate<Long>>> excludes = new ArrayList<>();
  private int binWidth;
  private int skipDistance = Integer.MAX_VALUE;

  public long getStartMillis(long now) {
    return startMillis.apply(now);
  }

  void setStartMillis(Function<Long, Long> startMillis) {
    this.startMillis = startMillis;
  }

  public Long getEndMillis(long now) {
    return endMillis.apply(now);
  }

  void setEndMillis(Function<Long, Long> endMillis) {
    this.endMillis = endMillis;
  }

  public Iterable<Predicate<Long>> getIncludes(long now) {
    return Iterables.transform(includes, f -> f.apply(now));
  }

  void setIncludes(List<Function<Long, Predicate<Long>>> includes) {
    this.includes = includes;
  }

  public Iterable<Predicate<Long>> getExcludes(long now){
    return Iterables.transform(excludes, f -> f.apply(now));
  }

  void setExcludes(List<Function<Long, Predicate<Long>>> excludes) {
    this.excludes = excludes;
  }

  public long getBinWidth() {
    return binWidth;
  }

  void setBinWidth(int binWidth) {
    this.binWidth = binWidth;
  }

  public long getSkipDistance() {
    return skipDistance;
  }

  void setSkipDistance(int skipDistance) {
    this.skipDistance = skipDistance;
  }

  public List<Interval> toIntervals(long now) {
    List<Interval> intervals = new ArrayList<>();
    long startMillis = getStartMillis(now);
    long endMillis = getEndMillis(now);
    Iterable<Predicate<Long>> includes = getIncludes(now);
    Iterable<Predicate<Long>> excludes = getExcludes(now);
    for(long left = startMillis;left + binWidth <= endMillis;left += skipDistance) {
      Interval interval = new Interval(left, left + binWidth);
      boolean include = includes.iterator().hasNext()?false:true;
      for(Predicate<Long> inclusionPredicate : includes) {
        include |= inclusionPredicate.test(left);
      }
      if(include) {
        for(Predicate<Long> exclusionPredicate : excludes) {
          include &= !exclusionPredicate.test(left);
        }
      }
      if(include) {
        intervals.add(interval);
      }
    }
    return intervals;
  }
}
