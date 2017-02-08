package org.apache.metron.profiler.client.window.predicates;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

public enum DayPredicates implements Predicate<Long> {
  SUNDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 1),
  MONDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 2),
  TUESDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 3),
  WEDNESDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 4),
  THURSDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 5),
  FRIDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 6),
  SATURDAY( ts -> toCalendar(ts).get(Calendar.DAY_OF_WEEK) == 7),
  WEEKDAY( ts -> {
    int dow = toCalendar(ts).get(Calendar.DAY_OF_WEEK);
    return dow > 1 && dow < 7;
  }),
  WEEKEND( ts -> {
    int dow = toCalendar(ts).get(Calendar.DAY_OF_WEEK);
    return dow == 1 || dow == 7;
  })
  ;
  Predicate<Long> predicate;
  DayPredicates(Predicate<Long> predicate) {
    this.predicate = predicate;
  }

  private static Calendar toCalendar(Long ts) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date(ts));
    return c;
  }

  @Override
  public boolean test(Long ts) {
    return predicate.test(ts);
  }

}
