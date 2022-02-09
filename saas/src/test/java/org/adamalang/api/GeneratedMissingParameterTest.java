/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.adamalang.TestFrontEnd;
import org.adamalang.common.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class GeneratedMissingParameterTest {
  @Test
  public void missing() throws Exception {
    try (TestFrontEnd fe = new TestFrontEnd()) {
      ObjectNode node;
      String _identity = fe.setupDevIdentity();
      //InitSetupAccount
      node = Json.newJsonObject();
      node.put("id", 1);
      node.put("method", "init/setup-account");
      Iterator<String> c2 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:473103", c2.next());
      node.put("email", "x@x.com");
      //InitCompleteAccount
      node = Json.newJsonObject();
      node.put("id", 2);
      node.put("method", "init/complete-account");
      Iterator<String> c3 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:473103", c3.next());
      node.put("email", "x@x.com");
      Iterator<String> c4 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:455681", c4.next());
      node.put("code", "x");
      //Probe
      node = Json.newJsonObject();
      node.put("id", 4);
      node.put("method", "probe");
      Iterator<String> c5 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c5.next());
      node.put("identity", _identity);
      //AuthorityCreate
      node = Json.newJsonObject();
      node.put("id", 5);
      node.put("method", "authority/create");
      Iterator<String> c6 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c6.next());
      node.put("identity", _identity);
      //AuthoritySet
      node = Json.newJsonObject();
      node.put("id", 6);
      node.put("method", "authority/set");
      Iterator<String> c7 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c7.next());
      node.put("identity", _identity);
      Iterator<String> c8 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:430095", c8.next());
      node.put("authority", "x");
      Iterator<String> c9 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:457743", c9.next());
      node.put("key-store", Json.newJsonObject());
      //AuthorityGet
      node = Json.newJsonObject();
      node.put("id", 9);
      node.put("method", "authority/get");
      Iterator<String> c10 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c10.next());
      node.put("identity", _identity);
      Iterator<String> c11 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:430095", c11.next());
      node.put("authority", "x");
      //AuthorityList
      node = Json.newJsonObject();
      node.put("id", 11);
      node.put("method", "authority/list");
      Iterator<String> c12 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c12.next());
      node.put("identity", _identity);
      //AuthorityDestroy
      node = Json.newJsonObject();
      node.put("id", 12);
      node.put("method", "authority/destroy");
      Iterator<String> c13 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c13.next());
      node.put("identity", _identity);
      Iterator<String> c14 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:430095", c14.next());
      node.put("authority", "x");
      //SpaceCreate
      node = Json.newJsonObject();
      node.put("id", 14);
      node.put("method", "space/create");
      Iterator<String> c15 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c15.next());
      node.put("identity", _identity);
      Iterator<String> c16 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c16.next());
      node.put("space", "x");
      //SpaceUsage
      node = Json.newJsonObject();
      node.put("id", 16);
      node.put("method", "space/usage");
      Iterator<String> c17 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c17.next());
      node.put("identity", _identity);
      Iterator<String> c18 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c18.next());
      node.put("space", "x");
      //SpaceGet
      node = Json.newJsonObject();
      node.put("id", 18);
      node.put("method", "space/get");
      Iterator<String> c19 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c19.next());
      node.put("identity", _identity);
      Iterator<String> c20 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c20.next());
      node.put("space", "x");
      //SpaceSet
      node = Json.newJsonObject();
      node.put("id", 20);
      node.put("method", "space/set");
      Iterator<String> c21 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c21.next());
      node.put("identity", _identity);
      Iterator<String> c22 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c22.next());
      node.put("space", "x");
      Iterator<String> c23 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:425999", c23.next());
      node.put("plan", Json.newJsonObject());
      //SpaceDelete
      node = Json.newJsonObject();
      node.put("id", 23);
      node.put("method", "space/delete");
      Iterator<String> c24 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c24.next());
      node.put("identity", _identity);
      Iterator<String> c25 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c25.next());
      node.put("space", "x");
      //SpaceSetRole
      node = Json.newJsonObject();
      node.put("id", 25);
      node.put("method", "space/set-role");
      Iterator<String> c26 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c26.next());
      node.put("identity", _identity);
      Iterator<String> c27 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c27.next());
      node.put("space", "x");
      Iterator<String> c28 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:473103", c28.next());
      node.put("email", "x@x.com");
      Iterator<String> c29 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:456716", c29.next());
      node.put("role", "x");
      //SpaceReflect
      node = Json.newJsonObject();
      node.put("id", 29);
      node.put("method", "space/reflect");
      Iterator<String> c30 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c30.next());
      node.put("identity", _identity);
      Iterator<String> c31 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c31.next());
      node.put("space", "x");
      Iterator<String> c32 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:466947", c32.next());
      node.put("key", "x");
      //SpaceList
      node = Json.newJsonObject();
      node.put("id", 32);
      node.put("method", "space/list");
      Iterator<String> c33 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c33.next());
      node.put("identity", _identity);
      //DocumentCreate
      node = Json.newJsonObject();
      node.put("id", 33);
      node.put("method", "document/create");
      Iterator<String> c34 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c34.next());
      node.put("identity", _identity);
      Iterator<String> c35 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c35.next());
      node.put("space", "x");
      Iterator<String> c36 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:466947", c36.next());
      node.put("key", "x");
      Iterator<String> c37 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461826", c37.next());
      node.put("arg", Json.newJsonObject());
      //DocumentList
      node = Json.newJsonObject();
      node.put("id", 37);
      node.put("method", "document/list");
      Iterator<String> c38 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c38.next());
      node.put("identity", _identity);
      Iterator<String> c39 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c39.next());
      node.put("space", "x");
      //ConnectionCreate
      node = Json.newJsonObject();
      node.put("id", 39);
      node.put("method", "connection/create");
      Iterator<String> c40 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c40.next());
      node.put("identity", _identity);
      Iterator<String> c41 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c41.next());
      node.put("space", "x");
      Iterator<String> c42 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:466947", c42.next());
      node.put("key", "x");
      //ConnectionSend
      node = Json.newJsonObject();
      node.put("id", 42);
      node.put("method", "connection/send");
      Iterator<String> c43 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:405505", c43.next());
      node.put("connection", 100L);
      Iterator<String> c44 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:454659", c44.next());
      node.put("channel", "x");
      Iterator<String> c45 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:425987", c45.next());
      node.put("message", Json.newJsonObject());
      //ConnectionUpdate
      node = Json.newJsonObject();
      node.put("id", 45);
      node.put("method", "connection/update");
      Iterator<String> c46 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:405505", c46.next());
      node.put("connection", 100L);
      //ConnectionEnd
      node = Json.newJsonObject();
      node.put("id", 46);
      node.put("method", "connection/end");
      Iterator<String> c47 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:405505", c47.next());
      node.put("connection", 100L);
      //AttachmentStart
      node = Json.newJsonObject();
      node.put("id", 47);
      node.put("method", "attachment/start");
      Iterator<String> c48 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:458759", c48.next());
      node.put("identity", _identity);
      Iterator<String> c49 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:461828", c49.next());
      node.put("space", "x");
      Iterator<String> c50 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:466947", c50.next());
      node.put("key", "x");
      Iterator<String> c51 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:470028", c51.next());
      node.put("filename", "x");
      Iterator<String> c52 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:455691", c52.next());
      node.put("content-type", "x");
      //AttachmentAppend
      node = Json.newJsonObject();
      node.put("id", 52);
      node.put("method", "attachment/append");
      Iterator<String> c53 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:409609", c53.next());
      node.put("upload", 100L);
      Iterator<String> c54 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:462859", c54.next());
      node.put("chunk-md5", "x");
      Iterator<String> c55 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:409608", c55.next());
      node.put("base64-bytes", "x");
      //AttachmentFinish
      node = Json.newJsonObject();
      node.put("id", 55);
      node.put("method", "attachment/finish");
      Iterator<String> c56 = fe.execute(node.toString());
      Assert.assertEquals("ERROR:409609", c56.next());
      node.put("upload", 100L);
    }
  }
}