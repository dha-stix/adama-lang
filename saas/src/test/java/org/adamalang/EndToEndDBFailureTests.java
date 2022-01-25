/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang;

import org.adamalang.common.Json;
import org.adamalang.transforms.results.Keystore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class EndToEndDBFailureTests {
  @Test
  public void flow() throws Exception {
    try (TestFrontEnd fe = new TestFrontEnd()) {
      Runnable latch1 = fe.latchOnEmail("x@x.com");
      Iterator<String> c1 =
          fe.execute("{\"id\":1,\"method\":\"init/start\",\"email\":\"x@x.com\"}");
      latch1.run();
      Iterator<String> c2 =
          fe.execute(
              "{\"id\":2,\"connection\":1,\"method\":\"init/generate-identity\",\"code\":\""
                  + fe.codesSentToEmail.get("x@x.com")
                  + "\"}");
      String result1 = c2.next();
      Assert.assertTrue(result1.length() > 0);
      Assert.assertEquals("FINISH:{\"identity\":", result1.substring(0, 19));
      String identity = Json.parseJsonObject(result1.substring(7)).get("identity").textValue();
      Assert.assertEquals("FINISH:{}", c1.next());
      fe.kill("authorities");
      fe.kill("spaces");
      Iterator<String> c3 = fe.execute("{\"id\":3,\"method\":\"authority/create\",\"identity\":\"" + identity + "\"}");
      Assert.assertEquals("ERROR:982016", c3.next());
      Keystore ks = Keystore.parse("{}");
      String privateKeyFile = ks.generate("xyz");
      Iterator<String> c4 = fe.execute("{\"id\":6,\"method\":\"authority/set\",\"identity\":\"" + identity + "\",\"authority\":\"nope\",\"key-store\":"+ks.persist()+"}");
      Assert.assertEquals("ERROR:900098", c4.next());
      Iterator<String> c5 = fe.execute("{\"id\":6,\"method\":\"authority/get\",\"identity\":\"" + identity + "\",\"authority\":\"x\"}");
      Assert.assertEquals("ERROR:928819", c5.next());
      Iterator<String> c6 =
          fe.execute("{\"id\":4,\"method\":\"authority/list\",\"identity\":\"" + identity + "\"}");
      Assert.assertEquals("ERROR:998430", c6.next());
      Iterator<String> c7 = fe.execute("{\"id\":1,\"identity\":\""+ identity + "\",\"method\":\"space/create\",\"space\":\"spacename\"}");
      Assert.assertEquals("ERROR:900104", c7.next());
      Iterator<String> c8 = fe.execute("{\"id\":6,\"method\":\"authority/destroy\",\"identity\":\"" + identity + "\",\"authority\":\"x\"}");
      Assert.assertEquals("ERROR:913436", c8.next());
    }
  }
}