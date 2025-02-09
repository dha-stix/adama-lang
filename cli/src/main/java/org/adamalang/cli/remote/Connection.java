/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.cli.remote;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.adamalang.common.ErrorCodeException;
import org.adamalang.common.Json;
import org.adamalang.web.client.WebClientConnection;
import org.adamalang.web.contracts.WebJsonStream;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** a single connection to a remote Web Proxy host */
public class Connection implements AutoCloseable {
  private final WebClientConnection connection;

  public Connection(WebClientConnection connection) {
    this.connection = connection;
  }

  public void stream(ObjectNode request, BiConsumer<Integer, ObjectNode> stream) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Exception> failure = new AtomicReference<>(null);
    connection.execute(request, new WebJsonStream() {
      @Override
      public void data(int cId, ObjectNode node) {
        stream.accept(cId, node);
      }

      @Override
      public void complete() {
        latch.countDown();
      }

      @Override
      public void failure(int code) {
        failure.set(new ErrorCodeException(code));
        latch.countDown();
      }
    });
    latch.await(60000, TimeUnit.MILLISECONDS);
    if (failure.get() != null) {
      throw failure.get();
    }
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }

  public ObjectNode execute(ObjectNode request) throws Exception {
    AtomicReference<Object> value = new AtomicReference<>(null);
    CountDownLatch latch = new CountDownLatch(1);
    connection.execute(request, new WebJsonStream() {
      @Override
      public void data(int cId, ObjectNode node) {
        value.set(node);
      }

      @Override
      public void complete() {
        latch.countDown();
      }

      @Override
      public void failure(int code) {
        value.set(new ErrorCodeException(code));
        latch.countDown();
      }
    });
    if (latch.await(5000, TimeUnit.MILLISECONDS)) {
      if (value.get() != null) {
        if (value.get() instanceof ObjectNode) {
          return (ObjectNode) value.get();
        } else {
          throw (Exception) value.get();
        }
      }
      return Json.newJsonObject();
    } else {
      throw new Exception("timed out");
    }
  }
}
