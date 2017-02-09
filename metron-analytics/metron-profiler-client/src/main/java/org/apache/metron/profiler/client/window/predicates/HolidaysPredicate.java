package org.apache.metron.profiler.client.window.predicates;

import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HolidaysPredicate implements Predicate<Long> {
  HolidayManager manager;
  String[] args;
  public HolidaysPredicate(List<String> args) {
    if(args == null || args.size() == 0) {
      this.manager = HolidayManager.getInstance();
      this.args = new String[]{};
    }
    else {
      String code = args.get(0);
      this.args = args.size() == 1 ? new String[]{} : new String[args.size() - 1];
      Optional<HolidayCalendar> calendar = getCalendar(code);
      if(calendar.isPresent()) {
        this.manager = HolidayManager.getInstance(ManagerParameters.create(calendar.get()));
      }
      else {
        this.manager = HolidayManager.getInstance(ManagerParameters.create(code));
      }
      for (int i = 1; i < args.size(); ++i) {
        this.args[i - 1] = args.get(i);
      }
    }
  }

  private static Optional<HolidayCalendar> getCalendar(String code) {
    for(HolidayCalendar cal : HolidayCalendar.values()) {
      if(cal.getId().equalsIgnoreCase(code) || cal.name().equalsIgnoreCase(code)) {
        return Optional.of(cal);
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean test(Long ts) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date(ts));
    return manager.isHoliday(c, args);
  }
}
