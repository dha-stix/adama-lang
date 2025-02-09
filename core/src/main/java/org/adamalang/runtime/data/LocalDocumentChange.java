/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.data;

/** the local copy of the document should be changed by incorporating the given patch */
public class LocalDocumentChange {
  public final String patch;
  public final int reads;
  public final int seq;

  public LocalDocumentChange(String patch, int reads, int seq) {
    this.patch = patch;
    this.reads = reads;
    this.seq = seq;
  }
}
