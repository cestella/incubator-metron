package org.apache.metron.common.stellar.maas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.metron.common.utils.JSONUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

@Path("/")
public class MockDGAModel {
  private static HttpServer server;
  private Map<String, Boolean> isMalicious = ImmutableMap.of("yahoo.com", false
                                                            ,"badguy.com", true
                                                           );

  @GET
  @Path("/apply")
  @Produces("application/json")
  public Response apply(@QueryParam("host") String host ) throws JsonProcessingException {
    Boolean b = isMalicious.get(host);
    boolean isMalicious = b != null && b;
    Map<String, Boolean> ret = new HashMap<String, Boolean>();
    ret.put("is_malicious", isMalicious );
    String resp = JSONUtils.INSTANCE.toJSON(ret, true);
    return Response.ok(resp, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @ApplicationPath("rs")
  public static class ApplicationConfig extends Application {
    private final Set<Class<?>> classes;
    public ApplicationConfig() {
      HashSet<Class<?>> c = new HashSet<>();
      c.add(MockDGAModel.class);
      classes = Collections.unmodifiableSet(c);
    }
    @Override
    public Set<Class<?>> getClasses() {
      return classes;
    }
  }

  public static void start(int port) throws IOException {
    // Create an HTTP server listening at port
    URI uri = UriBuilder.fromUri("http://localhost/").port(port).build();
    server = HttpServer.create(new InetSocketAddress(uri.getPort()), 0);
    HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(new ApplicationConfig(), HttpHandler.class);
    server.createContext(uri.getPath(), handler);
    server.start();
  }

  public static void shutdown() {
    if(server != null) {
      server.stop(0);
    }
  }
}
