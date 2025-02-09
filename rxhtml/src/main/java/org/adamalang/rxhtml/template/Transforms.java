/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.rxhtml.template;

import java.util.Locale;

public class Transforms {
  public static String of(String transform) {
    switch (transform.trim().toLowerCase(Locale.ROOT)) {
      case "ntclient.agent":
        return "function(x) { return x.agent; }";
      case "ntclient.authority":
        return "function(x) { return x.authority; }";
      case "trim":
        return "function(x) { return ('' + x).trim(); }";
      case "upper":
        return "function(x) { return ('' + x).toUpperCase(); }";
      case "lower":
        return "function(x) { return ('' + x).toLowerCase(); }";
      default:
        return "function(x) { return x; }";
    }
  }
}
