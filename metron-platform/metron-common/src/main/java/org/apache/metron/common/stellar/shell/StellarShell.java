/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.metron.common.stellar.shell;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;
import org.apache.metron.common.dsl.Context;
import org.apache.metron.common.dsl.StellarFunctionInfo;
import org.apache.metron.common.utils.JSONUtils;
import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.AeshConsoleCallback;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleOperation;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.CharacterType;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalCharacter;
import org.jboss.aesh.terminal.TerminalColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A REPL environment for Stellar.
 *
 * Useful for debugging Stellar expressions.
 */
public class StellarShell extends AeshConsoleCallback implements Completion {

  private static final String WELCOME = "Stellar, Go!";
  private List<TerminalCharacter> EXPRESSION_PROMPT = new ArrayList<TerminalCharacter>()
  {{
    add(new TerminalCharacter('[', new TerminalColor(Color.RED, Color.DEFAULT)));
    add(new TerminalCharacter('S', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter('t', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter('e', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter('l', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter('l', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter('a', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter('r', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.BOLD));
    add(new TerminalCharacter(']', new TerminalColor(Color.RED, Color.DEFAULT)));
    add(new TerminalCharacter('$', new TerminalColor(Color.GREEN, Color.DEFAULT), CharacterType.UNDERLINE));
    add(new TerminalCharacter(' ', new TerminalColor(Color.DEFAULT, Color.DEFAULT)));
  }};

  private static final String ERROR_PROMPT = "[!] ";
  private static final String MAGIC_PREFIX = "%";
  public static final String MAGIC_FUNCTIONS = MAGIC_PREFIX + "functions";
  public static final String MAGIC_VARS = MAGIC_PREFIX + "vars";
  private static final String DOC_PREFIX = "?";

  private StellarExecutor executor;

  private Console console;

  /**
   * Execute the Stellar REPL.
   */
  public static void main(String[] args) throws Exception {
    StellarShell shell = new StellarShell(args);
    shell.execute();
  }

  /**
   * Create a Stellar REPL.
   * @param args The commmand-line arguments.
   */
  public StellarShell(String[] args) throws Exception {

    // define valid command-line options
    Options options = new Options();
    options.addOption("z", "zookeeper", true, "Zookeeper URL");
    options.addOption("v", "variables", true, "File containing a JSON Map of variables");
    options.addOption("irc", "inputrc", true, "File containing the inputrc if not the default ~/.inputrc");
    options.addOption("na", "no_ansi", false, "Make the input prompt not use ANSI colors.");
    options.addOption("h", "help", false, "Print help");

    CommandLineParser parser = new PosixParser();
    CommandLine commandLine = parser.parse(options, args);

    // print help
    if(commandLine.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("stellar", options);
      System.exit(0);
    }

    // create the executor
    if(commandLine.hasOption("z")) {
      String zookeeperUrl = commandLine.getOptionValue("z");
      executor = new StellarExecutor(zookeeperUrl);

    } else {
      executor = new StellarExecutor();
    }

    if(commandLine.hasOption("v")) {
      Map<String, Object> variables = JSONUtils.INSTANCE.load(new File(commandLine.getOptionValue("v")), new TypeReference<Map<String, Object>>() {
      });
      for(Map.Entry<String, Object> kv : variables.entrySet()) {
        executor.assign(kv.getKey(), kv.getValue());
      }
    }
    SettingsBuilder settings = new SettingsBuilder().enableAlias(true)
                                                    .enableMan(true)
                                                    .parseOperators(false)
                                                    ;
    if(commandLine.hasOption("irc")) {
      settings = settings.inputrc(new File(commandLine.getOptionValue("irc")));
    }

    console = new Console(settings.create());
    if(!commandLine.hasOption("na")) {
      console.setPrompt(new Prompt(EXPRESSION_PROMPT));
    }
    else {
      console.setPrompt(new Prompt("[Stellar]$"));
    }
    console.addCompletion(this);
    console.setConsoleCallback(this);
  }

  /**
   * Handles the main loop for the REPL.
   */
  public void execute() {

    // welcome message and print globals
    writeLine(WELCOME);
    executor.getContext()
            .getCapability(Context.Capabilities.GLOBAL_CONFIG)
            .ifPresent(conf -> writeLine(conf.toString()));

    console.start();
  }

  /**
   * Handles user interaction when executing a Stellar expression.
   * @param expression The expression to execute.
   */
  private void handleStellar(String expression) {

    Iterable<String> assignmentSplit = Splitter.on(":=").split(expression);
    String stellarExpression = expression;
    String variable = null;
    if(Iterables.size(assignmentSplit) == 2) {
      //assignment
      variable = Iterables.getFirst(assignmentSplit, null);
      if(variable != null) {
        variable = variable.trim();
      }
      stellarExpression = Iterables.getLast(assignmentSplit, null);
    }
    if(!stellarExpression.isEmpty()) {
      stellarExpression = stellarExpression.trim();
    }
    Object result = executeStellar(stellarExpression);
    if(result != null) {
      writeLine(result.toString());
    }
    if(variable != null) {
      executor.assign(variable, result);
    }
  }

  /**
   * Handles user interaction when executing a Magic command.
   * @param rawExpression The expression to execute.
   */
  private void handleMagic( String rawExpression) {
    String expression = rawExpression.trim();
    if(MAGIC_FUNCTIONS.equals(expression)) {

      // list all functions
      String functions = StreamSupport
              .stream(executor.getFunctionResolver().getFunctionInfo().spliterator(), false)
              .map(info -> String.format("%s", info.getName()))
              .sorted()
              .collect(Collectors.joining(", "));
      writeLine(functions);

    } else if(MAGIC_VARS.equals(expression)) {

      // list all variables
      executor.getVariables()
              .forEach((k,v) -> writeLine(String.format("%s = %s", k, v)));

    } else {
      writeLine(ERROR_PROMPT + "undefined magic command: " + expression);
    }
  }

  /**
   * Handles user interaction when executing a doc command.
   * @param expression The expression to execute.
   */
  private void handleDoc(String expression) {

    String functionName = StringUtils.substring(expression, 1);
    StreamSupport
            .stream(executor.getFunctionResolver().getFunctionInfo().spliterator(), false)
            .filter(info -> StringUtils.equals(functionName, info.getName()))
            .map(info -> format(info))
            .forEach(doc -> write(doc));
  }

  /**
   * Formats the Stellar function info object into a readable string.
   * @param info The stellar function info object.
   * @return A readable string.
   */
  private String format(StellarFunctionInfo info) {
    return String.format(
            "%s\n desc: %-60s\n args: %-60s\n  ret: %-60s\n",
            info.getName(),
            info.getDescription(),
            StringUtils.join(info.getParams(), ", "),
            info.getReturns());
  }

  /**
   * Is a given expression a built-in magic?
   * @param expression The expression.
   */
  private boolean isMagic(String expression) {
    return StringUtils.startsWith(expression, MAGIC_PREFIX);
  }

  /**
   * Is a given expression asking for function documentation?
   * @param expression The expression.
   */
  private boolean isDoc(String expression) {
    return StringUtils.startsWith(expression, DOC_PREFIX);
  }

  /**
   * Executes a Stellar expression.
   * @param expression The expression to execute.
   * @return The result of the expression.
   */
  private Object executeStellar(String expression) {
    Object result = null;

    try {
      result = executor.execute(expression);

    } catch(Throwable t) {
      writeLine(ERROR_PROMPT + t.getMessage());
      t.printStackTrace();
    }

    return result;
  }

  private void write(String out) {
    System.out.print(out);
  }

  private void writeLine(String out) {
    console.getShell().out().println(out);
  }

  @Override
  public int execute(ConsoleOperation output) throws InterruptedException {
    String expression = output.getBuffer();
      if(StringUtils.isNotBlank(expression)) {
        if(isMagic(expression)) {
          handleMagic( expression);

        } else if(isDoc(expression)) {
          handleDoc(expression);

        } else if (expression.equals("quit")) {
          try {
            console.stop();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
        else {
          handleStellar(expression);
        }
      }

    return 0;
  }

  @Override
  public void complete(CompleteOperation completeOperation) {
    if(!completeOperation.getBuffer().isEmpty()) {
      String lastToken = Iterables.getLast(Splitter.on(" ").split(completeOperation.getBuffer()), null);
      if(lastToken != null && !lastToken.isEmpty()) {
        Iterable<String> candidates = executor.autoComplete(lastToken.trim());
        if(candidates != null && !Iterables.isEmpty(candidates)) {
          completeOperation.setCompletionCandidates(Lists.newArrayList(candidates));
        }
      }
    }

  }
}
