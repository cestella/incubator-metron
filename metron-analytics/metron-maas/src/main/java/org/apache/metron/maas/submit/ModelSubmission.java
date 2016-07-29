package org.apache.metron.maas.submit;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.metron.maas.config.Action;
import org.apache.metron.maas.config.MaaSConfig;
import org.apache.metron.maas.config.ModelRequest;
import org.apache.metron.maas.service.ConfigUtil;
import org.apache.metron.maas.service.queue.Queue;
import org.apache.metron.maas.service.queue.ZKQueue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class ModelSubmission {
  public enum ModelSubmissionOptions {
    HELP("h", code -> {
      Option o = new Option(code, "help", false, "This screen");
      o.setRequired(false);
      return o;
    })
    ,ZK_QUORUM("zq", code -> {
      Option o = new Option(code, "zk_quorum", true, "Zookeeper Quorum");
      o.setRequired(true);
      return o;
    })
    ,ZK_ROOT("zr", code -> {
      Option o = new Option(code, "zk_root", true, "Zookeeper Root");
      o.setRequired(true);
      return o;
    })
    ,LOCAL_MODEL_PATH("lmp", code -> {
      Option o = new Option(code, "local_model_path", true, "Model Path (local)");
      o.setRequired(true);
      return o;
    })
    ,HDFS_MODEL_PATH("hmp", code -> {
      Option o = new Option(code, "hdfs_model_path", true, "Model Path (HDFS)");
      o.setRequired(true);
      return o;
    })
    ,NAME("n", code -> {
      Option o = new Option(code, "name", true, "Model Name");
      o.setRequired(true);
      return o;
    })
    ,VERSION("v", code -> {
      Option o = new Option(code, "version", true, "Model version");
      o.setRequired(true);
      return o;
    })
    ,NUM_INSTANCES("ni", code -> {
      Option o = new Option(code, "num_instances", true, "Number of model instances");
      o.setRequired(true);
      return o;
    })
    ,MEMORY("m", code -> {
      Option o = new Option(code, "memory", true, "Memory for container");
      o.setRequired(true);
      return o;
    })
    ;
    Option option;
    String shortCode;
    ModelSubmissionOptions(String shortCode
                 , Function<String, Option> optionHandler
                 ) {
      this.shortCode = shortCode;
      this.option = optionHandler.apply(shortCode);
    }

    public boolean has(CommandLine cli) {
      return cli.hasOption(shortCode);
    }

    public String get(CommandLine cli) {
      return cli.getOptionValue(shortCode);
    }
    public String get(CommandLine cli, String def) {
      return has(cli)?cli.getOptionValue(shortCode):def;
    }

    public Map.Entry<ModelSubmissionOptions, String> of(String value) {
      if(option.hasArg()) {
        return new AbstractMap.SimpleEntry<>(this, value);
      }
      return new AbstractMap.SimpleEntry<>(this, null);
    }

    public static String toArgs(Map.Entry<ModelSubmissionOptions, String> ... arg) {
      return
      Joiner.on(" ").join(Iterables.transform(Arrays.asList(arg)
                                             , a -> "-" + a.getKey().option.getOpt()
                                                  + a.getValue() == null?"":(" " + a.getValue())
                                             )
                         );

    }
    public static CommandLine parse(CommandLineParser parser, String[] args) throws ParseException {
      try {
        CommandLine cli = parser.parse(getOptions(), args);
        if(HELP.has(cli)) {
          printHelp();
          System.exit(0);
        }
        return cli;
      } catch (ParseException e) {
        System.err.println("Unable to parse args: " + Joiner.on(' ').join(args));
        e.printStackTrace(System.err);
        printHelp();
        throw e;
      }
    }

    public static void printHelp() {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "ModelSubmission", getOptions());
    }

    public static Options getOptions() {
      Options ret = new Options();
      for(ModelSubmissionOptions o : ModelSubmissionOptions.values()) {
        ret.addOption(o.option);
      }
      return ret;
    }
  }

  public void execute(FileSystem fs, String... argv) throws Exception {
    CommandLine cli = ModelSubmissionOptions.parse(new PosixParser(), argv);
    ModelRequest request = new ModelRequest() {{
      setName(ModelSubmissionOptions.NAME.get(cli));
      setAction(Action.ADD);
      setVersion(ModelSubmissionOptions.VERSION.get(cli));
      setNumInstances(Integer.parseInt(ModelSubmissionOptions.NUM_INSTANCES.get(cli)));
      setMemory(Integer.parseInt(ModelSubmissionOptions.MEMORY.get(cli)));
      setPath(ModelSubmissionOptions.HDFS_MODEL_PATH.get(cli));
    }};
    CuratorFramework client = null;
    try {
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
      client = CuratorFrameworkFactory.newClient(ModelSubmissionOptions.ZK_QUORUM.get(cli), retryPolicy);
      client.start();
      MaaSConfig config = ConfigUtil.INSTANCE.read(client, ModelSubmissionOptions.ZK_ROOT.get(cli), MaaSConfig.class);

      if (ModelSubmissionOptions.LOCAL_MODEL_PATH.has(cli)) {
        File localDir = new File(ModelSubmissionOptions.LOCAL_MODEL_PATH.get(cli));
        Path hdfsPath = new Path(ModelSubmissionOptions.HDFS_MODEL_PATH.get(cli));
        updateHDFS(fs, localDir, hdfsPath);
      }
      Queue queue = config.createQueue(ImmutableMap.of(ZKQueue.ZK_CLIENT, client));
      queue.enqueue(request);
    }
    finally {
      if(client != null) {
        client.close();
      }
    }
  }

  public static void main(String... argv) throws Exception {
    FileSystem fs = FileSystem.get(new Configuration());
    ModelSubmission submission = new ModelSubmission();
    submission.execute(fs, argv);
  }

  public static void updateHDFS(FileSystem fs, File localDir, Path hdfsPath) throws IOException {
    if(localDir.exists() && localDir.isDirectory()) {
      if(!fs.exists(hdfsPath)) {
        fs.mkdirs(hdfsPath);
      }
      for(File f : localDir.listFiles()) {
        Path p = new Path(hdfsPath, f.getName());
        FSDataOutputStream out = fs.create(p);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
    }
  }
}
