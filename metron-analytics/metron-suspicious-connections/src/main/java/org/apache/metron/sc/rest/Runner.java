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
package org.apache.metron.sc.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.metron.common.utils.JSONUtils;
import org.apache.metron.sc.ClusterModel;
import org.eclipse.jetty.server.AbstractNetworkConnector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Runner extends Application<SuspiciousConnectionConfig> {
  private Environment environment;
  @Path("/apply")
  @Produces(MediaType.APPLICATION_JSON)
  public static class Resource {
    private final ClusterModel model;
    private static final List<String> EMPTY_LIST = new ArrayList<>();
    public Resource(ClusterModel model) {
      this.model = model;
    }

    @GET
    @Timed
    public Map<String, Double> score( @QueryParam("message") Optional<Map<String, Object>> message
                                    , @QueryParam("wordFields") Optional<List<String>> wordFields
                                    )
    {

      Map<String, Double> ret = new HashMap<>();
      if(!message.isPresent()) {
        return ret;
      }

      String specialWord = model.computeSpecialWord(message.get());
      for(String word : wordFields.orElse(EMPTY_LIST)) {
        Object wordValue =  message.get().get(word);
        if(! (wordValue instanceof String)) {
          continue;
        }
        ret.put(word, model.score(specialWord, (String)wordValue));
      }
      return ret;
    }
  }

  @Override
  public void initialize(Bootstrap<SuspiciousConnectionConfig> bootstrap) {
    try(PrintWriter pw = new PrintWriter(new File("endpoint.dat"))) {
      pw.println(JSONUtils.INSTANCE.toJSON(ImmutableMap.of("url", "http://localhost:" + getPort())));

    } catch (IOException e) {
      throw new IllegalStateException("Unable to write out to endpoint.dat", e);
    }
  }

  public int getPort() {
    return ((AbstractNetworkConnector) environment.getApplicationContext().getServer().getConnectors()[0]).getLocalPort();
  }

  @Override
  public void run(SuspiciousConnectionConfig suspiciousConnectionConfig, Environment environment) throws Exception {
    this.environment = environment;
    ClusterModel model = ClusterModel.load(new File(suspiciousConnectionConfig.getModelLocation()));
    final Resource resource = new Resource(model);
    environment.jersey().register(resource);
  }

  public static void main(String... argv) throws Exception {
    new Runner().run(argv);
  }
}
