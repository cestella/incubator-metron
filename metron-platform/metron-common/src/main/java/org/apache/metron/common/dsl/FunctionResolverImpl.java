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
package org.apache.metron.common.dsl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FunctionResolverImpl implements FunctionResolver {
  protected static final Logger LOG = LoggerFactory.getLogger(FunctionResolverImpl.class);
  private final Map<String, StellarFunctionInfo> functions = new HashMap<>();
  private final AtomicBoolean isInitialized = new AtomicBoolean(false);
  private static final ReadWriteLock lock = new ReentrantReadWriteLock();
  private static FunctionResolverImpl INSTANCE = new FunctionResolverImpl();
  private void loadFunctions(final Map<String, StellarFunctionInfo> ret) {
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(effectiveClassPathUrls(classLoader)));
      for (Class<?> clazz : reflections.getSubTypesOf(StellarFunction.class)) {
        if (clazz.isAnnotationPresent(Stellar.class)) {
          Map.Entry<String, StellarFunctionInfo> instance = create((Class<? extends StellarFunction>) clazz);
          if (instance != null) {
            ret.put(instance.getKey(), instance.getValue());
          }
        }
      }
      //I know this looks weird, but the logger might not be initialized when we're doing this call, so it NPE's
      getLogger().info("Found " + ret.size() + " Stellar Functions...");
    }
    catch(Throwable ex) {
      //I know this looks weird, but the logger might not be initialized when we're doing this call, so it NPE's
      getLogger().error("Unable to initialize FunctionResolverImpl: " + ex.getMessage(), ex);
      throw ex;
    }
  }

  public static Collection<URL> effectiveClassPathUrls(ClassLoader... classLoaders) {
    return ClasspathHelper.forManifest(ClasspathHelper.forClassLoader(classLoaders));
  }

  public static Logger getLogger() {
    if(LOG != null) {
      return LOG;
    }
    else {
      return LoggerFactory.getLogger(FunctionResolverImpl.class);
    }
  }

  private static Map.Entry<String, StellarFunctionInfo> create(Class<? extends StellarFunction> stellarClazz) {
    String fqn = getNameFromAnnotation(stellarClazz);
    if(fqn == null) {
      getLogger().error("Unable to resolve fully qualified stellar name for " + stellarClazz.getName());
    }
    StellarFunction f = createFunction(stellarClazz);
    if(fqn != null && f != null) {
      Stellar stellarAnnotation = stellarClazz.getAnnotation(Stellar.class);
      StellarFunctionInfo info = new StellarFunctionInfo(stellarAnnotation.description()
                                                        , fqn
                                                        , stellarAnnotation.params()
                                                        , f
                                                        );
      return new AbstractMap.SimpleEntry<>(fqn, info);
    }
    else {
      getLogger().error("Unable to create instance for StellarFunction " + stellarClazz.getName() + " name: " + fqn);
    }
    return null;
  }

  private static String getNameFromAnnotation(Class<? extends StellarFunction> stellarClazz) {
    if(stellarClazz.isAnnotationPresent(Stellar.class)) {
      Stellar stellarAnnotation = stellarClazz.getAnnotation(Stellar.class);
      String namespace = stellarAnnotation.namespace();
      String name = stellarAnnotation.name();
      if(name == null || name.trim().length() == 0) {
        return null;
      }
      else {
        name = name.trim();
      }
      if(namespace == null || namespace.length() == 0) {
        namespace = null;
      }
      else {
        namespace = namespace.trim();
      }
      return Joiner.on("_").skipNulls().join(Arrays.asList(namespace, name));
    }
    return null;

  }

  private static StellarFunction createFunction(Class<? extends StellarFunction> stellarClazz) {
    try {
      return stellarClazz.newInstance();
    } catch (Exception e) {
      getLogger().error("Unable to load " + stellarClazz.getName() + " because " + e.getMessage(), e);
      return null;
    }
  }
  public static FunctionResolver getInstance() {
    return INSTANCE;
  }

  private Map<String, StellarFunctionInfo> _getFunctions() {
    lock.readLock().lock();
    try {
      if (isInitialized.get()) {
        return functions;
      }
    }
    finally {
      lock.readLock().unlock();
    }
    lock.writeLock().lock();
    try {
      if(!isInitialized.get()) {
        loadFunctions(functions);
        isInitialized.set(true);
      }
      return functions;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Iterable<StellarFunctionInfo> getFunctionInfo() {
    return _getFunctions().values();
  }

  @Override
  public Iterable<String> getFunctions() {
    return _getFunctions().keySet();
  }

  /**
   * Applies this function to the given argument.
   *
   * @param s the function argument
   * @return the function result
   */
  @Override
  public StellarFunction apply(String s) {
    StellarFunctionInfo ret = _getFunctions().get(s);
    if(ret == null) {
      throw new IllegalStateException("Unable to resolve function " + s);
    }
    return ret.getFunction();
  }
}
