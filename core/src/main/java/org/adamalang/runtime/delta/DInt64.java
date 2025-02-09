/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.delta;

import org.adamalang.runtime.contracts.DeltaNode;
import org.adamalang.runtime.json.PrivateLazyDeltaWriter;

/** an int64 that will respect privacy and sends state to client only on changes */
public class DInt64 implements DeltaNode {
  private Long prior;

  public DInt64() {
    prior = null;
  }

  /** the int64 is no longer visible (was made private) */
  public void hide(final PrivateLazyDeltaWriter writer) {
    if (prior != null) {
      writer.writeNull();
      prior = null;
    }
  }

  @Override
  public void clear() {
    prior = null;
  }

  /** memory usage */
  @Override
  public long __memory() {
    return 40;
  }

  /** the int64 is visible, so show changes */
  public void show(final long value, final PrivateLazyDeltaWriter writer) {
    if (prior == null || value != prior.longValue()) {
      writer.writeFastString("" + value);
    }
    prior = value;
  }
}
