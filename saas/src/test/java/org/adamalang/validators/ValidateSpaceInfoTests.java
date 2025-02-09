/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.validators;

import org.adamalang.common.ErrorCodeException;
import org.junit.Assert;
import org.junit.Test;

public class ValidateSpaceInfoTests {
  @Test
  public void tooLong() throws Exception {
    StringBuilder sb = new StringBuilder();
    for (int k = 0; k < 127; k++) {
      sb.append("a");
      ValidateSpace.validate(sb.toString());
    }
    try {
      sb.append("a");
      ValidateSpace.validate(sb.toString());
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(998515, ece.code);
    }
  }

  @Test
  public void tooShort() {
    try {
      ValidateSpace.validate("");
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(937076, ece.code);
    }
  }

  @Test
  public void tooComplex() {
    try {
      ValidateSpace.validate("#&");
      Assert.fail();
    } catch (ErrorCodeException ece) {
      Assert.assertEquals(998515, ece.code);
    }
  }

  @Test
  public void good() throws Exception {
    ValidateSpace.validate("simple");
  }
}
