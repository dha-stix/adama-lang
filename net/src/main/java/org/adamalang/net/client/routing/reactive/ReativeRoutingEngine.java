/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.net.client.routing.reactive;

import org.adamalang.common.NamedRunnable;
import org.adamalang.common.SimpleExecutor;
import org.adamalang.net.client.ClientMetrics;
import org.adamalang.net.client.contracts.RoutingSubscriber;
import org.adamalang.net.client.contracts.RoutingTarget;
import org.adamalang.net.client.contracts.SpaceTrackingEvents;
import org.adamalang.net.client.routing.Router;
import org.adamalang.runtime.data.Key;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Consumer;

public class ReativeRoutingEngine implements RoutingTarget, Router {
  private final ClientMetrics metrics;
  private final SimpleExecutor executor;
  private final RoutingTable table;
  private final int broadcastDelayOffset;
  private final int broadcastDelayJitter;
  private boolean broadcastInflight;

  public ReativeRoutingEngine(ClientMetrics metrics, SimpleExecutor executor, SpaceTrackingEvents events, int broadcastDelayOffset, int broadcastDelayJitter) {
    this.metrics = metrics;
    this.executor = executor;
    this.table = new RoutingTable(events);
    this.broadcastInflight = false;
    this.broadcastDelayOffset = broadcastDelayOffset;
    this.broadcastDelayJitter = broadcastDelayJitter;
  }

  public void list(String space, Consumer<TreeSet<String>> callback) {
    executor.execute(new NamedRunnable("listing-targets") {
      @Override
      public void execute() throws Exception {
        callback.accept(table.targetsFor(space));
      }
    });
  }

  public void random(Consumer<String> callback) {
    executor.execute(new NamedRunnable("find-random-target") {
      @Override
      public void execute() throws Exception {
        callback.accept(table.random());
      }
    });
  }

  @Override
  public void get(Key key, RoutingSubscriber callback) {
    executor.execute(new NamedRunnable("get", key.space, key.key) {
      @Override
      public void execute() throws Exception {
        callback.onMachine(table.get(key.space, key.key));
      }
    });
  }

  @Override
  public void integrate(String target, Collection<String> newSpaces) {
    executor.execute(new NamedRunnable("routing-integrate", target) {
      @Override
      public void execute() throws Exception {
        table.integrate(target, newSpaces);
        scheduleBroadcastWhileInExecutor();
      }
    });
  }

  private void scheduleBroadcastWhileInExecutor() {
    if (!broadcastInflight) {
      broadcastInflight = true;
      executor.schedule(new NamedRunnable("routing-broadcast") {
        @Override
        public void execute() throws Exception {
          table.broadcast();
          broadcastInflight = false;
        }
      }, (int) (broadcastDelayOffset + Math.random() * broadcastDelayJitter));
    }
  }

  public void remove(String target) {
    executor.execute(new NamedRunnable("routing-remove", target) {
      @Override
      public void execute() throws Exception {
        table.remove(target);
        scheduleBroadcastWhileInExecutor();
      }
    });
  }

  @Override
  public void subscribe(Key key, RoutingSubscriber subscriber, Consumer<Runnable> onCancel) {
    executor.execute(new NamedRunnable("routing-subscribe", key.space, key.key) {
      @Override
      public void execute() throws Exception {
        Runnable cancel = table.subscribe(key, (machine) -> subscriber.onMachine(machine));
        onCancel.accept(() -> executor.execute(new NamedRunnable("routing-unsubscribe") {
          @Override
          public void execute() throws Exception {
            cancel.run();
          }
        }));
      }
    });
  }
}
