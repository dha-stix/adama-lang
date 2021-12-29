/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.cli.commands;

import org.adamalang.cli.Config;
import org.adamalang.cli.Util;
import org.adamalang.lsp.LanguageServer;

public class Code {
    public static void execute(Config config, String[] args) throws Exception {
        if (args.length == 0) {
            codeHelp();
            return;
        }
        String command = Util.normalize(args[0]);
        String[] next = Util.tail(args);
        switch (command) {
            case "lsp":
                lsp(next);
                return;
            case "help":
                codeHelp();
                return;
        }
    }

    public static void lsp(String[] args) throws Exception {
        int port = 2423;
        for (int k = 0; k + 1 < args.length; k++) {
            if ("--port".equals(args[k])) {
                port = Integer.parseInt(args[k+1]);
            }
        }
        LanguageServer.singleThread(port);
    }

    public static void codeHelp() {
        System.out.println(Util.prefix("Local development tools.", Util.ANSI.Green));
        System.out.println("");
        System.out.println(Util.prefix("USAGE:", Util.ANSI.Yellow));
        System.out.println("    " + Util.prefix("adama code", Util.ANSI.Green) + " " + Util.prefix("[CODESUBCOMMAND]", Util.ANSI.Magenta));
        System.out.println("");
        System.out.println(Util.prefix("FLAGS:", Util.ANSI.Yellow));
        System.out.println("    " + Util.prefix("--config", Util.ANSI.Green) + "          Supplies a config file path other than the default (~/.adama)");
        System.out.println("");
        System.out.println(Util.prefix("CODESUBCOMMAND:", Util.ANSI.Yellow));
        System.out.println("    " + Util.prefix("validate-plan", Util.ANSI.Green) + "     Validates a deployment plan (locally) for speed");
        System.out.println("    " + Util.prefix("compile-file", Util.ANSI.Green) + "      Compiles the adama file and shows any problems");
        System.out.println("    " + Util.prefix("lsp", Util.ANSI.Green) + "               Spin up a single threaded language service protocol server");
    }
}