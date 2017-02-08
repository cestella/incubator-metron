package org.apache.metron.profiler.client.window;

import java.util.List;
import java.util.function.Predicate;

public class Window {
  private long startMillis;
  private Long endMillis;
  private List<Predicate<Long>> includes;
  private List<Predicate<Long>> excludes;
  private int binWidth;
  private int skipDistance;

  public long getStartMillis() {
    return startMillis;
  }

  void setStartMillis(long startMillis) {
    this.startMillis = startMillis;
  }

  public Long getEndMillis() {
    return endMillis;
  }

  void setEndMillis(Long endMillis) {
    this.endMillis = endMillis;
  }

  public List<Predicate<Long>> getIncludes() {
    return includes;
  }

  void setIncludes(List<Predicate<Long>> includes) {
    this.includes = includes;
  }

  public List<Predicate<Long>> getExcludes() {
    return excludes;
  }

  void setExcludes(List<Predicate<Long>> excludes) {
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
}
