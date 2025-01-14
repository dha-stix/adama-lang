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

import org.adamalang.ErrorCodes;
import org.adamalang.common.ErrorCodeException;
import org.adamalang.common.Validators;

public class ValidateSpace {
  public static void validate(String space) throws ErrorCodeException {
    if (space.length() == 0) {
      throw new ErrorCodeException(ErrorCodes.API_INVALID_SPACE_EMPTY);
    }
    if (!Validators.simple(space, 127)) {
      throw new ErrorCodeException(ErrorCodes.API_INVALID_SPACE_NOT_SIMPLE);
    }
  }
}
