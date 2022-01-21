/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.grpc.client;

import org.adamalang.common.Callback;
import org.adamalang.common.ErrorCodeException;
import org.adamalang.common.metrics.NoOpMetricsFactory;
import org.adamalang.grpc.TestBed;
import org.adamalang.grpc.client.contracts.BillingStream;
import org.adamalang.grpc.client.contracts.CreateCallback;
import org.adamalang.grpc.client.contracts.SimpleEvents;
import org.adamalang.grpc.client.sm.Connection;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientTests {
  @Test
  public void simple_happy_flows() throws Exception {
    try (TestBed bed =
             new TestBed(
                 12500,
                 "@can_create(who) { return true; } @connected(who) { return true; } public int x; @construct { x = 123; transition #p in 0.25; } #p { x++; } ")) {
      bed.startServer();
      Client client = new Client(bed.identity, new ClientMetrics(new NoOpMetricsFactory()), null);
      try {
        client.getTargetPublisher().accept(Collections.singletonList("127.0.0.1:12500"));
        CountDownLatch latchGetDeployTargets = new CountDownLatch(1);
        client.getDeploymentTargets(
            "space",
            new Consumer<String>() {
              @Override
              public void accept(String s) {
                Assert.assertEquals("127.0.0.1:12500", s);
                latchGetDeployTargets.countDown();
              }
            });
        Assert.assertTrue(latchGetDeployTargets.await(5000, TimeUnit.MILLISECONDS));
        client.notifyDeployment("127.0.0.1:12500", "space");
        CountDownLatch latchRandomBillingExchangeFinishes = new CountDownLatch(1);
        CountDownLatch latchFound = new CountDownLatch(1);
        for (int k = 0; k < 10; k++) {
          client.routing().get(
              "space",
              "key",
              new Consumer<String>() {
                @Override
                public void accept(String s) {
                  if (s != null) {
                    latchFound.countDown();
                  }
                }
              });
          latchFound.await(500, TimeUnit.MILLISECONDS);
        }
        Assert.assertTrue(latchFound.await(500, TimeUnit.MILLISECONDS));
        client.randomBillingExchange(new BillingStream() {
          @Override
          public void handle(String target, String batch, Runnable after) {

          }

          @Override
          public void failure(int code) {

          }

          @Override
          public void finished() {
            latchRandomBillingExchangeFinishes.countDown();
          }
        });
        Assert.assertTrue(latchGetDeployTargets.await(5000, TimeUnit.MILLISECONDS));
        CountDownLatch latchCreatedKey = new CountDownLatch(1);
        client.create("me", "dev", "space", "key1", null, "{}", new CreateCallback() {
          @Override
          public void created() {
            latchCreatedKey.countDown();
          }

          @Override
          public void error(int code) {
            System.err.println("CODE:" + code);
          }
        });
        Assert.assertTrue(latchCreatedKey.await(5000, TimeUnit.MILLISECONDS));
        CountDownLatch latchGotConnected = new CountDownLatch(1);
        CountDownLatch latchGotData = new CountDownLatch(1);
        CountDownLatch latchGotDisconnect = new CountDownLatch(1);
        Connection connection = client.connect("me", "dev", "space", "key1", new SimpleEvents() {
          @Override
          public void connected() {
            latchGotConnected.countDown();
          }

          @Override
          public void delta(String data) {
            latchGotData.countDown();
          }

          @Override
          public void error(int code) {

          }

          @Override
          public void disconnected() {
            latchGotDisconnect.countDown();
          }
        });
        Assert.assertTrue(latchGotConnected.await(5000, TimeUnit.MILLISECONDS));
        Assert.assertTrue(latchGotData.await(5000, TimeUnit.MILLISECONDS));
        CountDownLatch latchGotReflection = new CountDownLatch(1);
        client.reflect("space", "key", new Callback<String>() {
          @Override
          public void success(String value) {
            latchGotReflection.countDown();
          }

          @Override
          public void failure(ErrorCodeException ex) {

          }
        });
        Assert.assertTrue(latchGotReflection.await(5000, TimeUnit.MILLISECONDS));
        connection.close();
        Assert.assertTrue(latchGotDisconnect.await(5000, TimeUnit.MILLISECONDS));
      } finally{
        client.shutdown();
      }
    }
  }

  @Test
  public void no_capacity_for_create() throws Exception {
    try (TestBed bed =
             new TestBed(
                 12500,
                 "@can_create(who) { return true; } @connected(who) { return true; } public int x; @construct { x = 123; transition #p in 0.25; } #p { x++; } ")) {
      Client client = new Client(bed.identity, new ClientMetrics(new NoOpMetricsFactory()), null);
      try {
        CountDownLatch latchFailed = new CountDownLatch(1);
        client.create("me", "dev", "space", "key1", null, "{}", new CreateCallback() {
          @Override
          public void created() {
          }

          @Override
          public void error(int code) {
            Assert.assertEquals(912447, code);
            latchFailed.countDown();
          }
        });
        Assert.assertTrue(latchFailed.await(5000, TimeUnit.MILLISECONDS));
      } finally{
        client.shutdown();
      }
    }
  }

  @Test
  public void not_allowed_create() throws Exception {
    try (TestBed bed =
             new TestBed(
                 12501,
                 "@can_create(who) { return false; } @connected(who) { return true; } public int x; @construct { x = 123; transition #p in 0.25; } #p { x++; } ")) {
      bed.startServer();
      Client client = new Client(bed.identity, new ClientMetrics(new NoOpMetricsFactory()), null);
      try {
        client.getTargetPublisher().accept(Collections.singletonList("127.0.0.1:12501"));
        CountDownLatch latchFound = new CountDownLatch(1);
        for (int k = 0; k < 10; k++) {
          client.routing().get(
              "space",
              "key",
              new Consumer<String>() {
                @Override
                public void accept(String s) {
                  if (s != null) {
                    latchFound.countDown();
                  }
                }
              });
          latchFound.await(500, TimeUnit.MILLISECONDS);
        }
        Assert.assertTrue(latchFound.await(1000, TimeUnit.MILLISECONDS));
        CountDownLatch latchFailed = new CountDownLatch(1);
        client.create("me", "dev", "space", "key1", null, "{}", new CreateCallback() {
          @Override
          public void created() {
          }

          @Override
          public void error(int code) {
            Assert.assertEquals(134259, code);
            latchFailed.countDown();
          }
        });
        Assert.assertTrue(latchFailed.await(5000, TimeUnit.MILLISECONDS));
      } finally{
        client.shutdown();
      }
    }
  }
}