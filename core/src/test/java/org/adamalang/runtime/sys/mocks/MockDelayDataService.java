/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.sys.mocks;

import org.adamalang.common.Callback;
import org.adamalang.runtime.data.*;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockDelayDataService implements DataService {
  private final ArrayList<CountDownLatch> latches;
  private DataService parent;
  private boolean paused;
  private ArrayList<Runnable> actions;

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

  public void unpause() {
    System.out.println("UNPAUSED");
    for (Runnable r : unpauseWithLock()) {
      r.run();
    }
  }

  private synchronized ArrayList<Runnable> unpauseWithLock() {
    ArrayList<Runnable> copy = new ArrayList<>(actions);
    actions.clear();
    paused = false;
    return copy;
  }

  public void once() {
    System.out.println("ONCE");
    onceWithLock().run();
  }

  private synchronized Runnable onceWithLock() {
    return actions.remove(0);
  }

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    enqueue(() -> parent.get(key, callback));
  }

  private void enqueue(Runnable run) {
    enqueueWithLock(run).run();
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

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    enqueue(() -> parent.initialize(key, patch, callback));
  }

  @Override
  public void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    enqueue(() -> parent.patch(key, patches, callback));
  }

  @Override
  public void compute(
      Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    enqueue(() -> parent.compute(key, method, seq, callback));
  }

  @Override
  public void delete(Key key, Callback<Void> callback) {
    enqueue(() -> parent.delete(key, callback));
  }

  @Override
  public void snapshot(Key key, int seq, String snapshot, int history, Callback<Integer> callback) {
    enqueue(() -> parent.snapshot(key, seq, snapshot, history, callback));
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    enqueue(() -> parent.close(key, callback));
  }

  @Override
  public void archive(Key key, ArchiveWriter writer) {
    enqueue(() -> parent.archive(key, writer));
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
}
