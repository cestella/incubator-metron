package org.apache.metron.profiler.client.window;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Window {
  private long startMillis;
  private Optional<Long> endMillis;
  private List<Predicate> includes;
  private List<Predicate> excludes;
  private long binWidth;
  private long skipDistance;

  public long getStartMillis() {
    return startMillis;
  }

  void setStartMillis(long startMillis) {
    this.startMillis = startMillis;
  }

  public Optional<Long> getEndMillis() {
    return endMillis;
  }

  void setEndMillis(Optional<Long> endMillis) {
    this.endMillis = endMillis;
  }

  public List<Predicate> getIncludes() {
    return includes;
  }

  void setIncludes(List<Predicate> includes) {
    this.includes = includes;
  }

  public List<Predicate> getExcludes() {
    return excludes;
  }

  void setExcludes(List<Predicate> excludes) {
    this.excludes = excludes;
  }

  public long getBinWidth() {
    return binWidth;
  }

  void setBinWidth(long binWidth) {
    this.binWidth = binWidth;
  }

  public long getSkipDistance() {
    return skipDistance;
  }

  void setSkipDistance(long skipDistance) {
    this.skipDistance = skipDistance;
  }
}
