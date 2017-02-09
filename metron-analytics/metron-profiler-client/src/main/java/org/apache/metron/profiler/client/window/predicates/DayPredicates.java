package org.apache.metron.profiler.client.window.predicates;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public enum DayPredicates {
  SUNDAY( x -> dayOfWeekPredicate(1)),
  MONDAY( x -> dayOfWeekPredicate(2)),
  TUESDAY( x -> dayOfWeekPredicate(3)),
  WEDNESDAY( x -> dayOfWeekPredicate(4)),
  THURSDAY( x -> dayOfWeekPredicate(5)),
  FRIDAY( x -> dayOfWeekPredicate(6)),
  SATURDAY( x -> dayOfWeekPredicate(7)),
  WEEKDAY( x -> (ts -> {
    int dow = toCalendar(ts).get(Calendar.DAY_OF_WEEK);
    return dow > 1 && dow < 7;
  })),
  WEEKEND( x -> (ts -> {
    int dow = toCalendar(ts).get(Calendar.DAY_OF_WEEK);
    return dow == 1 || dow == 7;
  })),
  HOLIDAY(x -> new HolidaysPredicate(x))
  ;
  Function<List<String>, Predicate<Long>> predicateCreator;
  DayPredicates(Function<List<String>, Predicate<Long>> predicate) {
    this.predicateCreator = predicate;
  }

  private static Calendar toCalendar(Long ts) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date(ts));
    return c;
  }

  public static int getDayOfWeek(Long ts) {
    return toCalendar(ts).get(Calendar.DAY_OF_WEEK);
  }

  public static Predicate<Long> dayOfWeekPredicate(int dayOfWeek) {
    return ts -> getDayOfWeek(ts) == dayOfWeek;
  }

  public static Predicate<Long> create(String name, List<String> arg) {
    return DayPredicates.valueOf(name).predicateCreator.apply(arg);
  }

}
