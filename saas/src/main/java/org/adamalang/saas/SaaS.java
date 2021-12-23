package org.adamalang.saas;

import org.adamalang.extern.Email;
import org.adamalang.extern.ExternNexus;
import org.adamalang.mysql.Base;
import org.adamalang.mysql.BaseConfig;
import org.adamalang.mysql.frontend.ManagementInstaller;

public class SaaS {
    public static void main(String[] args) throws Exception {
        // TODO: search for a default config
        // TODO: search args for --config to pick up a file to use for config
        String config = "{}";



        if (args.length == 0) {
            System.err.println("requires args");
            System.exit(1);
        }
        if ("frontend".equals(args[0])) {
            Base base = new Base(new BaseConfig(config));
            new ManagementInstaller(base).install();

            ExternNexus nexus = new ExternNexus(new Email() {
                @Override
                public void sendCode(String email, String code) {
                    System.err.println("send code:" + code + " to " + email);
                }
            }, base);

            Frontend.execute(nexus, config);
        }
    }
}