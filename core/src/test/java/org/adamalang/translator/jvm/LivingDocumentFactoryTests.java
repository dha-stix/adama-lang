/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.translator.jvm;

import org.adamalang.common.ErrorCodeException;
import org.adamalang.runtime.ContextSupport;
import org.adamalang.runtime.natives.NtClient;
import org.adamalang.runtime.remote.Deliverer;
import org.junit.Assert;
import org.junit.Test;

public class LivingDocumentFactoryTests {
  @Test
  public void almostOK() throws Exception {
    final var compiler =
        new LivingDocumentFactory(
            "Foo",
            "import java.util.HashMap; \nimport org.adamalang.runtime.contracts.DocumentMonitor;import org.adamalang.runtime.natives.*;import org.adamalang.runtime.sys.*;\n public class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(CoreRequestContext who) { return false; } public static boolean __onCanInvent(CoreRequestContext who) { return false; } public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } public static HashMap<String, Object> __config() { return new HashMap<>(); } public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); } } ",
            "{}", Deliverer.FAILURE);
    var success = false;
    try {
      compiler.create(null);
      success = true;
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(115747, nsme.code);
    }
    Assert.assertFalse(success);
  }

  @Test
  public void badCode() throws Exception {
    var failed = true;
    try {
      new LivingDocumentFactory(
          "Foo",
          "import org.adamalang.runtime.reactives.RxObject;\n class Foo { public Foo(}",
          "{}", Deliverer.FAILURE);
      failed = false;
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(180258, nsme.code);
    }
    Assert.assertTrue(failed);
  }

  @Test
  public void castFailure() throws Exception {
    final var compiler =
        new LivingDocumentFactory(
            "Foo",
            "import java.util.HashMap; \nimport org.adamalang.runtime.contracts.DocumentMonitor;import org.adamalang.runtime.natives.*; import org.adamalang.runtime.sys.*;\n public class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(CoreRequestContext who) { return false; }  public static boolean __onCanInvent(CoreRequestContext who) { return false; } public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } public static HashMap<String, Object> __config() { return new HashMap<>(); } public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); } }",
            "{}", Deliverer.FAILURE);
    var success = false;
    try {
      compiler.create(null);
      success = true;
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(115747, nsme.code);
    }
    Assert.assertFalse(success);
  }

  @Test
  public void noConstructor() throws Exception {
    try {
      new LivingDocumentFactory(
          "Foo",
          "import java.util.HashMap;" +
              "import org.adamalang.runtime.natives.*;" +
              "import org.adamalang.runtime.sys.*;" +
              "class Foo {" +
              " public static boolean __onCanCreate(CoreRequestContext who) { return false; }" +
              "public static boolean __onCanInvent(CoreRequestContext who) { return false; }" +
              "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { return false; } " +
              "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
              "public static HashMap<String, Object> __config() { return new HashMap<>(); }" +
              "}",
          "{}", Deliverer.FAILURE);
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }

  @Test
  public void invalidPolicies() throws Exception {
    LivingDocumentFactory factory = new LivingDocumentFactory(
        "Foo",
        "import org.adamalang.runtime.contracts.DocumentMonitor; import org.adamalang.runtime.sys.*;" +
            "import java.util.HashMap; import org.adamalang.runtime.natives.*; public class Foo {" +
            "public Foo(final DocumentMonitor __monitor) { }" +
            "public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanInvent(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
            "public static HashMap<String, Object> __config() { return new HashMap<>(); }" +
            "}",
        "{}", Deliverer.FAILURE);

    Assert.assertEquals(10000, factory.maximum_history);
    try {
      factory.canCreate(ContextSupport.WRAP(NtClient.NO_ONE));
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(180858, ex.code);
    }
    try {
      factory.canInvent(ContextSupport.WRAP(NtClient.NO_ONE));
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(146558, ex.code);
    }
    try {
      factory.canSendWhileDisconnected(ContextSupport.WRAP(NtClient.NO_ONE));
      Assert.fail();
    } catch (ErrorCodeException ex) {
      Assert.assertEquals(148095, ex.code);
    }
  }

  @Test
  public void configWorks() throws Exception {
    LivingDocumentFactory factory = new LivingDocumentFactory(
        "Foo",
        "import org.adamalang.runtime.contracts.DocumentMonitor; import org.adamalang.runtime.sys.*;" +
            "import java.util.HashMap; import org.adamalang.runtime.natives.*; public class Foo {" +
            "public Foo(final DocumentMonitor __monitor) { }" +
            "public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanInvent(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static HashMap<String, Object> __config() { HashMap<String, Object> map = new HashMap<>(); map.put(\"maximum_history\", 150); return map; }" +
            "public static HashMap<String, HashMap<String, Object>> __services() { return new HashMap<>(); }" +
            "}",
        "{}", Deliverer.FAILURE);
    Assert.assertEquals(150, factory.maximum_history);
  }

  @Test
  public void servicesWork() throws Exception {
    LivingDocumentFactory factory = new LivingDocumentFactory(
        "Foo",
        "import org.adamalang.runtime.contracts.DocumentMonitor; import org.adamalang.runtime.sys.*;" +
            "import java.util.HashMap; import org.adamalang.runtime.natives.*; public class Foo {" +
            "public Foo(final DocumentMonitor __monitor) { }" +
            "public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanInvent(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static boolean __onCanSendWhileDisconnected(CoreRequestContext who) { throw new NullPointerException(); }" +
            "public static HashMap<String, Object> __config() { HashMap<String, Object> map = new HashMap<>(); map.put(\"maximum_history\", 150); return map; }" +
            "public static HashMap<String, HashMap<String, Object>> __services() { HashMap<String, HashMap<String, Object>> map = new HashMap<>(); map.put(\"test\", new HashMap<>()); return map; }" +
            "}",
        "{}", Deliverer.FAILURE);
    Assert.assertTrue(factory.registry.contains("test"));
  }

  @Test
  public void missingPolicy1() throws Exception {
    try {
      new LivingDocumentFactory(
          "Foo",
          "import org.adamalang.runtime.contracts.DocumentMonitor; class Foo { public Foo(DocumentMonitor dm) {} }",
          "{}", Deliverer.FAILURE);
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }

  @Test
  public void missingPolicy2() throws Exception {
    try {
      new LivingDocumentFactory(
          "Foo",
          "import org.adamalang.runtime.natives.*; import org.adamalang.runtime.contracts.DocumentMonitor; class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(NtClient who) { throw new NullPointerException(); } }",
          "{}", Deliverer.FAILURE);
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }

  @Test
  public void missingPolicy3() throws Exception {
    try {
      new LivingDocumentFactory(
          "Foo",
          "import org.adamalang.runtime.natives.*; import org.adamalang.runtime.sys.*; import org.adamalang.runtime.contracts.DocumentMonitor; class Foo { public Foo(DocumentMonitor dm) {} public static boolean __onCanCreate(CoreRequestContext who) { throw new NullPointerException(); } public static boolean __onCanSendWhileDisconnected(NtClient who) { throw new NullPointerException(); } }",
          "{}", Deliverer.FAILURE);
      Assert.fail();
    } catch (final ErrorCodeException nsme) {
      Assert.assertEquals(198174, nsme.code);
    }
  }
}
