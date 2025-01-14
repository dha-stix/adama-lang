/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.rxhtml.template;

import org.adamalang.rxhtml.acl.commands.Command;
import org.adamalang.rxhtml.atl.Parser;
import org.adamalang.rxhtml.atl.tree.Tree;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class Attributes {

  public final Environment env;
  public final String eVar;
  public final boolean expand;

  public Attributes(Environment env, String eVar) {
    this.env = env;
    this.eVar = eVar;
    this.expand = env.element.hasAttr("rx:expand-view-state");
  }

  public void _if() {
    commonBetweenIfAndIfNot("rx:if");
  }

  public void commonBetweenIfAndIfNot(String version) {
    String value = env.element.attr(version);
    if (value.startsWith("decide:")) {
      String channel = value.substring(7);
      String key = env.element.hasAttr("key") ? env.element.attr("key") : "id";
      StatePath pathName = StatePath.resolve(env.element.hasAttr("name") ? env.element.attr(version) : "id", env.stateVar);
      String childStateVar = env.pool.ask();
      String parentVar = env.pool.ask();
      env.writer.tab().append("$.DE(").append(eVar).append(",").append(env.stateVar).append(",").append(pathName.command);
      env.writer.append(",'").append(channel).append("','").append(key).append("'");
      env.writer.append(",'").append(pathName.name).append("', ").append(version.equals("rx:if") ? "true" : "false").append(",").append(expand ? "true" : "false").append(",function(").append(parentVar).append(",").append(childStateVar).append(") {").tabUp().newline();
      Base.children(env.stateVar(childStateVar).parentVariable(parentVar), (node) -> {
        if (node instanceof Element) {
          return !node.hasAttr("rx:else");
        } else {
          return true;
        }
      });
      env.writer.tabDown().tab().append("}, function(").append(parentVar).append(",").append(childStateVar).append(") {").tabUp().newline();
      Base.children(env.stateVar(childStateVar).parentVariable(parentVar), (node) -> {
        if (node instanceof Element) {
          return node.hasAttr("rx:else");
        } else {
          return false;
        }
      });
      env.writer.tabDown().tab().append("});").newline();
      env.pool.give(childStateVar);
      env.pool.give(parentVar);
    } else {
      StatePath path = StatePath.resolve(env.element.attr(version), env.stateVar);
      String childStateVar = env.pool.ask();
      String parentVar = env.pool.ask();
      env.writer.tab().append("$.IF(").append(eVar).append(",").append(path.command);
      env.writer.append(",'").append(path.name).append("', ").append(version.equals("rx:if") ? "true" : "false").append(",").append(expand ? "true" : "false").append(",function(").append(parentVar).append(",").append(childStateVar).append(") {").tabUp().newline();
      Base.children(env.stateVar(childStateVar).parentVariable(parentVar), (node) -> {
        if (node instanceof Element) {
          return !node.hasAttr("rx:else");
        } else {
          return true;
        }
      });
      env.writer.tabDown().tab().append("}, function(").append(parentVar).append(",").append(childStateVar).append(") {").tabUp().newline();
      Base.children(env.stateVar(childStateVar).parentVariable(parentVar), (node) -> {
        if (node instanceof Element) {
          return node.hasAttr("rx:else");
        } else {
          return false;
        }
      });
      env.writer.tabDown().tab().append("});").newline();
      env.pool.give(childStateVar);
      env.pool.give(parentVar);
    }
  }

  public void _ifnot() {
    commonBetweenIfAndIfNot("rx:ifnot");
  }

  public void _iterate() {
    StatePath path = StatePath.resolve(env.element.attr("rx:iterate"), env.stateVar);
    String childStateVar = env.pool.ask();
    env.writer.tab().append("$.IT(").append(eVar).append(",").append(path.command).append(",'").append(path.name).append("',").append(expand ? "true" : "false").append(",function(").append(childStateVar).append(") {").tabUp().newline();
    String childDomVar = Base.write(env.stateVar(childStateVar).parentVariable(null).element(env.soloChild(), true), true);
    env.writer.tab().append("return ").append(childDomVar).append(";").newline();
    env.pool.give(childDomVar);
    env.writer.tabDown().tab().append("});").newline();
    env.pool.give(childStateVar);
  }

  public void _switch() {
    StatePath path = StatePath.resolve(env.element.attr("rx:switch"), env.stateVar);
    String childStateVar = env.pool.ask();
    String caseVar = env.pool.ask();
    String parentVar = env.pool.ask();
    env.writer.tab().append("$.SW(").append(eVar).append(",").append(path.command);
    env.writer.append(",'").append(path.name).append("',function(").append(parentVar).append(",").append(childStateVar).append(",").append(caseVar).append(") {").tabUp().newline();
    Base.children(env.stateVar(childStateVar).caseVar(caseVar).parentVariable(parentVar));
    env.writer.tabDown().tab().append("});").newline();
    env.pool.give(caseVar);
    env.pool.give(childStateVar);
    env.pool.give(parentVar);
  }

  public void _template() {
    String name = env.element.attr("rx:template");
    String parentVar = env.pool.ask();
    String childStateVar = env.pool.ask();
    env.writer.tab().append("$.UT(").append(eVar).append(",").append(env.stateVar).append(",'").append(name).append("', function(").append(parentVar).append(",").append(childStateVar).append(") {").tabUp().newline();
    Base.children(env.stateVar(childStateVar).parentVariable(parentVar));
    env.writer.tabDown().tab().append("});").newline();
    env.pool.give(childStateVar);
    env.pool.give(parentVar);
  }

  public void _base() {
    for (Attribute attr : env.element.attributes().asList()) {
      if (attr.getKey().equals("xmlns") || attr.getKey().startsWith("rx:")) {
        continue;
      }
      if (attr.hasDeclaredValue()) {
        Tree tree = Parser.parse(attr.getValue());
        Map<String, String> vars = tree.variables();
        if (vars.size() > 0) {
          var oVar = env.pool.ask();
          var computeFoo = env.pool.ask();
          env.writer.tab().append("{").tabUp().newline();
          env.writer.tab().append("var ").append(oVar).append(" = {};").newline();
          env.writer.tab().append(oVar).append(".__dom = ").append(eVar).append(";").newline();
          env.writer.tab().append("var ").append(computeFoo).append(" = (function() {").tabUp().newline();
          writeDomSetter("this.__dom", attr.getKey(), tree.js("this"));
          env.writer.tabDown().tab().append("}).bind(").append(oVar).append(");").newline();
          for (Map.Entry<String, String> ve : vars.entrySet()) {
            StatePath path = StatePath.resolve(ve.getValue(), env.stateVar);
            env.writer.tab().append("$.Y(").append(path.command).append(",").append(oVar).append(",'").append(path.name).append("',").append(computeFoo).append(");").newline();
          }
          env.pool.give(oVar);
          env.pool.give(computeFoo);
          env.writer.tab().append(computeFoo).append("();").newline();
          env.writer.tabDown().tab().append("}").newline();
        } else {
          writeDomSetter(eVar, attr.getKey(), "'" + Escapes.escape39(attr.getValue()) + "'");
        }
      } else {
        writeDomSetter(eVar, attr.getKey(), "true");
      }
    }
  }

  private void writeDomSetter(String var, String key, String expr) {
    boolean hasValue = env.element.tagName().equalsIgnoreCase("textarea") || env.element.tagName().equalsIgnoreCase("input");
    if (hasValue && key.equalsIgnoreCase("value")) {
      env.writer.tab().append(var).append(".value=").append(expr).append(";").newline();
    } else {
      if (key.startsWith("json:")) {
        env.writer.tab().append(var).append(".set_").append(key.substring(5).toLowerCase(Locale.ROOT)).append("(").append(expr).append(");").newline();
      } else {
        env.writer.tab().append(var).append(".setAttribute('").append(key).append("',").append(expr).append(");").newline();
      }
    }
  }

  public void _event(String event) {
    env.pool.ask();
    ArrayList<Command> commands = org.adamalang.rxhtml.acl.Parser.parse(env.element.attr("rx:" + event));
    for (Command command : commands) {
      command.write(env, event, eVar);
    }
  }

  private void walkAndValidateAndWitness(Element element, Function<Element, Boolean> check, HashSet<String> seen) {
    if (element.tagName().equalsIgnoreCase("input")) {
      if (check.apply(element)) {
        seen.add(element.attr("name"));
      }
    } else {
      for (Element child : element.children()) {
        walkAndValidateAndWitness(child, check, seen);
      }
    }
  }

  private boolean walkAndValidateAndCheck(Environment env, Function<Element, Boolean> test, String... checks) {
    HashSet<String> seen = new HashSet<>();
    walkAndValidateAndWitness(env.element, test, seen);
    boolean result = true;
    for (String check : checks) {
      if (!seen.contains(check)) {
        env.feedback.warn(env.element, "Failed to find an input for '" + check + "'");
        result = false;
      }
    }
    return result;
  }

  private void check_action_sign_in() {
    walkAndValidateAndCheck(env, (el) -> {
      String name = el.attr("name");
      String type = el.attr("type");
      if ("password".equals(name)) {
        if (!("password".equals(type))) {
          env.feedback.warn(el, "Passwords should have type 'password'.");
        }
        return true;
      }
      if ("email".equals(name)) {
        if (!("email".equals(type))) {
          env.feedback.warn(el, "Emails should have type 'email'.");
        }
        return true;
      }
      if ("remember".equals(name)) {
        return true;
      }
      if ("submit".equals(type)) {
        return true;
      }
      env.feedback.warn(el, "The input '" + name + "' is excessive.");
      return false;
    }, "email", "password", "remember");
  }

  private void check_action_sign_up() {
    walkAndValidateAndCheck(env, (el) -> {
      if ("email".equals(el.attr("name"))) {
        if (!("email".equals(el.attr("type")))) {
          env.feedback.warn(el, "Emails should have type 'email'");
        }
        return true;
      }
      return false;
    }, "email");
  }

  private void check_set_password() {
    walkAndValidateAndCheck(env, (el) -> {
      String name = el.attr("name");
      if ("password".equals(name)) {
        if (!("password".equals(el.attr("type")))) {
          env.feedback.warn(el, "Passwords should have type 'password'.");
        }
        return true;
      }
      if ("email".equals(name)) {
        if (!("email".equals(el.attr("type")))) {
          env.feedback.warn(el, "Emails should have type 'email'");
        }
        return true;
      }
      return "code".equals(name);
    }, "email", "password", "code");
  }

  public void _action() {
    String action = env.element.attr("rx:action").trim();
    if ("adama:sign-in".equalsIgnoreCase(action)) { // sign in as an Adama user
      check_action_sign_in();
      env.writer.tab().append("$.aSO(").append(eVar) //
          .append(",").append(env.stateVar) //
          .append(",'").append(env.val("rx:identity", "default")) //
          .append("','").append(env.val("rx:failure", "sign_in_failed")) //
          .append("','").append(env.val("rx:forward", "/")) //
          .append("');").newline();
    } else if ("adama:sign-up".equalsIgnoreCase(action)) { // sign up as an Adama user
      check_action_sign_up();
      env.writer.tab().append("$.aSU(").append(eVar) //
          .append(",").append(env.stateVar) //
          .append(",'").append(env.val("rx:failure", "sign_up_failed")) //
          .append("','").append(env.val("rx:forward", "/")) //
          .append("');").newline();
    } else if ("adama:set-password".equalsIgnoreCase(action)) { // set the password for the connection
      check_set_password();
      env.writer.tab().append("$.aSP(").append(eVar) //
          .append(",").append(env.stateVar) //
          .append(",'").append(env.val("rx:failure", "set_password_failed")) //
          .append("','").append(env.val("rx:forward", "/")) //
          .append("'").append(");").newline();
    } else if (action.startsWith("send:")) { // Send a message on the given channel
      String channel = action.substring(5);
      env.writer.tab().append("$.aSD(").append(eVar) //
          .append(",").append(env.stateVar) //
          .append(",'").append(channel) //
          .append("','").append(env.val("rx:failure", "send_failed")) //
          .append("');").newline();
    } else if (action.startsWith("copy:")) { // Copy the form object into the view
      String path = action.substring(5);
      boolean tuned = path.startsWith("view:") | path.startsWith("data:");
      StatePath _path = StatePath.resolve(tuned ? path : ("view:" + path), env.stateVar);
      env.writer.tab().append("$.aCP(").append(eVar).append(",").append(_path.command).append(",'").append(_path.name).append("');").newline();
    }
  }
}
