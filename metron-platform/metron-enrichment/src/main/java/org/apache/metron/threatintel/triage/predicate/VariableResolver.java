package org.apache.metron.threatintel.triage.predicate;

public interface VariableResolver {
  String resolve(String variable);
}
