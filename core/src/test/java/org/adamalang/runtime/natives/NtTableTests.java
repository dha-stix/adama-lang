/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.natives;

import org.adamalang.runtime.mocks.MockMessage;
import org.junit.Assert;
import org.junit.Test;

public class NtTableTests {
  @Test
  public void flow() {
    final var table = new NtTable<>(MockMessage::new);
    new NtTable<>(table);
    table.make();
    Assert.assertEquals(1, table.size());
    table.make();
    Assert.assertEquals(2, table.size());
    table.delete();
    Assert.assertEquals(0, table.size());
    Assert.assertEquals(0, table.iterate(false).size());
    table.make();
    table.make();
    table.make();
    Assert.assertEquals(3, table.iterate(false).size());
    table.__raiseInvalid();
  }
}
