/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.web.service;

import org.adamalang.common.metrics.NoOpMetricsFactory;
import org.adamalang.web.service.mocks.MockServiceBase;
import org.junit.Test;

public class InitializerTests {
  @Test
  public void sanity() throws Exception {
    new Initializer(WebConfigTests.mockConfig(WebConfigTests.Scenario.Dev), new WebMetrics(new NoOpMetricsFactory()), new MockServiceBase(), null);
  }
}
