package org.adamalang.runtime.sys;

import org.junit.Assert;
import org.junit.Test;

public class PredictiveInventoryTests {
  @Test
  public void flow() {
    PredictiveInventory inventory = new PredictiveInventory();
    inventory.grow();
    PredictiveInventory.PreciseSnapshotAccumulator snapshot = new PredictiveInventory.PreciseSnapshotAccumulator();
    snapshot.count = 1;
    snapshot.memory = 1000;
    snapshot.ticks = 10000;
    inventory.accurate(snapshot);
    inventory.grow();
    inventory.message();
    inventory.message();
    inventory.message();
    PredictiveInventory.Billing billing = inventory.toBill();
    Assert.assertEquals(2, billing.count);
    Assert.assertEquals(2000, billing.memory);
    Assert.assertEquals(20000, billing.cpu);
    Assert.assertEquals(3, billing.messages);
    snapshot.count = 2;
    snapshot.memory = 2000;
    snapshot.ticks = 20000;
    inventory.accurate(snapshot);
    inventory.grow();
    inventory.grow();
    inventory.message();
    inventory.message();
    billing = inventory.toBill();
    Assert.assertEquals(4, billing.count);
    Assert.assertEquals(4000, billing.memory);
    Assert.assertEquals(40000, billing.cpu);
    Assert.assertEquals(2, billing.messages);
    snapshot.count = 10;
    snapshot.memory = 5000;
    snapshot.ticks = 60000;
    inventory.accurate(snapshot);
    inventory.grow();
    inventory.grow();
    inventory.message();
    inventory.message();
    inventory.message();
    inventory.message();
    inventory.message();
    billing = inventory.toBill();
    Assert.assertEquals(12, billing.count);
    Assert.assertEquals(6230, billing.memory);
    Assert.assertEquals(73846, billing.cpu);
    Assert.assertEquals(5, billing.messages);
  }

  @Test
  public void add() {
    PredictiveInventory.Billing a = new PredictiveInventory.Billing(100, 1000, 5, 100);
    PredictiveInventory.Billing b = new PredictiveInventory.Billing(1100, 21000, 51, 1100);
    PredictiveInventory.Billing s = PredictiveInventory.Billing.add(a, b);
    Assert.assertEquals(1200, s.memory);
    Assert.assertEquals(22000, s.cpu);
    Assert.assertEquals(56, s.count);
    Assert.assertEquals(1200, s.messages);
  }
}