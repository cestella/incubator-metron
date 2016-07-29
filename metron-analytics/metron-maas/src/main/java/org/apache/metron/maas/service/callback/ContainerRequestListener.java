package org.apache.metron.maas.service.callback;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.TimelineClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
import org.apache.metron.maas.service.yarn.YarnUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContainerRequestListener implements AMRMClientAsync.CallbackHandler, NMClientAsync.CallbackHandler  {

  private static final Log LOG = LogFactory.getLog(ContainerRequestListener.class);

  private Map<String, BlockingQueue<Container>> acceptedContainersByResource = Maps.newHashMap();
  private AMRMClientAsync amRMClient;
  @VisibleForTesting
  private UserGroupInformation appSubmitterUgi;
  private String domainId = null;
  private ConcurrentMap<ContainerId, Container> containers = new ConcurrentHashMap<>();
  @VisibleForTesting
  private TimelineClient timelineClient;
  private NMClientAsync nmClient;

  public ContainerRequestListener( TimelineClient timelineClient
                                 , UserGroupInformation appSubmitterUgi
                                 , String domainId
                                 )
  {
    this.domainId = domainId;
    this.appSubmitterUgi = appSubmitterUgi;
    this.timelineClient = timelineClient;
  }

  public void initialize(AMRMClientAsync amRMClient
                        , NMClientAsync nmClient
                        )
  {
    this.nmClient = nmClient;
    this.amRMClient = amRMClient;
  }



  public void removeContainers(int number, Resource characteristic) {

  }

  public void requestContainers(int number, Resource characteristic) {
    Priority pri = Priority.newInstance(0);
    BlockingQueue<Container> queue = getQueue(characteristic);
    AMRMClient.ContainerRequest request = new AMRMClient.ContainerRequest(characteristic, null, null, pri, true);
    for(int i = 0;i < number;++i) {
      amRMClient.addContainerRequest(request);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onContainersCompleted(List<ContainerStatus> completedContainers) {
    LOG.info("Got response from RM for container ask, completedCnt="
            + completedContainers.size());
    for (ContainerStatus containerStatus : completedContainers) {
      LOG.info("Got container status for containerID="
              + containerStatus.getContainerId() + ", state="
              + containerStatus.getState() + ", exitStatus="
              + containerStatus.getExitStatus() + ", diagnostics="
              + containerStatus.getDiagnostics());

      // non complete containers should not be here
      assert (containerStatus.getState() == ContainerState.COMPLETE);

      // increment counters for completed/failed containers
      int exitStatus = containerStatus.getExitStatus();
      if (0 != exitStatus) {
        // container failed
        if (ContainerExitStatus.ABORTED != exitStatus) {
          // shell script failed
          // counts as completed
        } else {
          // container was killed by framework, possibly preempted
          // we should re-try as the container was lost for some reason
          // we do not need to release the container as it would be done
          // by the RM
        }
      } else {
        // nothing to do
        // container completed successfully
        LOG.info("Container completed successfully." + ", containerId="
                + containerStatus.getContainerId());
      }
      if(timelineClient != null) {
        YarnUtils.INSTANCE.publishContainerEndEvent(
                timelineClient, containerStatus, domainId, appSubmitterUgi);
      }
    }
  }

  @Override
  public void onContainersAllocated(List<Container> allocatedContainers) {
    LOG.info("Got response from RM for container ask, allocatedCnt="
            + allocatedContainers.size());
    for (Container allocatedContainer : allocatedContainers) {
      BlockingQueue<Container> queue = getQueue(allocatedContainer.getResource());
      queue.add(allocatedContainer);
      LOG.info("Launching shell command on a new container."
              + ", containerId=" + allocatedContainer.getId()
              + ", containerNode=" + allocatedContainer.getNodeId().getHost()
              + ":" + allocatedContainer.getNodeId().getPort()
              + ", containerNodeURI=" + allocatedContainer.getNodeHttpAddress()
              + ", containerResourceMemory="
              + allocatedContainer.getResource().getMemory()
              + ", containerResourceVirtualCores="
              + allocatedContainer.getResource().getVirtualCores());
    }
  }

  public BlockingQueue<Container> getQueue(Resource resource) {
    synchronized(acceptedContainersByResource) {
      BlockingQueue<Container> containers = acceptedContainersByResource.get(resource.getMemory() + ":" + resource.getVirtualCores());
      if(containers == null) {
        containers = new LinkedBlockingDeque<>();
        acceptedContainersByResource.put(resource.getMemory() + ":" + resource.getVirtualCores(), containers);
      }
      return containers;
    }
  }


  @Override
  public void onShutdownRequest() {
  }

  @Override
  public void onNodesUpdated(List<NodeReport> updatedNodes) {}

  @Override
  public float getProgress() {
    // set progress to deliver to RM on next heartbeat
    float progress = 0;
    return progress;
  }

  @Override
  public void onError(Throwable e) {
    LOG.error(e.getMessage(), e);
  }

  @Override
    public void onContainerStopped(ContainerId containerId) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Succeeded to stop Container " + containerId);
      }
      containers.remove(containerId);
    }

    @Override
    public void onContainerStatusReceived(ContainerId containerId,
                                          ContainerStatus containerStatus) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Container Status: id=" + containerId + ", status=" +
                containerStatus);
      }
    }

    @Override
    public void onContainerStarted(ContainerId containerId,
                                   Map<String, ByteBuffer> allServiceResponse) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Succeeded to start Container " + containerId);
      }
      Container container = containers.get(containerId);
      if (container != null) {
        nmClient.getContainerStatusAsync(containerId, container.getNodeId());
      }
      if(timelineClient != null) {
        YarnUtils.INSTANCE.publishContainerStartEvent( timelineClient, container, domainId, appSubmitterUgi);
      }
    }

    @Override
    public void onStartContainerError(ContainerId containerId, Throwable t) {
      LOG.error("Failed to start Container " + containerId);
      containers.remove(containerId);
    }

    @Override
    public void onGetContainerStatusError(
            ContainerId containerId, Throwable t) {
      LOG.error("Failed to query the status of Container " + containerId);
    }

    @Override
    public void onStopContainerError(ContainerId containerId, Throwable t) {
      LOG.error("Failed to stop Container " + containerId);
      containers.remove(containerId);
    }
}
