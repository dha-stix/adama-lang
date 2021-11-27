/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE'
 * which is in the root directory of the repository. This file is part of the 'Adama'
 * project which is a programming language and document store for board games.
 * 
 * See http://www.adama-lang.org/ for more information.
 * 
 * (c) 2020 - 2021 by Jeffrey M. Barber (http://jeffrey.io)
*/
package org.adamalang.runtime.sys;

import org.adamalang.runtime.ErrorCodes;
import org.adamalang.runtime.contracts.*;
import org.adamalang.runtime.exceptions.ErrorCodeException;
import org.adamalang.runtime.json.JsonStreamReader;
import org.adamalang.runtime.json.JsonStreamWriter;
import org.adamalang.runtime.json.PrivateView;
import org.adamalang.runtime.natives.NtAsset;
import org.adamalang.runtime.natives.NtClient;
import org.adamalang.translator.jvm.LivingDocumentFactory;

import java.util.ArrayDeque;

/** A LivingDocument tied to a document id and DataService */
public class DurableLivingDocument {

  private static class IngestRequest {
    private final String request;
    private final Callback<Integer> callback;

    private IngestRequest(String request, Callback<Integer> callback) {
      this.request = request;
      this.callback = callback;
    }
  }

  private LivingDocumentFactory currentFactory;
  public final DocumentThreadBase base;
  public final Key key;
  private LivingDocument document;
  private Integer requiresInvalidateMilliseconds;
  private final ArrayDeque<IngestRequest> pending;
  private boolean inflightPatch;
  private boolean catastrophicFailureOccurred;

  private DurableLivingDocument(final Key key, final LivingDocument document, final LivingDocumentFactory currentFactory, final DocumentThreadBase base) {
    this.key = key;
    this.document = document;
    this.currentFactory = currentFactory;
    this.base = base;
    this.requiresInvalidateMilliseconds = null;
    this.pending = new ArrayDeque<>(2);
    this.inflightPatch = false;
    this.catastrophicFailureOccurred = false;
  }

  public LivingDocument document() {
    return document;
  }

  public Integer getAndCleanRequiresInvalidateMilliseconds() {
    Integer result = requiresInvalidateMilliseconds;
    requiresInvalidateMilliseconds = null;
    return result;
  }

  public static void fresh(
          final Key key,
          final LivingDocumentFactory factory,
          final NtClient who,
          final String arg,
          final String entropy,
          final DocumentMonitor monitor,
          final DocumentThreadBase base,
          final Callback<DurableLivingDocument> callback) {
    try {
      DurableLivingDocument document = new DurableLivingDocument(key, factory.create(monitor), factory, base);
      document.construct(who, arg, entropy, Callback.transform(callback, ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_FRESH_TRANSFORM, (seq) -> document));
    } catch (Throwable ex) {
      callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_FRESH_DRIVE, ex));
    }
  }

  public static void load(
          final Key key,
          final LivingDocumentFactory factory,
          final DocumentMonitor monitor,
          final DocumentThreadBase base,
          final Callback<DurableLivingDocument> callback) {
    try {
      LivingDocument doc = factory.create(monitor);
      base.service.get(key, Callback.transform(callback, ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_PARSE, (data) -> {
          JsonStreamReader reader = new JsonStreamReader(data.patch);
          reader.ingestDedupe(doc.__get_intern_strings());
          doc.__insert(reader);
          JsonStreamWriter writer = new JsonStreamWriter();
          doc.__dump(writer);
          return new DurableLivingDocument(key, doc, factory, base);
      }));
    } catch (Throwable ex) {
      callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_LOAD, ex));
    }
  }

  public LivingDocumentFactory getCurrentFactory() {
    return currentFactory;
  }

  public void deploy(LivingDocumentFactory factory, Callback<Integer> callback) throws ErrorCodeException {
    LivingDocument newDocument = factory.create(document.__monitor);
    JsonStreamWriter writer = new JsonStreamWriter();
    document.__dump(writer);
    newDocument.__insert(new JsonStreamReader(writer.toString()));
    document.__usurp(newDocument);
    document = newDocument;
    currentFactory = factory;
    invalidate(callback);
  }

  public JsonStreamWriter forge(final String command, final NtClient who) {
    final var writer = new JsonStreamWriter();
    writer.beginObject();
    writer.writeObjectFieldIntro("command");
    writer.writeFastString(command);
    writer.writeObjectFieldIntro("timestamp");
    writer.writeLong(base.time.nowMilliseconds());
    if (who != null) {
      writer.writeObjectFieldIntro("who");
      writer.writeNtClient(who);
    }
    return writer;
  }

  private void finishSuccessDataServicePatch(Integer whenToInvalidateMilliseconds, Callback<Integer> callback, int seq) {
    this.requiresInvalidateMilliseconds = whenToInvalidateMilliseconds;
    this.inflightPatch = false;

    if (pending.size() == 0) {
      if (requiresInvalidateMilliseconds != null && requiresInvalidateMilliseconds == 0) {
        requiresInvalidateMilliseconds = null;
        invalidate(callback);
      } else {
        callback.success(seq);
        if (requiresInvalidateMilliseconds != null) {
          base.executor.schedule(key, () -> {
            invalidate(Callback.DONT_CARE_INTEGER);
          }, requiresInvalidateMilliseconds);
        }
      }
    } else {
      callback.success(seq);
      executeNow(pending.removeFirst());
    }
  }

  private void catastrophicFailure() {
    document.__nukeViews();
    base.map.remove(key);
    catastrophicFailureOccurred = true;
    while (pending.size() > 0) {
      pending.removeFirst().callback.failure(new ErrorCodeException(ErrorCodes.E5_CATASTROPHIC_DOCUMENT_FAILURE_EXCEPTION));
    }
  }

  private void executeNow(IngestRequest request) {
    try {
      final var change = document.__transact(request.request);
      inflightPatch = true;

      if (change.update.requiresFutureInvalidation) {
        this.requiresInvalidateMilliseconds = change.update.whenToInvalidateMilliseconds;
      }
      base.service.patch(key, change.update, new Callback<>() {
        @Override
        public void success(Void value) {
          base.executor.execute(() -> {
            change.complete();
            finishSuccessDataServicePatch(change.update.requiresFutureInvalidation ? change.update.whenToInvalidateMilliseconds : null, request.callback, change.update.seq);
          });
        }

        @Override
        public void failure(ErrorCodeException ex) {
          base.executor.execute(() -> {
            request.callback.failure(ex);
            catastrophicFailure();
          });
        }
      });
    } catch (ErrorCodeException ex) {
      request.callback.failure(ex);
    }
  }

  private void queueAndExecuteUnderLock(IngestRequest request) {
    if (catastrophicFailureOccurred) {
      request.callback.failure(new ErrorCodeException(ErrorCodes.E5_CATASTROPHIC_DOCUMENT_FAILURE_EXCEPTION));
      return;
    }
    if (inflightPatch) {
      pending.add(request);
    } else {
      executeNow(request);
    }
  }

  private void ingest(String request, Callback<Integer> callback) {
    try {
      queueAndExecuteUnderLock(new IngestRequest(request, callback));
    } catch (Throwable ex) {
      callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_INGEST_DRIVE, ex));
    }
  }

  private void construct(final NtClient who, final String arg, final String entropy, Callback<Integer> callback) {
    try {
      final var writer = forge("construct", who);
      writer.writeObjectFieldIntro("arg");
      writer.injectJson(arg);
      if (entropy != null) {
        writer.writeObjectFieldIntro("entropy");
        writer.writeFastString(entropy);
      }
      writer.endObject();
      final var change = document.__transact(writer.toString());
      base.service.initialize(key, change.update, Callback.handoff(callback, ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_CONSTRUCT_DONE, () -> {
        change.complete();
        invalidate(callback);
      }));
    } catch (Throwable ex) {
      callback.failure(ErrorCodeException.detectOrWrap(ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_CONSTRUCT_DRIVE, ex));
    }
  }

  public void invalidate(Callback<Integer> callback) {
    final var request = forge("invalidate", null);
    request.endObject();
    ingest(request.toString(), callback);
  }

  public int getCodeCost() {
    return document.__getCodeCost();
  }

  public void bill(Callback<Integer> callback) {
    final var request = forge("bill", null);
    request.endObject();
    ingest(request.toString(), callback);
  }

  public void expire(long limit, Callback<Integer> callback) {
    final var request = forge("expire", null);
    request.writeObjectFieldIntro("limit");
    request.writeLong(limit);
    request.endObject();
    ingest(request.toString(), callback);
  }

  public void connect(final NtClient who, Callback<Integer> callback) {
    final var request = forge("connect", who);
    request.endObject();
    ingest(request.toString(), callback);
  }

  public boolean isConnected(final NtClient who) {
    return document.__isConnected(who);
  }

  public  void createPrivateView(final NtClient who, final Perspective perspective, Callback<PrivateView> callback) {
    PrivateView result = document.__createView(who, perspective);
    invalidate(Callback.transform(callback, ErrorCodes.E1_DURABLE_LIVING_DOCUMENT_STAGE_ATTACH_PRIVATE_VIEW, (seq) -> result));
  }

  public int garbageCollectPrivateViewsFor(final NtClient who) {
    return document.__garbageCollectViews(who);
  }

  public void scheduleCleanup() {
    base.executor.schedule(key, () -> {
      if (document.__canRemoveFromMemory()) {
        base.map.remove(key);
      }
    }, base.getMillisecondsForCleanupCheck());
  }

  public void disconnect(final NtClient who, Callback<Integer> callback) {
    final var request = forge("disconnect", who);
    request.endObject();
    ingest(request.toString(), callback);
    if (document.__canRemoveFromMemory()) {
      scheduleCleanup();
    }
  }

  public void send(final NtClient who, final String marker, final String channel, final String message, Callback<Integer> callback) {
    final var writer = forge("send", who);
    writer.writeObjectFieldIntro("channel");
    writer.writeFastString(channel);
    if (marker != null) {
      writer.writeObjectFieldIntro("marker");
      writer.writeString(marker);
    }
    writer.writeObjectFieldIntro("message");
    writer.injectJson(message);
    writer.endObject();
    ingest(writer.toString(), callback);
  }

  public void apply(NtClient who, String patch, Callback<Integer> callback) {
    final var writer = forge("apply", who);
    writer.writeObjectFieldIntro("patch");
    writer.injectJson(patch);
    writer.endObject();
    ingest(writer.toString(), callback);
  }

  public boolean canAttach(NtClient who) {
    return document.__onCanAssetAttached(who);
  }

  public void attach(NtClient who, NtAsset asset, Callback<Integer> callback) {
    final var writer = forge("attach", who);
    writer.writeObjectFieldIntro("asset");
    writer.writeNtAsset(asset);
    writer.endObject();
    ingest(writer.toString(), callback);
  }

  public String json() {
    final var writer = new JsonStreamWriter();
    document.__dump(writer);
    return writer.toString();
  }

  public void postLoadReconcile() {
    for (NtClient client : document.__reconcileClientsToForceDisconnect()) {
      disconnect(client, Callback.DONT_CARE_INTEGER);
    }
  }

  public static final Callback<DurableLivingDocument> INVALIDATE_ON_SUCCESS = new Callback<DurableLivingDocument>() {
    @Override
    public void success(DurableLivingDocument value) {
      value.invalidate(Callback.DONT_CARE_INTEGER);
    }

    @Override
    public void failure(ErrorCodeException ex) {
    }
  };
}