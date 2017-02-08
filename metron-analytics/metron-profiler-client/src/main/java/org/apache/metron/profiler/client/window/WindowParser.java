package org.apache.metron.profiler.client.window;

import org.apache.metron.common.dsl.Token;
import org.apache.metron.profiler.client.window.generated.WindowBaseListener;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class WindowParser extends WindowBaseListener {
  private final Date now;
  private Stack<Token<?>> stack;
  private static final Token<Object> LIST_MARKER = new Token<>(null, Object.class);

  public WindowParser(Date now) {
    this.now = now;
  }

  public WindowParser() {
    this(new Date());
  }

  private void enterList() {
    stack.push(LIST_MARKER);
  }

  private List<Predicate> getPredicates() {
    LinkedList<Predicate> predicates = new LinkedList<>();
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
  public void enterExcluding(org.apache.metron.profiler.client.window.generated.WindowParser.ExcludingContext ctx) {
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
    super.exitExcluding(ctx);
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
    super.enterIncluding(ctx);
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
    super.exitIncluding(ctx);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitSpecifier_list(org.apache.metron.profiler.client.window.generated.WindowParser.Specifier_listContext ctx) {
    super.exitSpecifier_list(ctx);
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
    super.exitFromToDuration(ctx);
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
    super.exitToDuration(ctx);
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
    super.exitSkipDistance(ctx);
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
    super.exitBinWidth(ctx);
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
    super.exitTimeInterval(ctx);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <p>The default implementation does nothing.</p>
   *
   * @param ctx
   */
  @Override
  public void exitTimeIntervalNow(org.apache.metron.profiler.client.window.generated.WindowParser.TimeIntervalNowContext ctx) {
    super.exitTimeIntervalNow(ctx);
  }
}
