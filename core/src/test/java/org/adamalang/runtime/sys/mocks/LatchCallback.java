package org.adamalang.runtime.sys.mocks;

import org.adamalang.runtime.contracts.Callback;
import org.adamalang.runtime.exceptions.ErrorCodeException;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LatchCallback implements Callback<Integer> {

    private CountDownLatch latch;
    private int value;
    private ErrorCodeException ex;

    public LatchCallback() {
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void success(Integer value) {
        this.value = value;
        latch.countDown();
    }

    @Override
    public void failure(ErrorCodeException ex) {
        this.ex = ex;
        latch.countDown();
    }

    public void await_success(int value) {
        try {
            Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
            Assert.assertEquals(value, this.value);
        } catch (InterruptedException ie) {
            Assert.fail();
        }
    }
    public void await_failure(int code) {
        try {
            Assert.assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
            Assert.assertEquals(code, ex.code);
        } catch (InterruptedException ie) {
            Assert.fail();
        }
    }
}