package org.adamalang.runtime.sys.mocks;

import org.adamalang.runtime.contracts.ActiveKeyStream;
import org.adamalang.runtime.contracts.Callback;
import org.adamalang.runtime.contracts.DataService;
import org.adamalang.runtime.contracts.Key;
import org.adamalang.runtime.natives.NtClient;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockDelayDataService implements DataService {
    private DataService parent;
    private boolean paused;
    private ArrayList<Runnable> actions;
    private final ArrayList<CountDownLatch> latches;

    public MockDelayDataService(DataService parent) {
        this.paused = false;
        this.parent = parent;
        this.actions = new ArrayList<>();
        this.latches = new ArrayList<>();
    }

    public synchronized void pause() {
        System.out.println("PAUSE");
        this.paused = true;
    }

    public synchronized void set(DataService service) {
        this.parent = service;
    }

    private synchronized ArrayList<Runnable> unpauseWithLock() {
        ArrayList<Runnable> copy = new ArrayList<>(actions);
        actions.clear();
        paused = false;
        return copy;
    }

    private synchronized Runnable onceWithLock() {
        return actions.remove(0);
    }

    public void unpause() {
        System.out.println("UNPAUSED");
        for (Runnable r : unpauseWithLock()) {
            r.run();
        }
    }

    public void once() {
        System.out.println("ONCE");
        onceWithLock().run();
    }

    @Override
    public synchronized void scan(ActiveKeyStream streamback) {
        parent.scan(streamback);
    }

    private synchronized Runnable enqueueWithLock(Runnable run) {
        if (paused) {
            actions.add(run);
            Iterator<CountDownLatch> it = latches.iterator();
            while (it.hasNext()) {
                CountDownLatch latch = it.next();
                latch.countDown();
                if (latch.getCount() == 0) {
                    it.remove();
                }
            }
            return () -> {};
        } else {
            return run;
        }
    }

    public synchronized Runnable latchAt(int count) {
        CountDownLatch latch = new CountDownLatch(count);
        latches.add(latch);
        return () -> {
            try {
                Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
            } catch (InterruptedException ie) {
                Assert.fail();
            }
        };
    }

    private void enqueue(Runnable run) {
        enqueueWithLock(run).run();
    }

    @Override
    public void get(Key key, Callback<LocalDocumentChange> callback) {
        enqueue(() -> parent.get(key, callback));
    }

    @Override
    public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
        enqueue(() -> parent.initialize(key, patch, callback));
    }

    @Override
    public void patch(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
        enqueue(() -> parent.patch(key, patch, callback));
    }

    @Override
    public void compute(Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
        enqueue(() -> parent.compute(key, method, seq, callback));
    }

    @Override
    public void delete(Key key, Callback<Void> callback) {
        enqueue(() -> parent.delete(key, callback));
    }
}