/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.translator.tree.types.checking.ruleset;

import org.adamalang.translator.env.Environment;
import org.adamalang.translator.tree.types.TyType;
import org.adamalang.translator.tree.types.natives.TyNativeMap;
import org.adamalang.translator.tree.types.reactive.TyReactiveMap;

public class RuleSetMap {
  public static boolean IsMap(final Environment environment, final TyType tyTypeOriginal) {
    var tyType =  RuleSetCommon.Resolve(environment, tyTypeOriginal, true);
    return tyType != null && (tyType instanceof TyNativeMap || tyType instanceof TyReactiveMap);
  }

  public static boolean IsNativeMap(final Environment environment, final TyType tyTypeOriginal) {
    var tyType =  RuleSetCommon.Resolve(environment, tyTypeOriginal, true);
    return tyType instanceof TyNativeMap;
  }

  public static boolean IsReactiveMap(final Environment environment, final TyType tyTypeOriginal) {
    var tyType =  RuleSetCommon.Resolve(environment, tyTypeOriginal, true);
    return tyType instanceof TyReactiveMap;
  }
}
