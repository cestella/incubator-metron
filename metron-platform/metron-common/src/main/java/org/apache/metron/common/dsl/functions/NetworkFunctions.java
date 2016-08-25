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

package org.apache.metron.common.dsl.functions;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.net.InternetDomainName;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.metron.common.dsl.BaseStellarFunction;
import org.apache.metron.common.dsl.Stellar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

public class NetworkFunctions {
  @Stellar(name="IN_SUBNET")
  public static class InSubnet extends BaseStellarFunction {

    @Override
    public Object apply(List<Object> list) {
      if(list.size() < 2) {
        throw new IllegalStateException("IN_SUBNET expects at least two args: [ip, cidr1, cidr2, ...]"
                + " where cidr is the subnet mask in cidr form"
        );
      }
      String ip = (String) list.get(0);
      if(ip == null) {
        return false;
      }
      boolean inSubnet = false;
      for(int i = 1;i < list.size() && !inSubnet;++i) {
        String cidr = (String) list.get(1);
        if(cidr == null) {
          continue;
        }
        inSubnet |= new SubnetUtils(cidr).getInfo().isInRange(ip);
      }

      return inSubnet;
    }
  }

  @Stellar(name="DOMAIN_REMOVE_SUBDOMAINS")
  public static class RemoveSubdomains extends BaseStellarFunction {

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
          String hostnameWithoutTLD = suffix.substring(0, suffix.length() - 1);
          String hostnameWithoutSubsAndTLD = Iterables.getLast(Splitter.on(".").split(hostnameWithoutTLD), null);
          if(hostnameWithoutSubsAndTLD == null) {
            return null;
          }
          return hostnameWithoutSubsAndTLD + "." + tld;
        }
        else {
          return null;
        }
      }
      return null;
    }
  }

  @Stellar(name="DOMAIN_REMOVE_TLD")
  public static class RemoveTLD extends BaseStellarFunction {
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

  @Stellar(name="DOMAIN_TO_TLD")
  public static class ExtractTLD extends BaseStellarFunction {
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

  @Stellar(name="URL_TO_PORT")
  public static class URLToPort extends BaseStellarFunction {
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

  @Stellar(name="URL_TO_PATH")
  public static class URLToPath extends BaseStellarFunction {
    @Override
    public Object apply(List<Object> objects) {
      URL url =  toUrl(objects.get(0));
      return url == null?null:url.getPath();
    }
  }

  @Stellar(name="URL_TO_HOST")
  public static class URLToHost extends BaseStellarFunction {

    @Override
    public Object apply(List<Object> objects) {
      URL url =  toUrl(objects.get(0));
      return url == null?null:url.getHost();
    }
  }

  @Stellar(name="URL_TO_PROTOCOL")
  public static class URLToProtocol extends BaseStellarFunction {

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
