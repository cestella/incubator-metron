package org.apache.metron.threatintel.triage.predicate;

public class PredicateToken<T> {
  T value;
  Class<T> underlyingType;
  public PredicateToken(T value, Class<T> clazz) {
    this.value = value;
    this.underlyingType = clazz;
  }
  public T getValue() {
    return value;
  }
  public Class<T> getUnderlyingType() {
    return underlyingType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PredicateToken<?> predicateToken = (PredicateToken<?>) o;

    if (getValue() != null ? !getValue().equals(predicateToken.getValue()) : predicateToken.getValue() != null) return false;
    return getUnderlyingType() != null ? getUnderlyingType().equals(predicateToken.getUnderlyingType()) : predicateToken.getUnderlyingType() == null;

  }

  @Override
  public int hashCode() {
    int result = getValue() != null ? getValue().hashCode() : 0;
    result = 31 * result + (getUnderlyingType() != null ? getUnderlyingType().hashCode() : 0);
    return result;
  }
}
