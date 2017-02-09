package org.apache.metron.profiler.client.window.predicates;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DateSpecifierPredicate implements Predicate<Long> {
  final static ThreadLocal<SimpleDateFormat> FORMAT = new ThreadLocal<SimpleDateFormat>() {

    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat("yyyy/MM/dd");
    }
  };
  Date date;
  public DateSpecifierPredicate(List<String> args) {
    if(args.size() == 1) {
      //just the date, use the default.
      try {
        date = FORMAT.get().parse(args.get(0));
      } catch (ParseException e) {
        throw new IllegalStateException("Unable to parse " + args.get(0) + " as a date using " + FORMAT.get().toPattern());
      }
    }
    else if(args.size() == 0){
      throw new IllegalStateException("You must specify at least a date and optionally a format");
    }
    else {
      String dateStr = args.get(0);
      String format =  args.get(1);
      try {
        date = new SimpleDateFormat(format).parse(dateStr);
      } catch (ParseException e) {
        throw new IllegalStateException("Unable to parse " + dateStr + " as a date using " + format);
      }
    }
  }

  @Override
  public boolean test(Long ts) {
    return DateUtils.isSameDay(new Date(ts), date);
  }
}
