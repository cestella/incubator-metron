package org.apache.metron.profiler.client.window;

import org.apache.metron.common.dsl.Token;
import org.apache.metron.common.utils.ConversionUtils;
import org.apache.metron.profiler.client.window.generated.WindowBaseListener;
import org.apache.metron.profiler.client.window.predicates.DayPredicates;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class WindowParser extends WindowBaseListener {
  private final Date now;
  private Stack<Token<?>> stack;
  private static final Token<Object> LIST_MARKER = new Token<>(null, Object.class);
  private Window window;

  public WindowParser(Date now) {
    this.now = now;
    this.window = new Window();
  }

  public WindowParser() {
    this(new Date());
  }

  private void enterList() {
    stack.push(LIST_MARKER);
  }

  private List<Predicate<Long>> getPredicates() {
    LinkedList<Predicate<Long>> predicates = new LinkedList<>();
    while (true) {
      Token<?> token = stack.pop();
      if (token == LIST_MARKER) {
        break;
      } else {
        predicates.addFirst((Predicate) token.getValue());
      }
    }
    return predicates;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitDaySpecifier(org.apache.metron.profiler.client.window.generated.WindowParser.DaySpecifierContext ctx) {
    String specifier = ctx.getText().toUpperCase();
    if(specifier.endsWith("s") || specifier.endsWith("S")) {
      specifier = specifier.substring(0, specifier.length() - 1);
    }
    Predicate<Long> predicate = DayPredicates.valueOf(specifier);
    stack.push(new Token<>(predicate, Predicate.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void enterExcluding(org.apache.metron.profiler.client.window.generated.WindowParser.ExcludingContext ctx) {
    enterList();
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitExcluding(org.apache.metron.profiler.client.window.generated.WindowParser.ExcludingContext ctx) {
    List<Predicate<Long>> exclusionList= getPredicates();
    window.setExcludes(exclusionList);

  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void enterIncluding(org.apache.metron.profiler.client.window.generated.WindowParser.IncludingContext ctx) {
    enterList();
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitIncluding(org.apache.metron.profiler.client.window.generated.WindowParser.IncludingContext ctx) {
    List<Predicate<Long>> inclusionList= getPredicates();
    window.setIncludes(inclusionList);
  }


  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitFromToDuration(org.apache.metron.profiler.client.window.generated.WindowParser.FromToDurationContext ctx) {
    Token<?> toInterval = stack.pop();
    Token<?> fromInterval = stack.pop();
    Integer to = (Integer)toInterval.getValue();
    Integer from = (Integer)fromInterval.getValue();
    window.setEndMillis(now.getTime() - from);
    window.setStartMillis(now.getTime() - from - to);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitToDuration(org.apache.metron.profiler.client.window.generated.WindowParser.ToDurationContext ctx) {
    Token<?> timeInterval = stack.pop();
    Integer width = (Integer)timeInterval.getValue();
    window.setEndMillis(now.getTime());
    window.setStartMillis(now.getTime() - width);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitSkipDistance(org.apache.metron.profiler.client.window.generated.WindowParser.SkipDistanceContext ctx) {
    Token<?> timeInterval = stack.pop();
    Integer width = (Integer)timeInterval.getValue();
    window.setSkipDistance(width);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitBinWidth(org.apache.metron.profiler.client.window.generated.WindowParser.BinWidthContext ctx) {
    Token<?> timeInterval = stack.pop();
    Integer width = (Integer)timeInterval.getValue();
    window.setBinWidth(width);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitTimeInterval(org.apache.metron.profiler.client.window.generated.WindowParser.TimeIntervalContext ctx) {
    Token<?> timeUnit = stack.pop();
    Token<?> timeDuration = stack.pop();
    int duration = ConversionUtils.convert(timeDuration.getValue(), Integer.class);
    TimeUnit unit = (TimeUnit) timeUnit.getValue();
    stack.push(new Token<>(unit.toMillis(duration), Long.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitTimeAmount(org.apache.metron.profiler.client.window.generated.WindowParser.TimeAmountContext ctx) {
    int duration = Integer.parseInt(ctx.getText());
    stack.push(new Token<>(duration, Integer.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitTimeUnit(org.apache.metron.profiler.client.window.generated.WindowParser.TimeUnitContext ctx) {
    switch(ctx.getText().toUpperCase()) {
      case "DAY":
      case "DAYS":
        stack.push(new Token<>(TimeUnit.DAYS, TimeUnit.class));
        break;
      case "MINUTE":
      case "MINUTES":
        stack.push(new Token<>(TimeUnit.MINUTES, TimeUnit.class));
        break;
      case "SECOND":
      case "SECONDS":
        stack.push(new Token<>(TimeUnit.SECONDS, TimeUnit.class));
        break;
    }
  }
}
