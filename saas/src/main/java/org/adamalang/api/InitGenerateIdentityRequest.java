package org.adamalang.api;

import org.adamalang.runtime.contracts.Callback;
import org.adamalang.runtime.exceptions.ErrorCodeException;
import org.adamalang.web.io.*;

/** When the developer receives an email, this method is invoked to complete the hand-shake.
  * 
  * The server will generate a key-pair and send the secret to the client to stash within their config, and the public key will be stored to validate future requests made by this developer machine.
  * 
  * A public key will be held onto for 30 days. */
public class InitGenerateIdentityRequest {
  public final Long connection;
  public final String code;

  public InitGenerateIdentityRequest(final Long connection, final String code) {
    this.connection = connection;
    this.code = code;
  }

  public static void resolve(ConnectionNexus nexus, JsonRequest request, Callback<InitGenerateIdentityRequest> callback) {
    try {
      final Long connection = request.getLong("connection", true, 405505);
      final String code = request.getString("code", true, 455681);
      nexus.executor.execute(() -> {
        callback.success(new InitGenerateIdentityRequest(connection, code));
      });
    } catch (ErrorCodeException ece) {
      nexus.executor.execute(() -> {
        callback.failure(ece);
      });
    }
  }
}