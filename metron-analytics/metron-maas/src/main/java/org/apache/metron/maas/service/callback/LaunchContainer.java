package org.apache.metron.maas.service.callback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.metron.maas.config.ModelRequest;
import org.apache.metron.maas.service.runner.Runner.RunnerOptions;

import java.nio.ByteBuffer;
import java.util.*;

public class LaunchContainer implements Runnable {
  private static final Log LOG = LogFactory.getLog(LaunchContainer.class);
  private ModelRequest request;
  private Container container;
  private Path scriptPath;
  private Configuration conf ;
  private NMClientAsync nmClientAsync;
  private ContainerRequestListener containerListener;
  private String zkQuorum;
  private String zkRoot;
  private ByteBuffer allTokens;

  public LaunchContainer( Path scriptPath
                        , Configuration conf
                        , String zkQuorum
                        , String zkRoot
                        , NMClientAsync nmClientAsync
                        , ContainerRequestListener containerListener
                        , ModelRequest request
                        , Container container
                        , ByteBuffer allTokens
                        )
  {
    this.allTokens = allTokens;
    this.zkQuorum = zkQuorum;
    this.zkRoot = zkRoot;
    this.scriptPath = scriptPath;
    this.request = request;
    this.container = container;
    this.conf = conf;
    this.nmClientAsync = nmClientAsync;
    this.containerListener = containerListener;
  }

  @Override
  public void run() {
    LOG.info("Setting up container launch container for containerid="
            + container.getId());

    // Set the local resources
    Map<String, LocalResource> localResources = new HashMap<>();
    String script = localizeResources(localResources, scriptPath);
    assert script != null;

    String modelScript = localizeResources(localResources, new Path(request.getPath()));

    // The container for the eventual shell commands needs its own local
    // resources too.
    // In this scenario, if a shell script is specified, we need to have it
    // copied and made available to the container.
      /*if (!scriptPath.isEmpty()) {
        Path renamedScriptPath = null;
        if (Shell.WINDOWS) {
          renamedScriptPath = new Path(scriptPath + ".bat");
        } else {
          renamedScriptPath = new Path(scriptPath + ".sh");
        }

        try {
          // rename the script file based on the underlying OS syntax.
          renameScriptFile(renamedScriptPath);
        } catch (Exception e) {
          LOG.error(
                  "Not able to add suffix (.bat/.sh) to the shell script filename",
                  e);
          return;
        }

        URL yarnUrl = null;
        try {
          yarnUrl = ConverterUtils.getYarnUrlFromURI(
                  new URI(renamedScriptPath.toString()));
        } catch (URISyntaxException e) {
          LOG.error("Error when trying to use shell script path specified"
                  + " in env, path=" + renamedScriptPath, e);
          return;
        }
        LocalResource shellRsrc = LocalResource.newInstance(yarnUrl,
                LocalResourceType.FILE, LocalResourceVisibility.APPLICATION,
                shellScriptPathLen, shellScriptPathTimestamp);
        localResources.put(Shell.WINDOWS ? ExecBatScripStringtPath :
                ExecShellStringPath, shellRsrc);
        shellCommand = Shell.WINDOWS ? windows_command : linux_bash_command;
      }*/

    // Set the necessary command to execute on the allocated container
    Map<String, String> env = new HashMap<>();
    // For example, we could setup the classpath needed.
    // Assuming our classes or jars are available as local resources in the
    // working directory from which the command will be run, we need to append
    // "." to the path.
    // By default, all the hadoop specific classpaths will already be available
    // in $CLASSPATH, so we should be careful not to overwrite it.
    String classPathEnv = "$CLASSPATH:./*:";
    env.put("CLASSPATH", classPathEnv);

    // Construct the command to be executed on the launched container
    String command = "./" + script
            + RunnerOptions.toArgs(RunnerOptions.CONTAINER_ID.of(container.getId().getContainerId() + "")
                                  ,RunnerOptions.ZK_QUORUM.of(zkQuorum)
                                  ,RunnerOptions.ZK_ROOT.of(zkRoot)
                                  ,RunnerOptions.SCRIPT.of(modelScript)
                                  ,RunnerOptions.NAME.of(request.getName())
                                  ,RunnerOptions.VERSION.of(request.getVersion())
                                  )
            + " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
            + " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr";
    List<String> commands = new ArrayList<String>();
    commands.add(command);


    // Set up ContainerLaunchContext, setting local resource, environment,
    // command and token for constructor.

    // Note for tokens: Set up tokens for the container too. Today, for normal
    // shell commands, the container in distribute-shell doesn't need any
    // tokens. We are populating them mainly for NodeManagers to be able to
    // download anyfiles in the distributed file-system. The tokens are
    // otherwise also useful in cases, for e.g., when one is running a
    // "hadoop dfs" command inside the distributed shell.
    ContainerLaunchContext ctx = ContainerLaunchContext.newInstance(
            localResources, env, commands, null, allTokens.duplicate(), null);
    //TODO: Add container to listener so it can be removed
    nmClientAsync.startContainerAsync(container, ctx);
  }

  public String localizeResources(Map<String, LocalResource> resources, Path scriptLocation) {
    try {
      FileSystem fs = scriptLocation.getFileSystem(conf);
      String script = null;
      for (RemoteIterator<LocatedFileStatus> it = fs.listFiles(scriptLocation, true);it.hasNext();) {
        LocatedFileStatus status = it.next();
        URL url = ConverterUtils.getYarnUrlFromURI( status.getPath().toUri());
        LocalResource resource = LocalResource.newInstance(url,
                LocalResourceType.FILE, LocalResourceVisibility.APPLICATION,
                status.getLen(), status.getModificationTime());
        String name = status.getPath().getName();
        if(name.endsWith(".sh")) {
          script = name;
        }
        resources.put(name, resource);
      }
      return script;
    }
    catch(Exception e) {
      return null;
    }
  }
}
