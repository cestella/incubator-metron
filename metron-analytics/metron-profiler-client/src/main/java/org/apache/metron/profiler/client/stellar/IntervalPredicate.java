package org.apache.metron.profiler.client.stellar;

import org.joda.time.Interval;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class IntervalPredicate implements Predicate<Long> {
  private final List<Interval> intervals;
  public IntervalPredicate(List<Interval> intervals) {
    this.intervals = intervals;
  }

  private boolean containsInclusive(Interval interval, long ts) {
    return interval.contains(ts) || interval.getEndMillis() == ts;
  }


  public static final Comparator<Interval> INTERVAL_COMPARATOR = new Comparator<Interval>() {
    @Override
    public int compare(Interval o1, Interval o2) {
      if(o1.getStartMillis() == o2.getStartMillis() && o1.getEndMillis() == o2.getEndMillis()) {
        return 0;
      }
      else {
        int ret = Long.compare(o1.getStartMillis(), o2.getStartMillis());
        if(ret == 0) {
          return Long.compare(o1.getEndMillis(), o2.getEndMillis());
        }
        else {
          return ret;
        }
      }
    }
  };

  @Override
  public boolean test(Long ts) {
    int pos = Collections.binarySearch(intervals, new Interval(ts, ts), INTERVAL_COMPARATOR);
    if(pos < 0) {
      pos = -pos - 1;
    }

    Optional<Interval> right = pos >= 0 && pos < intervals.size()?Optional.of(intervals.get(pos)):Optional.empty();
    Optional<Interval> left = pos - 1 >= 0 && pos - 1 < intervals.size()?Optional.of(intervals.get(pos - 1)):Optional.empty();
    return (right.isPresent()?containsInclusive(right.get(),ts):false) || (left.isPresent()?containsInclusive(left.get(),ts):false);
  }
}
