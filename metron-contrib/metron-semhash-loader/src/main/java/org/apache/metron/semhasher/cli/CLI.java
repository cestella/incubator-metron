/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.semhasher.cli;

import com.google.common.base.Splitter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.common.utils.SerDeUtils;
import org.apache.metron.common.utils.cli.CLIOptions;
import org.apache.metron.common.utils.cli.OptionHandler;
import org.apache.metron.semhash.bin.LSHBinner;
import org.apache.metron.semhash.vector.VectorizerModel;
import org.apache.metron.semhash.SemanticHasher;
import org.apache.metron.semhasher.config.Config;
import org.apache.metron.semhash.transform.Context;
import org.apache.metron.semhasher.context.ContextUtil;
import org.apache.metron.semhasher.load.LoadUtil;
import org.apache.metron.semhasher.model.vectorization.Word2VecTrainer;
import org.apache.metron.semhasher.transform.TransformUtil;
import org.apache.metron.stellar.common.utils.ConversionUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CLI {
  public static final String NAME = "SemanticHasher";
  public enum HashOptions implements CLIOptions<HashOptions> {
    HELP(new OptionHandler<HashOptions>() {
      @Override
      public String getShortCode() {
        return "h";
      }

      @Nullable
      @Override
      public Option apply(@Nullable String s) {
        return new Option(getShortCode(), "help", false, "Generate Help screen");
      }
    }),
    INPUT(new OptionHandler<HashOptions>() {
      @Override
      public String getShortCode() {
        return "i";
      }

      @Override
      public Optional<Object> getValue(HashOptions option, CommandLine cli) {
        List<String> inputs = new ArrayList<>();
        for(String input : Splitter.on(",").split(Optional.ofNullable(option.get(cli)).orElse(""))) {
          inputs.add(input.trim());
        }
        return Optional.of(inputs);
      }

      @Nullable
      @Override
      public Option apply(@Nullable String s) {
        Option o = new Option(s, "input", true, "The HDFS data file(s) to load.  These can be comma separated.");
        o.setArgName("FILES");
        o.setRequired(true);
        return o;
      }
    })
    ,OUTPUT(new OptionHandler<HashOptions>() {
      @Override
      public String getShortCode() {
        return "o";
      }

      @Override
      public Optional<Object> getValue(HashOptions option, CommandLine cli) {
        if(option.has(cli)) {
          return Optional.ofNullable(option.get(cli));
        }
        return Optional.empty();
      }

      @Nullable
      @Override
      public Option apply(@Nullable String s) {
        Option o = new Option(s, "output", true, "The location to write out the serialized model.  This is a local file.");
        o.setArgName("FILE");
        o.setRequired(true);
        return o;
      }
    }),
    PARTITIONS(new OptionHandler<HashOptions>() {
      @Override
      public String getShortCode() {
        return "p";
      }

      @Override
      public Optional<Object> getValue(HashOptions option, CommandLine cli) {
        if(!option.has(cli)) {
          return Optional.empty();
        }
        Integer partitions = ConversionUtils.convert(option.get(cli), Integer.class);
        return Optional.ofNullable(partitions);
      }

      @Nullable
      @Override
      public Option apply(@Nullable String s) {
        Option o = new Option(s, "partitions", true, "The total number of partitions to use when processing the input data.");
        o.setArgName("PARTITION_NO");
        o.setRequired(false);
        return o;
      }
    }),
    CONFIG(new OptionHandler<HashOptions>() {
      @Override
      public String getShortCode() {
        return "c";
      }

      @Override
      public Optional<Object> getValue(HashOptions option, CommandLine cli) {
        String configLoc = option.get(cli);
        File configFile = new File(configLoc);
        if(configFile.exists()) {
          try {
            return Optional.of(JSONUtils.INSTANCE.load(configFile, Config.class));
          } catch (IOException e) {
            throw new IllegalStateException("Unable to load " + configLoc + ": " + e.getMessage(), e);
          }
        }
        else {
          throw new IllegalStateException("Unable to load " + configLoc + " because the file doesn't exist.");
        }
      }

      @Nullable
      @Override
      public Option apply(@Nullable String s) {
        Option o = new Option(s, "config", true, "Config to use.");
        o.setArgName("JSON_FILE");
        o.setRequired(true);
        return o;
      }
    })
    ;

    Option option;
    String shortCode;
    OptionHandler<HashOptions> handler;

    HashOptions(OptionHandler<HashOptions> optionHandler) {
      this.shortCode = optionHandler.getShortCode();
      this.handler = optionHandler;
      this.option = optionHandler.apply(shortCode);
    }

    @Override
    public Option getOption() {
      return option;
    }

    @Override
    public boolean has(CommandLine cli) {
      return cli.hasOption(shortCode);
    }

    @Override
    public String get(CommandLine cli) {
      return cli.getOptionValue(shortCode);
    }

    @Override
    public OptionHandler<HashOptions> getHandler() {
      return handler;
    }
    public static CommandLine parse(CommandLineParser parser, String[] args) {
      return OptionHandler.parse(NAME, parser, args, values(), HELP);
    }

    public static EnumMap<HashOptions, Optional<Object> > createConfig(CommandLine cli) {
      return OptionHandler.createConfig(cli, values(), HashOptions.class);
    }
  }

  private static JavaSparkContext createContext(String name) {
    SparkConf conf = new SparkConf().setAppName(name).registerKryoClasses(new Class<?>[] { Context.class});
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    return new JavaSparkContext(conf);
  }

  public static SemanticHasher create(JavaSparkContext sc, JavaRDD<byte[]> inputData, Config config) {
    JavaRDD<Map<String, Object>> transformedInputData = TransformUtil.INSTANCE.transform(config, inputData);
    transformedInputData.cache();
    Context context = ContextUtil.INSTANCE.generateContext(transformedInputData, config);
    Word2VecTrainer trainer = new Word2VecTrainer();
    VectorizerModel vecModel = trainer.train(sc, config, context, transformedInputData);

    LSHBinner binningModel = LSHBinner.create(vecModel, config.getBinningConfig());
    SemanticHasher ret = new SemanticHasher(vecModel, binningModel);
    return ret;
  }

  public static void main(String... argv) {
    CommandLine cli = HashOptions.parse(new PosixParser(), argv);
    EnumMap<HashOptions, Optional<Object>> params = HashOptions.createConfig(cli);

    JavaSparkContext sc = createContext(NAME);

    Config config = (Config) params.get(HashOptions.CONFIG).get();
    config.initialize();

    List<String> input = (List<String>)params.get(HashOptions.INPUT).get();
    Optional<Integer> partitions = Optional.ofNullable((Integer)params.get(HashOptions.PARTITIONS).get());
    JavaRDD<byte[]> trainingData = LoadUtil.INSTANCE.rawRead(sc, input, partitions);
    SemanticHasher binner = create(sc, trainingData, config);
    String output = (String) params.get(HashOptions.OUTPUT).get();
    try(OutputStream os = new FileOutputStream(new File(output))) {
      IOUtils.write(SerDeUtils.toBytes(binner), os);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to write to " + output + ", " + e.getMessage(), e);
    }
  }
}
