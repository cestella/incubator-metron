package org.apache.metron.common.dsl.functions;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.net.InternetDomainName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

public class NetworkFunctions {
  public static class RemoveTLD implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> objects) {
      Object dnObj = objects.get(0);
      InternetDomainName idn = toDomainName(dnObj);
      if(idn != null) {
        String dn = dnObj.toString();
        String tld = idn.publicSuffix().toString();
        String suffix = Iterables.getFirst(Splitter.on(tld).split(dn), null);
        if(suffix != null)
        {
          return suffix.substring(0, suffix.length() - 1);
        }
        else {
          return null;
        }
      }
      return null;
    }
  }

  public static class ExtractTLD implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> objects) {
      Object dnObj = objects.get(0);
      InternetDomainName idn = toDomainName(dnObj);
      if(idn != null) {
        return idn.publicSuffix().toString();
      }
      return null;
    }
  }

  public static class URLToPort implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> objects) {
      URL url =  toUrl(objects.get(0));
      if(url == null) {
        return null;
      }
      int port = url.getPort();
      return port >= 0?port:url.getDefaultPort();
    }
  }

  public static class URLToPath implements Function<List<Object>, Object> {
    @Override
    public Object apply(List<Object> objects) {
      URL url =  toUrl(objects.get(0));
      return url == null?null:url.getPath();
    }
  }
  public static class URLToHost implements Function<List<Object>, Object> {

    @Override
    public Object apply(List<Object> objects) {
      URL url =  toUrl(objects.get(0));
      return url == null?null:url.getHost();
    }
  }

  public static class URLToProtocol implements Function<List<Object>, Object> {

    @Override
    public Object apply(List<Object> objects) {
      URL url =  toUrl(objects.get(0));
      return url == null?null:url.getProtocol();
    }
  }
  private static InternetDomainName toDomainName(Object dnObj) {
    if(dnObj != null) {
      String dn = dnObj.toString();
      return InternetDomainName.from(dn);
    }
    return null;
  }

  private static URL toUrl(Object urlObj) {
    if(urlObj == null) {
      return null;
    }
    try {
      return new URL(urlObj.toString());
    } catch (MalformedURLException e) {
      return null;
    }
  }
}
