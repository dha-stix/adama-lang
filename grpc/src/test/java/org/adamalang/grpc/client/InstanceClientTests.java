package org.adamalang.grpc.client;

import org.adamalang.grpc.TestBed;
import org.adamalang.grpc.client.contracts.ClientLifecycle;
import org.adamalang.grpc.mocks.*;
import org.adamalang.runtime.contracts.ExceptionLogger;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class InstanceClientTests {
    @Test
    public void client_survives_server_stop() throws Exception {
        try (TestBed bed = new TestBed(12346, "@connected(who) { return true; } public int x; @construct { x = 123; transition #p in 0.5; } #p { x++; } ")) {
            MockClentLifecycle lifecycle = new MockClentLifecycle();
            try (InstanceClient client = new InstanceClient(bed.identity, "127.0.0.1:12346", bed.clientExecutor, lifecycle, new StdErrLogger())) {
                {
                    AssertCreateFailure failure = new AssertCreateFailure();
                    client.create("nope", "nope", "space", "1", null, "{}", failure);
                    Assert.assertFalse(client.ping(2500));
                    failure.await(723982);
                }
                bed.startServer();
                Assert.assertTrue(client.ping(5000));
                {
                    AssertCreateSuccess success = new AssertCreateSuccess();
                    client.create("nope", "nope", "space", "2", "123", "{}", success);
                    success.await();
                }
                bed.stopServer();
                Assert.assertFalse(client.ping(5000));
                {
                    AssertCreateFailure failure = new AssertCreateFailure();
                    client.create("nope", "nope", "space", "3", "42", "{}", failure);
                    failure.await(723982);
                }
                bed.startServer();
                Assert.assertTrue(client.ping(5000));
                {
                    AssertCreateSuccess success = new AssertCreateSuccess();
                    client.create("nope", "nope", "space", "4", null, "{}", success);
                    success.await();
                }
            }
        }
    }

    @Test
    public void client_connects() throws Exception {
        try (TestBed bed = new TestBed(12346, "@connected(who) { return true; } public int x; @construct { x = 123; transition #p in 0.1; } #p { x++; } ")) {
            bed.startServer();
            MockClentLifecycle lifecycle = new MockClentLifecycle();
            MockRemoveDocumentEvents events = new MockRemoveDocumentEvents();
            Runnable happy = events.latchAt(5);
            Runnable disconnect = events.latchAt(6);
            Runnable reconnect = events.latchAt(8);
            Runnable disconnectAgain = events.latchAt(9);
            AtomicBoolean created = new AtomicBoolean(false);
            try (InstanceClient client = new InstanceClient(bed.identity, "127.0.0.1:12346", bed.clientExecutor, new ClientLifecycle() {
                @Override
                public void connected(InstanceClient client) {
                    System.err.println("connected");
                    if (created.compareAndExchange(false, true) == false) {
                        AssertCreateSuccess success = new AssertCreateSuccess();
                        client.create("nope", "nope", "space", "1", "123", "{}", success);
                        success.await();
                    }
                    client.connect("nope", "test", "space", "1", events);
                    lifecycle.connected(client);
                }

                @Override
                public void disconnected(InstanceClient client) {
                    lifecycle.disconnected(client);
                }
            }, (t, errorCode) -> {
                System.err.println("EXCEPTION:" + t.getMessage());
            })) {
                happy.run();
                bed.stopServer();
                disconnect.run();
                bed.startServer();
                reconnect.run();
                bed.stopServer();
                disconnectAgain.run();
                events.assertWrite(0, "CONNECTED");
                events.assertWrite(1, "DELTA:{\"data\":{\"x\":123},\"seq\":4}");
                events.assertWrite(2, "DELTA:{\"data\":{\"x\":124},\"seq\":5}");
                events.assertWrite(3, "DELTA:{\"seq\":6}");
                events.assertWrite(4, "DELTA:{\"seq\":7}");
                events.assertWrite(5, "DISCONNECTED");
                events.assertWrite(6, "CONNECTED");
                events.assertWrite(7, "DELTA:{\"data\":{\"x\":124},\"seq\":12}");
                events.assertWrite(8, "DISCONNECTED");
            }
        }
    }
}