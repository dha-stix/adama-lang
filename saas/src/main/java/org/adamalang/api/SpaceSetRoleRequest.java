package org.adamalang.api;

import org.adamalang.runtime.contracts.Callback;
import org.adamalang.runtime.exceptions.ErrorCodeException;
import org.adamalang.transforms.results.AuthenticatedUser;
import org.adamalang.transforms.results.SpacePolicy;
import org.adamalang.web.io.*;

/**  */
public class SpaceSetRoleRequest {
  public final String identity;
  public final AuthenticatedUser who;
  public final String space;
  public final SpacePolicy policy;
  public final String email;
  public final Integer userId;
  public final String role;

  public SpaceSetRoleRequest(final String identity, final AuthenticatedUser who, final String space, final SpacePolicy policy, final String email, final Integer userId, final String role) {
    this.identity = identity;
    this.who = who;
    this.space = space;
    this.policy = policy;
    this.email = email;
    this.userId = userId;
    this.role = role;
  }

  public static void resolve(ConnectionNexus nexus, JsonRequest request, Callback<SpaceSetRoleRequest> callback) {
    try {
      final BulkLatch<SpaceSetRoleRequest> _latch = new BulkLatch<>(nexus.executor, 3, callback);
      final String identity = request.getString("identity", true, 458759);
      final LatchRefCallback<AuthenticatedUser> who = new LatchRefCallback<>(_latch);
      final String space = request.getString("space", true, 461828);
      final LatchRefCallback<SpacePolicy> policy = new LatchRefCallback<>(_latch);
      final String email = request.getString("email", true, 473103);
      final LatchRefCallback<Integer> userId = new LatchRefCallback<>(_latch);
      final String role = request.getString("role", true, 456716);
      _latch.with(() -> new SpaceSetRoleRequest(identity, who.get(), space, policy.get(), email, userId.get(), role));
      nexus.identityService.execute(identity, who);
      nexus.spaceService.execute(space, policy);
      nexus.emailService.execute(email, userId);
    } catch (ErrorCodeException ece) {
      nexus.executor.execute(() -> {
        callback.failure(ece);
      });
    }
  }
}