/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.delta.secure;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.SecretKey;

public class SecureAssetUtilTests {
  @Test
  public void assets() throws Exception {
    String str = SecureAssetUtil.makeAssetKeyHeader();
    SecretKey key = SecureAssetUtil.secretKeyOf(str);
    key.getEncoded();
    key.getAlgorithm();
    key.getFormat();
  }

  @Test
  public void coverage() {
    try {
      SecureAssetUtil.encryptToBase64(null, "x");
      Assert.fail();
    } catch (RuntimeException re) {

    }
    try {
      SecureAssetUtil.decryptFromBase64(null, "x");
      Assert.fail();
    } catch (RuntimeException re) {

    }
    try {
      SecureAssetUtil.getKeyGenerator("X");
      Assert.fail();
    } catch (RuntimeException re) {

    }
  }
}
