/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.common;

import java.util.ArrayList;

/** This aims to be a high resolution scheduler such that events can be executed after minimal wait time (i.e. 1 ms) */
public class PreciseSchedulerBus implements Runnable {
  private final ArrayList<Runnable>[] schedule;
  private int at;
  private long delay;
  private long snapshot;

  /** create with the given argument indicating how many milliseconds we can schedule in the future */
  public PreciseSchedulerBus(int max) {
    this.schedule = new ArrayList[max];
    this.at = 0;
    for (int k = 0; k < max; k++) {
      schedule[k] = new ArrayList<>();
    }
    this.delay = 1000000;
    this.snapshot = System.nanoTime();
  }

  @Override
  public void run() {
    while (true) {
      ArrayList<Runnable> local = null;
      synchronized (schedule) {
        if (schedule[at].size() > 0) {
          local = new ArrayList<>(schedule[at]);
          schedule[at].clear();
        }
        at++;
        at %= schedule.length;
      }
      if (local != null) {
        for (Runnable task : local) {
          task.run();
        }
      }
      try {
        long newSnapshot = System.nanoTime();
        int sinceLast = (int) (newSnapshot - snapshot);
        snapshot = newSnapshot;
        Thread.sleep(0, sinceLast >= 1000000 ? Math.max(250000, 1750000 - sinceLast) : 750000);
      } catch (InterruptedException ie) {
        return;
      }
    }
  }

  /** transfer the named runnable into the executor after the given milliseconds */
  public void schedule(SimpleExecutor executor, NamedRunnable runnable, int futureMilliseconds) {
    synchronized (schedule) {
      schedule[(at + futureMilliseconds / 2) % schedule.length].add(() -> {
        executor.execute(runnable);
      });
    }
  }
}
