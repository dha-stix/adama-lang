/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.net.client.contracts;

import org.adamalang.common.ErrorCodeException;

/** what happens when you subscribe to a key */
public interface RoutingSubscriber {

  /** routing found the document in another region */
  public void onRegion(String region);

  /** routing found the document within the current region on a specific machine */
  public void onMachine(String machine);

  /** a failure in finding a machine */
  public void failure(ErrorCodeException ex);
}
