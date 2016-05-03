package org.apache.metron.threatintel.triage;

import org.apache.metron.threatintel.triage.predicate.VariableResolver;

import java.util.Map;

public class MapVariableResolver implements VariableResolver {
  Map variableMapping;
  public MapVariableResolver(Map variableMapping) {
    this.variableMapping = variableMapping;
  }
  @Override
  public String resolve(String variable) {
    Object o = variableMapping.get(variable);
    return o == null?null:o.toString();
  }
}
