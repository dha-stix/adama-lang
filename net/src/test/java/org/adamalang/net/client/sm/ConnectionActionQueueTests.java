/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.net.client.sm;

import org.adamalang.common.*;
import org.adamalang.common.metrics.NoOpMetricsFactory;
import org.adamalang.net.TestBed;
import org.adamalang.net.client.ClientConfig;
import org.adamalang.net.client.ClientMetrics;
import org.adamalang.net.client.InstanceClientFinder;
import org.adamalang.net.client.TestClientConfig;
import org.adamalang.net.client.routing.MockSpaceTrackingEvents;
import org.adamalang.net.client.routing.reactive.ReativeRoutingEngine;
import org.adamalang.net.mocks.LatchedSeqCallback;
import org.adamalang.net.mocks.MockSimpleEvents;
import org.junit.Test;

import java.util.ArrayList;

public class ConnectionActionQueueTests {
  @Test
  public void queueFullCanAttach() throws Exception {
    ClientMetrics metrics = new ClientMetrics(new NoOpMetricsFactory());
    TestBed[] servers = new TestBed[2];
    SimpleExecutor executor = SimpleExecutor.create("executor");
    ExceptionLogger logger = (t, c) -> {};
    try {
      ClientConfig clientConfig = new TestClientConfig();
      ComplexHelper.spinUpCapacity(servers, true, ComplexHelper.SIMPLE);
      ReativeRoutingEngine engine = new ReativeRoutingEngine(metrics, executor, new MockSpaceTrackingEvents(), 5, 5);
      InstanceClientFinder finder = new InstanceClientFinder(servers[0].base, clientConfig, metrics, null, SimpleExecutorFactory.DEFAULT, 2, engine, logger);
      try {
        ConnectionBase base = new ConnectionBase(clientConfig, metrics, engine, finder, executor);
        MockSimpleEvents events = new MockSimpleEvents();
        Runnable gotConnected = events.latchAt(1);
        Runnable gotData = events.latchAt(2);
        Connection connection = new Connection(base, "127.0.0.1", "origin", "who", "dev", "space", "key", "{}", null, events);
        ArrayList<LatchedSeqCallback> callbacks = new ArrayList<>();
        for (int k = 0; k < 100; k++) {
          LatchedSeqCallback callback = new LatchedSeqCallback();
          callbacks.add(callback);
          connection.canAttach(new Callback<Boolean>() {
            @Override
            public void success(Boolean value) {
              callback.success(100);
            }

            @Override
            public void failure(ErrorCodeException ex) {
              callback.failure(ex);
            }
          });
        }
        for (int k = clientConfig.getConnectionQueueSize(); k < callbacks.size(); k++) {
          callbacks.get(k).assertFail(901163);
        }
        finder.sync(Helper.setOf("127.0.0.1:" + servers[0].port));
        connection.open();
        gotConnected.run();
        gotData.run();
        for (int k = 0; k < clientConfig.getConnectionQueueSize(); k++) {
          callbacks.get(k).assertJustSuccess();
        }
      } finally {
        finder.shutdown();
      }
    } finally {
      ComplexHelper.stopCapacity(servers);
    }
  }

  @Test
  public void queueFullAttach() throws Exception {
    ClientMetrics metrics = new ClientMetrics(new NoOpMetricsFactory());
    TestBed[] servers = new TestBed[2];
    SimpleExecutor executor = SimpleExecutor.create("executor");
    ExceptionLogger logger = (t, c) -> {};
    try {
      ClientConfig clientConfig = new TestClientConfig();
      ComplexHelper.spinUpCapacity(servers, true, ComplexHelper.SIMPLE);
      ReativeRoutingEngine engine = new ReativeRoutingEngine(metrics, executor, new MockSpaceTrackingEvents(), 5, 5);
      InstanceClientFinder finder = new InstanceClientFinder(servers[0].base, clientConfig, metrics, null, SimpleExecutorFactory.DEFAULT, 2, engine, logger);
      try {
        ConnectionBase base = new ConnectionBase(clientConfig, metrics, engine, finder, executor);
        MockSimpleEvents events = new MockSimpleEvents();
        Runnable gotConnected = events.latchAt(1);
        Runnable gotData = events.latchAt(2);
        Connection connection = new Connection(base, "127.0.0.1", "origin", "who", "dev", "space", "key", "{}", null, events);
        ArrayList<LatchedSeqCallback> callbacks = new ArrayList<>();
        for (int k = 0; k < 100; k++) {
          LatchedSeqCallback callback = new LatchedSeqCallback();
          callbacks.add(callback);
          connection.attach("id", "name", "content-type", 42, "md5", "sha256", callback);
        }
        for (int k = clientConfig.getConnectionQueueSize(); k < callbacks.size(); k++) {
          callbacks.get(k).assertFail(913447);
        }
        finder.sync(Helper.setOf("127.0.0.1:" + servers[0].port));
        connection.open();
        gotConnected.run();
        gotData.run();
        for (int k = 0; k < clientConfig.getConnectionQueueSize(); k++) {
          callbacks.get(k).assertJustSuccess();
        }
      } finally {
        finder.shutdown();
      }
    } finally {
      ComplexHelper.stopCapacity(servers);
    }
  }

