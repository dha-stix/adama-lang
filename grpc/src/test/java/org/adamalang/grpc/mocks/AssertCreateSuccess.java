package org.adamalang.grpc.mocks;

import org.adamalang.grpc.client.contracts.CreateCallback;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AssertCreateSuccess implements CreateCallback {
    private final CountDownLatch latch;
    private boolean success;

    public AssertCreateSuccess() {
        this.latch = new CountDownLatch(1);
        this.success = false;
    }

    @Override
    public void created() {
        success = true;
        latch.countDown();
    }

    @Override
    public void error(int code) {
        latch.countDown();
    }

    public void await() {
        try {
            Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
            Assert.assertTrue(success);
        } catch (InterruptedException ie) {
            Assert.fail();
        }
    }
}