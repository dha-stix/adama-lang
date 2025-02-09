/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.translator.tree.definitions;

import org.adamalang.translator.env.Environment;
import org.adamalang.translator.parser.token.Token;
import org.adamalang.translator.tree.definitions.web.Uri;
import org.adamalang.translator.tree.definitions.web.UriAction;
import org.adamalang.translator.tree.statements.Block;
import org.adamalang.translator.tree.statements.ControlFlow;
import org.adamalang.translator.tree.types.TyType;

import java.util.TreeMap;
import java.util.function.Consumer;

/** defines a URI to get a web resource */
public class DefineWebGet extends Definition implements UriAction {

  public final Token webToken;
  public final Token getToken;
  public final Uri uri;
  public final Block code;

  public DefineWebGet(Token webToken, Token getToken, Uri uri, Block code) {
    this.webToken = webToken;
    this.getToken = getToken;
    this.uri = uri;
    this.code = code;
    ingest(webToken);
    ingest(code);
  }

  @Override
  public void emit(Consumer<Token> yielder) {
    yielder.accept(webToken);
    yielder.accept(getToken);
    uri.emit(yielder);
    code.emit(yielder);
  }

  @Override
  public TreeMap<String, TyType> parameters() {
    return uri.variables;
  }

  public Environment next(Environment environment) {
    Environment env = environment.scopeAsReadOnlyBoundary().scopeAsWeb();
    uri.extendInto(env);
    uri.typing(env);
    return env;
  }

  @Override
  public void typing(Environment environment) {
    Environment env = next(environment);
    if (code.typing(env) == ControlFlow.Open) {
      environment.document.createError(this, String.format("The @web handlers must return a message"), "Web");
    }
  }
}
