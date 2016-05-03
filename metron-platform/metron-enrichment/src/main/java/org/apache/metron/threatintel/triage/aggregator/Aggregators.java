package org.apache.metron.threatintel.triage.aggregator;

import com.google.common.collect.Iterables;
import org.apache.metron.threatintel.triage.Aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public enum Aggregators implements Aggregator {
   MAX( (numbers, config) -> accumulate(0d, (x,y) -> Math.max(x.doubleValue(),y.doubleValue()), numbers))
  ,MIN( (numbers, config) -> accumulate(0d, (x,y) -> Math.min(x.doubleValue(),y.doubleValue()), numbers))
  ,SUM( (numbers, config) -> accumulate(0d, (x,y) -> x.doubleValue() + y.doubleValue(), numbers))
  ,MEAN( (numbers, config) -> scale(SUM.aggregate(numbers, config), numbers, n -> true))
  ,POSITIVE_MEAN( (numbers, config) -> scale(SUM.aggregate(numbers, config), numbers, n -> n.doubleValue() > 0))
  ;
  Aggregator aggregator;
  Aggregators(Aggregator agg) {
    aggregator = agg;
  }
  public Aggregator getAggregator() {
    return aggregator;
  }

  private static double accumulate(double initial, BinaryOperator<Number> op, List<Number> list) {
    if(list.isEmpty()) {
      return 0d;
    }
    return list.stream()
               .reduce(initial, op)
               .doubleValue();
  }

  private static double scale(double numberToScale, List<Number> list, Predicate<Number> filterFunc) {
    double scale = list.stream().filter(filterFunc).count();
    if(scale < 1e-5) {
      scale = 1;
    }
    return numberToScale / scale;
  }

  @Override
  public Double aggregate(List<Number> scores, Map<String, Object> config) {
    return aggregator.aggregate(scores, config);
  }
}
