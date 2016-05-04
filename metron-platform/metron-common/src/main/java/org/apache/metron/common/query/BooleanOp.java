package org.apache.metron.common.query;

public interface BooleanOp {
  boolean op(boolean left, boolean right);
}
