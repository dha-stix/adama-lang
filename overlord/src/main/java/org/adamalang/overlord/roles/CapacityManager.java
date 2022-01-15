/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.overlord.roles;

import org.adamalang.common.NamedRunnable;
import org.adamalang.common.SimpleExecutor;
import org.adamalang.gossip.Engine;
import org.adamalang.grpc.client.Client;
import org.adamalang.mysql.DataBase;
import org.adamalang.mysql.deployments.Deployments;
import org.adamalang.mysql.frontend.Spaces;
import org.adamalang.overlord.OverlordMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.TreeSet;

public class CapacityManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(CapacityManager.class);

  private static class CoreCapacityManagementTask extends NamedRunnable {
    private final SimpleExecutor executor;
    private final OverlordMetrics metrics;
    private final Client client;
    private final DataBase deploymentsDatabase;
    private final DataBase frontendDatabase;

    public CoreCapacityManagementTask(SimpleExecutor executor, OverlordMetrics metrics, Client client, DataBase deploymentsDatabase, DataBase frontendDatabase) {
      super("capacity-management");
      this.executor = executor;
      this.metrics = metrics;
      this.client = client;
      this.deploymentsDatabase = deploymentsDatabase;
      this.frontendDatabase = frontendDatabase;
    }

    private void simpleCapacityCheck(String space, boolean retryAvailable) {
      try {
        StringBuilder sb = new StringBuilder();
        int targets = 0;
        boolean append = false;
        ArrayList<Deployments.Deployment> deployments = Deployments.listTargetsOnSpace(deploymentsDatabase, space);
        Spaces.InternalDeploymentPlan plan = Spaces.getPlanByNameForInternalDeployment(frontendDatabase, space);
        for (Deployments.Deployment deployment : deployments) {
          if (append) {
            sb.append(", ");
          }
          append = true;
          sb.append(deployment.target);
          if (!deployment.hash.equals(plan.hash)) {
            metrics.capacity_monitor_found_inconsistent_deployment.run();
            sb.append(" [FIXED]");
            System.out.println("deploy:" + space + " to " + deployment.target);
            Deployments.deploy(deploymentsDatabase, space, deployment.target, plan.hash, plan.plan);
            metrics.capacity_monitor_fixed_inconsistent_deployment.run();
            client.notifyDeployment(deployment.target, space);
          }
          targets++;
        }
        if (targets < 3) {
          metrics.capacity_monitor_found_weak_space.run();
          sb.append(" [WEAK]");
          client.getDeploymentTargets(space, (target) -> {
            try {
              System.out.println("deploy:" + space + " to " + target);
              Deployments.deploy(deploymentsDatabase, space, target, plan.hash, plan.plan);
              client.notifyDeployment(target, space);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          });
        }
        // TODO: figure out how to manage heat to expand/retract capacity
        System.out.println("Capacity for: " + space + " = " + sb);
      } catch (Exception ex) {
        metrics.capacity_monitor_failed_space.run();
        if (retryAvailable) {
          executor.schedule(
              new NamedRunnable("inspecting-again", space) {
                @Override
                public void execute() throws Exception {
                  metrics.capacity_monitor_retry_space.run();
                  simpleCapacityCheck(space, false);
                }
              },
              2000);
        }
      }
    }

    @Override
    public void execute() throws Exception {
      int delay = 2000;
      try {
        metrics.capacity_monitor_sweep.run();
        for (final String spaceName : Spaces.listAllSpaceNames(frontendDatabase)) {
          metrics.capacity_monitor_queue_space.run();
          executor.schedule(new NamedRunnable("inspecting", spaceName) {
            String spaceToCheck = spaceName;
            @Override
            public void execute() throws Exception {
              metrics.capacity_monitor_dequeue_space.run();
              simpleCapacityCheck(spaceToCheck, true);
            }
          }, delay);
          delay += 2000;
        }
        metrics.capacity_monitor_sweep_finished.run();
      } catch (Exception ex) {
        metrics.capacity_monitor_sweep_failed.run();
      } finally{
        executor.schedule(this, 1000 * 60 * 10 + delay);
      }
    }
  }

  public static void kickOff(OverlordMetrics metrics, Client client, DataBase deploymentsDatabase, DataBase frontendDatabase) {
    SimpleExecutor executor = SimpleExecutor.create("capacity-management");
    CoreCapacityManagementTask task = new CoreCapacityManagementTask(executor, metrics, client, deploymentsDatabase, frontendDatabase);
    executor.schedule(task, 1000);
  }
}