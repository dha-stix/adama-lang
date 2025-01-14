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

import org.adamalang.runtime.delta.secure.SecureAssetUtil;
import org.junit.Assert;
import org.junit.Test;

public class AssetRequestTests {

  @Test
  public void flow() {
    String assetKey = SecureAssetUtil.makeAssetKeyHeader();
    String uri = "space/key/id=" + SecureAssetUtil.encryptToBase64(SecureAssetUtil.secretKeyOf(assetKey), "my-id");
    AssetRequest ar = AssetRequest.parse(uri, assetKey);
    Assert.assertEquals("space", ar.space);
    Assert.assertEquals("key", ar.key);
    Assert.assertEquals("my-id", ar.id);
  }

  @Test
  public void flowWithSlash() {
    String assetKey = SecureAssetUtil.makeAssetKeyHeader();
    String uri = "space/key/path/path2/id=" + SecureAssetUtil.encryptToBase64(SecureAssetUtil.secretKeyOf(assetKey), "my-id");
    AssetRequest ar = AssetRequest.parse(uri, assetKey);
    Assert.assertEquals("space", ar.space);
    Assert.assertEquals("key/path/path2", ar.key);
    Assert.assertEquals("my-id", ar.id);
  }

}
