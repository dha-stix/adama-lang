/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/** a service registry maps service names to services */
public class ServiceRegistry {
  public static TreeMap<String, Function<HashMap<String, Object>, Service>> REGISTRY = new TreeMap<>();
  public static ServiceRegistry NOT_READY = new ServiceRegistry() {
    @Override
    public Service find(String name) {
      return Service.NOT_READY;
    }

    @Override
    public void resolve(HashMap<String, HashMap<String, Object>> servicesConfig) {
    }
  };
  private final TreeMap<String, Service> services;

  public ServiceRegistry() {
    this.services = new TreeMap<>();
  }

  /** find a service */
  public Service find(String name) {
    Service local = services.get(name);
    if (local == null) {
      return Service.FAILURE;
    }
    return local;
  }

  public boolean contains(String name) {
    return services.containsKey(name);
  }

  public void resolve(HashMap<String, HashMap<String, Object>> servicesConfig) {
    for (Map.Entry<String, HashMap<String, Object>> entry : servicesConfig.entrySet()) {
      Service resolved = resolveService(entry.getValue());
      if (resolved == null) {
        resolved = Service.FAILURE;
      }
      services.put(entry.getKey(), resolved);
    }
  }

  private Service resolveService(HashMap<String, Object> config) {
    Object std = config.get("std");
    if (std != null && std instanceof String) {
      Function<HashMap<String, Object>, Service> cons = REGISTRY.get((String) std);
      if (cons != null) {
        return cons.apply(config);
      }
    }
    return null;
  }
}
