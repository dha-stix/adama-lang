/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.extern.prometheus;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.*;
import org.adamalang.common.metrics.*;

import java.util.Locale;
import java.util.regex.Pattern;

public class PrometheusMetricsFactory implements MetricsFactory {
  private final HTTPServer server;
  private static final double[] LATENCY_BUCKETS = new double[]{0.001D, 0.005D, 0.01D, 0.02D, 0.03D, 0.04D, 0.05D, 0.075D, 0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 1.0D, 2.0D, 5.0D, 10.0D};

  public PrometheusMetricsFactory(int metricsHttpPort) throws Exception {
    server = new HTTPServer.Builder()
        .withPort(metricsHttpPort)
        .build();
    new MemoryPoolsExports().register();
    new GarbageCollectorExports().register();
    new StandardExports().register();
    new MemoryAllocationExports().register();
    new ThreadExports().register();
  }

  public void shutdown() {
    server.close();
  }

  @Override
  public RequestResponseMonitor makeRequestResponseMonitor(String nameRaw) {
    String name = makeNameCompatibleWithPrometheus(nameRaw);
    Counter start = Counter.build().name("rr_" + name + "_start").help("Request started for " + name).register();
    Counter extra = Counter.build().name("rr_" + name + "_progress").help("Extra request data for " + name).register();
    Counter success = Counter.build().name("rr_" + name + "_success").help("Request success for " + name).register();
    Counter failure = Counter.build().name("rr_" + name + "_failure").help("Request failure for " + name).register();
    Gauge inflight = Gauge.build().name("rr_" + name + "_inflight").help("Inprogress streams for " + name).register();
    Histogram firstLatency = Histogram.build().name("rr_" + name + "_latency").buckets(LATENCY_BUCKETS).help("Latency for the first bit of progress for " + name).register();
    return () -> {
      Histogram.Timer timer = firstLatency.startTimer();
      start.inc();
      inflight.inc();
      return new RequestResponseMonitor.RequestResponseMonitorInstance() {
        boolean first = true;

        private void time() {
          if (first) {
            timer.close();
            inflight.dec();
            first = false;
          }
        }

        @Override
        public void success() {
          time();
          success.inc();
        }

        @Override
        public void extra() {
          extra.inc();
        }

        @Override
        public void failure(int code) {
          time();
          failure.inc();
        }
      };
    };
  }

  public static String makeNameCompatibleWithPrometheus(String nameRaw) {
    return nameRaw //
        .replaceAll(Pattern.quote("/"), "") //
        .replaceAll(Pattern.quote("-"), "") //
        .toLowerCase(Locale.ROOT);
  }

  @Override
  public StreamMonitor makeStreamMonitor(String nameRaw) {
    String name = makeNameCompatibleWithPrometheus(nameRaw);
    Counter start = Counter.build().name("stream_" + name + "_start").help("Stream requests started for " + name).register();
    Counter progress = Counter.build().name("stream_" + name + "_progress").help("Stream progress made for " + name).register();
    Counter finish = Counter.build().name("stream_" + name + "_finish").help("Stream finished for " + name).register();
    Counter failure = Counter.build().name("stream_" + name + "_failure").help("Stream filure for " + name).register();
    Gauge inflight = Gauge.build().name("stream_" + name + "_inflight").help("Inprogress streams for " + name).register();
    Histogram firstLatency = Histogram.build().name("stream_" + name + "_first_latency").buckets(LATENCY_BUCKETS).help("Latency for the first bit of progress for " + name).register();
    return () -> {
      Histogram.Timer timer = firstLatency.startTimer();
      start.inc();
      inflight.inc();
      return new StreamMonitor.StreamMonitorInstance() {
        boolean first = true;
        boolean lowered = false;

        private void time() {
          if (first) {
            timer.close();
            first = false;
          }
        }

        private void dec() {
          if (!lowered) {
            inflight.dec();
            lowered = true;
          }
        }

        @Override
        public void progress() {
          time();
          progress.inc();
        }

        @Override
        public void finish() {
          time();
          finish.inc();
          dec();
        }

        @Override
        public void failure(int code) {
          time();
          failure.inc();
          dec();
        }
      };
    };
  }

  @Override
  public ItemActionMonitor makeItemActionMonitor(String name) {
    Counter start = Counter.build().name("im_" + name + "_start").help("Item Monitor started for " + name).register();
    Counter executed = Counter.build().name("im_" + name + "_executed").help("Item Monitor executed for " + name).register();
    Counter rejected = Counter.build().name("im_" + name + "_rejected").help("Item Monitor rejected for " + name).register();
    Counter timeout = Counter.build().name("im_" + name + "_timeout").help("Item Monitor timeout for " + name).register();
    Gauge inflight = Gauge.build().name("im_" + name + "_inflight").help("Inprogress Item Monitors for " + name).register();
    Histogram latency = Histogram.build().name("im_" + name + "_latency").buckets(LATENCY_BUCKETS).help("Latency Item Monitor to succeed " + name).register();
    return () -> {
      start.inc();
      inflight.inc();
      Histogram.Timer timer = latency.startTimer();
      return new ItemActionMonitor.ItemActionMonitorInstance() {
        void fin() {
          inflight.dec();
          timer.close();
        }
        @Override
        public void executed() {
          executed.inc();
          fin();
        }

        @Override
        public void rejected() {
          rejected.inc();
          fin();
        }

        @Override
        public void timeout() {
          timeout.inc();
          fin();
        }
      };
    };
  }

  @Override
  public CallbackMonitor makeCallbackMonitor(String name) {
    Counter start = Counter.build().name("cb_" + name + "_start").help("Callback started for " + name).register();
    Counter success = Counter.build().name("cb_" + name + "_success").help("Callback success for " + name).register();
    Counter failure = Counter.build().name("cb_" + name + "_failure").help("Callback failure for " + name).register();
    Gauge inflight = Gauge.build().name("cb_" + name + "_inflight").help("Inprogress callbacks for " + name).register();
    Histogram latency = Histogram.build().name("cb_" + name + "_latency").buckets(LATENCY_BUCKETS).help("Latency callback to complete " + name).register();
    return new CallbackMonitor() {
      @Override
      public CallbackMonitor.CallbackMonitorInstance start() {
        start.inc();
        inflight.inc();
        Histogram.Timer timer = latency.startTimer();
        return new CallbackMonitorInstance() {
          boolean first = true;

          private void time() {
            if (first) {
              timer.observeDuration();
              inflight.dec();
              first = false;
            }
          }

          @Override
          public void success() {
            success.inc();
            time();
          }

          @Override
          public void failure(int code) {
            failure.inc();
            time();
          }
        };
      }
    };
  }

  @Override
  public Runnable counter(String name) {
    Counter start = Counter.build().name("raw_" + name).help("Raw counter for " + name).register();
    return () -> {
      start.inc();
    };
  }

  @Override
  public Inflight inflight(String name) {
    Gauge inflight = Gauge.build().name("inf_" + name).help("Inflight measure for " + name).register();
    return new Inflight() {
      @Override
      public void up() {
        inflight.inc();
      }

      @Override
      public void down() {
        inflight.dec();
      }

      @Override
      public void set(int value) {
        inflight.set(value);
      }
    };
  }

  @Override
  public void page(String name, String title) {
  }

  @Override
  public void section(String title) {
  }
}
