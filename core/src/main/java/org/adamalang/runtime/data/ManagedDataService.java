package org.adamalang.runtime.data;

import org.adamalang.common.Callback;
import org.adamalang.common.ErrorCodeException;
import org.adamalang.common.SimpleExecutor;
import org.adamalang.runtime.data.managed.Action;
import org.adamalang.runtime.data.managed.Base;

public class ManagedDataService implements DataService {
  private final Base base;

  public ManagedDataService(FinderService finder, ArchivingDataService data, String region, String target) {
    this.base = new Base(finder, data, region, target, SimpleExecutor.create("managed-data-service"));
  }

  @Override
  public void initialize(Key key, RemoteDocumentUpdate patch, Callback<Void> callback) {
    base.finder.bind(key, base.region, base.target, new Callback<Void>() {
      @Override
      public void success(Void value) {
        base.on(key, (machine) -> {
          machine.open();
          machine.write(new Action(() -> {
            base.data.initialize(key, patch, callback);
          }, callback));
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        callback.failure(ex);
      }
    });
  }

  @Override
  public void get(Key key, Callback<LocalDocumentChange> callback) {
    base.on(key, (machine) -> {
      machine.open();
      machine.read(new Action(() -> {
        base.data.get(key, callback);
      }, callback));
    });
  }

  @Override
  public void close(Key key, Callback<Void> callback) {
    base.on(key, (machine) -> {
      machine.close();
    });
  }

  @Override
  public void patch(Key key, RemoteDocumentUpdate[] patches, Callback<Void> callback) {
    base.on(key, (machine) -> {
      machine.write(new Action(() -> {
        base.data.patch(key, patches, callback);
      }, callback));
    });
  }

  @Override
  public void compute(Key key, ComputeMethod method, int seq, Callback<LocalDocumentChange> callback) {
    base.on(key, (machine) -> {
      machine.read(new Action(() -> {
        base.data.compute(key, method, seq, callback);
      }, callback));
    });
  }

  @Override
  public void delete(Key key, Callback<Void> callback) {
    base.finder.delete(key, base.target, new Callback<Void>() {
      @Override
      public void success(Void value) {
        base.on(key, (machine) -> {
          machine.write(new Action(() -> {
            base.data.delete(key, callback);
          }, callback));
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        // worse case situation: we deleted in the finder, lost the id, and then leaked data on the disk
        // TODO: write a delete queue to disk, so we can rectify the situation in failure modes
        callback.failure(ex);
      }
    });
  }

  @Override
  public void snapshot(Key key, int seq, String snapshot, int history, Callback<Integer> callback) {
    base.on(key, (machine) -> {
      machine.write(new Action(() -> {
        base.data.snapshot(key, seq, snapshot, history, callback);
      }, callback));
    });
  }
}
