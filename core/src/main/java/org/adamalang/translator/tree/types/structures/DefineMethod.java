/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.translator.tree.types.structures;

import org.adamalang.translator.env.Environment;
import org.adamalang.translator.parser.token.Token;
import org.adamalang.translator.tree.common.StringBuilderWithTabs;
import org.adamalang.translator.tree.definitions.FunctionArg;
import org.adamalang.translator.tree.statements.Block;
import org.adamalang.translator.tree.statements.ControlFlow;
import org.adamalang.translator.tree.types.TyType;
import org.adamalang.translator.tree.types.natives.functions.FunctionOverloadInstance;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DefineMethod extends StructureComponent {
  public final ArrayList<FunctionArg> args;
  public final Token closeParen;
  /** code that defines the function */
  public final Block code;
  /** return type of the function */
  public final Token introduceReturnToken;
  /** the name of the function */
  public final Token methodToken;

  public final String name;
  public final Token nameToken;
  /** arguments of the function */
  public final Token openParen;

  public final Token tokenReadonly;
  public TyType returnType;
  private FunctionOverloadInstance cachedInstance;
  private int functionId;

  /** construct the function of a type with a name */
  public DefineMethod(final Token methodToken, final Token nameToken, final Token openParen, final ArrayList<FunctionArg> args, final Token closeParen, final Token introduceReturnToken, final TyType returnType, final Token tokenReadonly, final Block code) {
    this.methodToken = methodToken;
    this.nameToken = nameToken;
    name = nameToken.text;
    this.openParen = openParen;
    this.args = args;
    this.closeParen = closeParen;
    this.introduceReturnToken = introduceReturnToken;
    this.returnType = returnType;
    this.tokenReadonly = tokenReadonly;
    this.code = code;
    cachedInstance = null;
    ingest(methodToken);
    ingest(nameToken);
    ingest(openParen);
    ingest(closeParen);
    ingest(code);
  }

  @Override
  public void emit(final Consumer<Token> yielder) {
    yielder.accept(methodToken);
    yielder.accept(nameToken);
    yielder.accept(openParen);
    for (final FunctionArg arg : args) {
      if (arg.commaToken != null) {
        yielder.accept(arg.commaToken);
      }
      arg.type.emit(yielder);
      yielder.accept(arg.argNameToken);
    }
    yielder.accept(closeParen);
    if (introduceReturnToken != null) {
      yielder.accept(introduceReturnToken);
      returnType.emit(yielder);
    }
    if (tokenReadonly != null) {
      yielder.accept(tokenReadonly);
    }
    code.emit(yielder);
  }

  public FunctionOverloadInstance typing(final Environment environment) {
    if (cachedInstance == null) {
      functionId = environment.autoVariable();
      returnType = environment.rules.Resolve(returnType, false);
      final var argTypes = new ArrayList<TyType>();
      for (final FunctionArg arg : args) {
        arg.typing(environment);
        argTypes.add(arg.type);
      }
      final var flow = code.typing(prepareEnvironment(environment));
      if (returnType != null && flow == ControlFlow.Open) {
        environment.document.createError(this, String.format("Function '%s' does not return in all cases", nameToken.text), "MethodDefine");
      }
      cachedInstance = new FunctionOverloadInstance("__METH_" + functionId + "_" + name, returnType, argTypes, tokenReadonly != null, false);
      cachedInstance.ingest(this);
    }
    return cachedInstance;
  }

  /** prepare the environment for execution */
  private Environment prepareEnvironment(final Environment environment) {
    var toUse = tokenReadonly != null ? environment.scopeAsReadOnlyBoundary() : environment.scopeWithCache("__cache");
    for (final FunctionArg arg : args) {
      toUse.define(arg.argName, arg.type, true, arg.type);
    }
    toUse.setReturnType(returnType);
    return toUse;
  }

  /** write the java for the function/procedure */
  public void writeFunctionJava(final StringBuilderWithTabs sb, final Environment environment) {
    sb.append("private ");
    if (returnType == null) {
      sb.append("void");
    } else {
      sb.append(returnType.getJavaConcreteType(environment));
    }
    sb.append(" ").append("__METH_").append(functionId + "_").append(name).append("(");
    var first = true;
    for (final FunctionArg arg : args) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(arg.type.getJavaConcreteType(environment)).append(" ").append(arg.argName);
    }
    sb.append(") ");
    code.writeJava(sb, prepareEnvironment(environment));
    sb.writeNewline();
  }
}
