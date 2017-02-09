package org.apache.metron.profiler.client.window;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.apache.metron.common.dsl.ErrorListener;
import org.apache.metron.common.dsl.Token;
import org.apache.metron.common.utils.ConversionUtils;
import org.apache.metron.profiler.client.window.generated.WindowBaseListener;
import org.apache.metron.profiler.client.window.generated.WindowLexer;
import org.apache.metron.profiler.client.window.generated.WindowParser;
import org.apache.metron.profiler.client.window.predicates.DayPredicates;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class WindowProcessor extends WindowBaseListener {
  private Stack<Token<?>> stack;
  private static final Token<Object> LIST_MARKER = new Token<>(null, Object.class);
  private static final Token<Object> DAY_SPECIFIER_MARKER = new Token<>(null, Object.class);
  private Window window;

  public WindowProcessor() {
    this.stack = new Stack<>();
    this.window = new Window();
  }

  public Window getWindow() {
    return window;
  }

  private void enterList() {
    stack.push(LIST_MARKER);
  }

  private List<Function<Long, Predicate<Long>>> getPredicates() {
    LinkedList<Function<Long, Predicate<Long>>> predicates = new LinkedList<>();
    while (true) {
      Token<?> token = stack.pop();
      if (token == LIST_MARKER) {
        break;
      } else {
        predicates.addFirst((Function<Long, Predicate<Long>>) token.getValue());
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
  public void exitIdentifier(WindowParser.IdentifierContext ctx) {
    stack.push(new Token<>(ctx.getText(), String.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void enterSpecifier(WindowParser.SpecifierContext ctx) {
    stack.push(DAY_SPECIFIER_MARKER);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitSpecifier(WindowParser.SpecifierContext ctx) {
    LinkedList<String> args = new LinkedList<>();
    while (true) {
      Token<?> token = stack.pop();
      if (token == DAY_SPECIFIER_MARKER) {
        break;
      } else {
        args.addFirst((String) token.getValue());
      }
    }
    String specifier = args.removeFirst();
    List<String> arg = args.size() > 0?args:new ArrayList<>();
    Function<Long, Predicate<Long>> predicate = null;
    if(specifier.equals("THIS DAY OF THE WEEK")) {
      predicate = now -> DayPredicates.dayOfWeekPredicate(DayPredicates.getDayOfWeek(now));
    }
    else {
      final Predicate<Long> dayOfWeekPredicate = DayPredicates.create(specifier, arg);
      predicate = now -> dayOfWeekPredicate;
    }
    stack.push(new Token<>(predicate, Function.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitDay_specifier(WindowParser.Day_specifierContext ctx) {
    String specifier = ctx.getText().toUpperCase();
    if(specifier.endsWith("S")) {
      specifier = specifier.substring(0, specifier.length() - 1);
    }
    stack.push(new Token<>(specifier, String.class));
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void enterExcluding_specifier(WindowParser.Excluding_specifierContext ctx) {
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
  public void exitExcluding_specifier(WindowParser.Excluding_specifierContext ctx) {
    window.setExcludes(getPredicates());
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void enterIncluding_specifier(WindowParser.Including_specifierContext ctx) {
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
  public void exitIncluding_specifier(WindowParser.Including_specifierContext ctx) {
    window.setIncludes(getPredicates());
  }

  private void setFromTo(int from, int to) {
    window.setEndMillis(now -> now - to);
    window.setStartMillis(now -> now - from);
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
    setFromTo(from, to);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitFromDuration(org.apache.metron.profiler.client.window.generated.WindowParser.FromDurationContext ctx) {
    Token<?> timeInterval = stack.pop();
    Integer from = (Integer)timeInterval.getValue();
    setFromTo(from, 0);
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
    window.setStartMillis(now -> now - width);
    window.setEndMillis(now -> now);
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
    stack.push(new Token<>((int)unit.toMillis(duration), Integer.class));
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
      case "HOUR":
      case "HOURS":
        stack.push(new Token<>(TimeUnit.HOURS, TimeUnit.class));
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

  public static Window parse(String statement) {
    if (statement == null || isEmpty(statement.trim())) {
      return null;
    }

    ANTLRInputStream input = new ANTLRInputStream(statement);
    WindowLexer lexer = new WindowLexer(input);
    lexer.removeErrorListeners();
    lexer.addErrorListener(new ErrorListener());
    TokenStream tokens = new CommonTokenStream(lexer);
    WindowParser parser = new WindowParser(tokens);

    WindowProcessor treeBuilder = new WindowProcessor();
    parser.addParseListener(treeBuilder);
    parser.removeErrorListeners();
    parser.addErrorListener(new ErrorListener());
    parser.window();
    return treeBuilder.getWindow();
  }
}
