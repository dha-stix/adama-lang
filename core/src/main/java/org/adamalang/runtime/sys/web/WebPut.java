/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.sys.web;

import org.adamalang.runtime.json.JsonStreamReader;
import org.adamalang.runtime.natives.NtClient;
import org.adamalang.runtime.natives.NtDynamic;
import org.adamalang.runtime.natives.NtMap;

public class WebPut {
  public final NtClient who;
  public final String uri;
  public final WebRouter router;
  public final NtMap<String, String> headers;
  public final NtDynamic parameters;
  public String bodyJson;

  public WebPut(NtClient who, WebPutRaw put) {
    this.who = who;
    this.uri = put.uri;
    this.router = new WebRouter(put.uri);
    this.headers = new NtMap<>();
    this.headers.storage.putAll(put.headers);
    this.parameters = put.parameters;
    this.bodyJson = put.bodyJson;
  }

  public JsonStreamReader body() {
    return new JsonStreamReader(bodyJson);
  }
}
