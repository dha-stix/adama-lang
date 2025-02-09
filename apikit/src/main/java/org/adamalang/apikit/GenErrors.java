/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.apikit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class GenErrors {

  public final Random rng;

  public GenErrors() throws Exception {
    rng = new Random();
  }

  public static void main(String[] args) throws Exception {
    GenErrors err = new GenErrors();
    HashSet<Integer> existingErrors = err.candidates(7);
    for (int k = 0; k < 500; k++) {
      insertBest(existingErrors, err.candidates(7));
    }
    for (int error : existingErrors) {
      System.err.println(error);
    }
  }

  public HashSet<Integer> candidates(int c) {
    HashSet<Integer> set = new HashSet<>();
    for (int k = 0; k < 10; k++) {
      set.add(generateErrorClass(c));
    }
    return set;
  }

  public static void insertBest(HashSet<Integer> errors, HashSet<Integer> candidates) {
    Iterator<Integer> it = candidates.iterator();
    int winner = it.next();
    long winningDistance = sumDistance(winner, errors);
    while (it.hasNext()) {
      int candidate = it.next();
      long candidateDistance = sumDistance(candidate, errors);
      if (candidateDistance > winningDistance) {
        winner = candidate;
        winningDistance = candidateDistance;
      }
    }
    errors.add(winner);
  }

  public int generateErrorClass(int c) {
    return c * 100000 + Math.abs(rng.nextInt(99999));
  }

  public static long sumDistance(int test, HashSet<Integer> others) {
    long d = 0;
    for (int other : others) {
      d += distance(test, other);
    }
    return d;
  }

  public static long distance(int x, int y) {
    int a = x;
    int b = y;
    long d = 0;
    while (a > 0 && b > 0) {
      int l = (a % 4) - (b % 4);
      d += l * l;
      a /= 4;
      b /= 4;
    }
    return d;
  }
}
