/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.runtime.stdlib;

import org.adamalang.runtime.natives.NtComplex;
import org.adamalang.runtime.natives.NtMaybe;
import org.adamalang.translator.reflect.Extension;
import org.adamalang.translator.reflect.HiddenType;

import java.util.function.BiFunction;

/** a very simple math library which extends some of the already exposed Math calls */
public class LibMath {
  public static double SQRT2 = 1.4142135623730950488016887242097;

  @Extension
  public static NtComplex sqrt(final double x) {
    if (x < 0) {
      return new NtComplex(0.0, Math.sqrt(-x));
    }
    return new NtComplex(Math.sqrt(x), 0.0);
  }

  @Extension
  public static int ceil(final double x) {
    return (int) Math.ceil(x);
  }

  @Extension
  public static int floor(final double x) {
    return (int) Math.floor(x);
  }

  @Extension
  public static boolean near(final NtComplex a, final NtComplex b) {
    return near(a.real, b.real) && near(a.imaginary, b.imaginary);
  }

  @Extension
  public static boolean near(final double a, final double b) {
    final var diff = Math.abs(a - b);
    return diff < 0.0000001;
  }

  @Extension
  public static boolean near(final NtComplex a, int b) {
    return near(a.real, b) && near(a.imaginary, b);
  }

  @Extension
  public static boolean near(final NtComplex a, long b) {
    return near(a.real, b) && near(a.imaginary, b);
  }

  @Extension
  public static boolean near(final NtComplex a, double b) {
    return near(a.real, b) && near(a.imaginary, b);
  }

  @Extension
  public static boolean xor(final boolean a, final boolean b) {
    if (a) {
      return !b;
    }
    return b;
  }

  @Extension
  public static int round(final double x) {
    return (int) Math.round(x);
  }

  @Extension
  public static double roundTo(final double x, int numberOfDigits) {
    double shift = Math.pow(10, numberOfDigits);
    return Math.round(x * shift) / shift;
  }

  @Extension
  public static NtComplex conj(final NtComplex x) {
    return new NtComplex(x.real, -x.imaginary);
  }

  @Extension
  public static double len(final NtComplex x) {
    return Math.sqrt(x.real * x.real + x.imaginary * x.imaginary);
  }

  public static boolean isTrue(@HiddenType(clazz = Boolean.class) NtMaybe<Boolean> x) {
    if (x.has()) {
      return x.get();
    }
    return false;
  }

  public static <T> boolean equality(NtMaybe<T> x, T y, BiFunction<T, T, Boolean> check) {
    if (x.has()) {
      return check.apply(x.get(), y);
    }
    return false;
  }
}
