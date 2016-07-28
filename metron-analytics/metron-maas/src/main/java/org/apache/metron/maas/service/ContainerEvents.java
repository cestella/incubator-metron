package org.apache.metron.maas.service;

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.classification.InterfaceAudience;

@VisibleForTesting
@InterfaceAudience.Private
public enum ContainerEvents {
  DS_APP_ATTEMPT_START, DS_APP_ATTEMPT_END, DS_CONTAINER_START, DS_CONTAINER_END
}
