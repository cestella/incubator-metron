package org.apache.metron.profiler.client.stellar;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.ParseException;
import org.apache.metron.common.dsl.Stellar;
import org.apache.metron.common.dsl.StellarFunction;
import org.apache.metron.common.utils.ConversionUtils;
import org.apache.metron.profiler.ProfilePeriod;
import org.apache.metron.profiler.client.window.Window;
import org.apache.metron.profiler.client.window.WindowProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Stellar(
      namespace="PROFILE",
      name="WINDOW",
      description="The profiler periods associated with a fixed lookback starting from now.",
      params={
        "windowSelector - The statement specifying the window to select",
        "now - Optional - The timestamp to use for now.",
        "config_overrides - Optional - Map (in curly braces) of name:value pairs, each overriding the global config parameter " +
                "of the same name. Default is the empty Map, meaning no overrides."
      },
      returns="The selected profile measurement periods.  These are ProfilePeriod objects."
)
public class WindowLookback implements StellarFunction {

  private Cache<String, Window> windowCache;

  @Override
  public Object apply(List<Object> args, Context context) throws ParseException {
    Optional<Map> configOverridesMap = Optional.empty();
    long now = System.currentTimeMillis();
    String windowSelector = Util.getArg(0, String.class, args);
    if(args.size() > 1) {
      Optional<Object> arg2 = Optional.ofNullable(args.get(1));
      Optional<Object> mapArg = args.size() > 2?Optional.ofNullable(args.get(2)):Optional.empty();
      if(!mapArg.isPresent() && arg2.isPresent() && arg2.get() instanceof Map) {
        mapArg = arg2;
      }

      if(arg2.isPresent() && arg2.get() instanceof Number) {
        now = ConversionUtils.convert(arg2.get(), Long.class);
      }

      if(mapArg.isPresent()) {
        Map rawMap = ConversionUtils.convert(mapArg.get(), Map.class);
        configOverridesMap = rawMap == null || rawMap.isEmpty() ? Optional.empty() : Optional.of(rawMap);
      }

    }
    Map<String, Object> effectiveConfigs = Util.getEffectiveConfig(context, configOverridesMap.orElse(null));
    Long tickDuration = ProfilerConfig.PROFILER_PERIOD.get(effectiveConfigs, Long.class);
    TimeUnit tickUnit = TimeUnit.valueOf(ProfilerConfig.PROFILER_PERIOD_UNITS.get(effectiveConfigs, String.class));
    Window w = null;
    try {
      w = windowCache.get(windowSelector, () -> WindowProcessor.parse(windowSelector));
    } catch (ExecutionException e) {
      throw new IllegalStateException("Unable to parse " + windowSelector + ": " + e.getMessage(), e);
    }
    long end = w.getEndMillis(now);
    long start = w.getStartMillis(now);
    IntervalPredicate<ProfilePeriod> intervalSelector = new IntervalPredicate<>(period -> period.getStartTimeMillis()
                                                                               , w.toIntervals(now)
                                                                               , ProfilePeriod.class
                                                                               );
    return ProfilePeriod.visitPeriods(start, end, tickDuration, tickUnit, Optional.of(intervalSelector), period -> period);
  }

  @Override
  public void initialize(Context context) {
    windowCache = CacheBuilder.newBuilder()
                              .maximumSize(200)
                              .expireAfterAccess(10, TimeUnit.MINUTES)
                              .build();
  }

  @Override
  public boolean isInitialized() {
    return windowCache != null;
  }
}