  @Test
  public void queueFullSend() throws Exception {
    ClientMetrics metrics = new ClientMetrics(new NoOpMetricsFactory());
    TestBed[] servers = new TestBed[2];
    SimpleExecutor executor = SimpleExecutor.create("executor");
    ExceptionLogger logger = (t, c) -> {};
    try {
      ClientConfig clientConfig = new TestClientConfig();
      ComplexHelper.spinUpCapacity(servers, true, ComplexHelper.SIMPLE);
      ReativeRoutingEngine engine = new ReativeRoutingEngine(metrics, executor, new MockSpaceTrackingEvents(), 5, 5);
      InstanceClientFinder finder = new InstanceClientFinder(servers[0].base, clientConfig, metrics, null, SimpleExecutorFactory.DEFAULT, 2, engine, logger);
      try {
        ConnectionBase base = new ConnectionBase(clientConfig, metrics, engine, finder, executor);
        MockSimpleEvents events = new MockSimpleEvents();
        Runnable gotConnected = events.latchAt(1);
        Runnable gotData = events.latchAt(2);
        Connection connection = new Connection(base, "127.0.0.1", "origin", "who", "dev", "space", "key", "{}", null, events);
        ArrayList<LatchedSeqCallback> callbacks = new ArrayList<>();
        for (int k = 0; k < 100; k++) {
          LatchedSeqCallback callback = new LatchedSeqCallback();
          callbacks.add(callback);
          connection.send("foo", "marker-" + k, "{}", callback);
        }
        for (int k = clientConfig.getConnectionQueueSize(); k < callbacks.size(); k++) {
          callbacks.get(k).assertFail(916520);
        }
        finder.sync(Helper.setOf("127.0.0.1:" + servers[0].port));
        connection.open();
        gotConnected.run();
        gotData.run();
        for (int k = 0; k < clientConfig.getConnectionQueueSize(); k++) {
          callbacks.get(k).assertJustSuccess();
        }
      } finally {
        finder.shutdown();
      }
    } finally {
      ComplexHelper.stopCapacity(servers);
    }
  }

  @Test
  public void queueUpdate() throws Exception {
    ClientMetrics metrics = new ClientMetrics(new NoOpMetricsFactory());
    TestBed[] servers = new TestBed[2];
    SimpleExecutor executor = SimpleExecutor.create("executor");
    ExceptionLogger logger = (t, c) -> {};
    try {
      ClientConfig clientConfig = new TestClientConfig();
      ComplexHelper.spinUpCapacity(servers, true, ComplexHelper.VIEW_MIRROR);
      ReativeRoutingEngine engine = new ReativeRoutingEngine(metrics, executor, new MockSpaceTrackingEvents(), 5, 5);
      InstanceClientFinder finder = new InstanceClientFinder(servers[0].base, clientConfig, metrics, null, SimpleExecutorFactory.DEFAULT, 2, engine, logger);
      try {
        ConnectionBase base = new ConnectionBase(clientConfig, metrics, engine, finder, executor);
        MockSimpleEvents events = new MockSimpleEvents();
        Runnable gotConnected = events.latchAt(1);
        Runnable gotData = events.latchAt(2);
        Connection connection = new Connection(base, "127.0.0.1", "origin", "who", "dev", "space", "key", "{}", null, events);
        for (int k = 0; k < 100; k++) {
          connection.update("{\"z\":" + k + "}");
        }
        finder.sync(Helper.setOf("127.0.0.1:" + servers[0].port));
        connection.open();
        gotConnected.run();
        events.assertWrite(0, "CONNECTED");
        gotData.run();
        events.assertWrite(1, "DELTA:{\"data\":{\"x\":123,\"zz\":1099},\"seq\":4}");
      } finally {
        finder.shutdown();
      }
    } finally {
      ComplexHelper.stopCapacity(servers);
    }
  }
}
