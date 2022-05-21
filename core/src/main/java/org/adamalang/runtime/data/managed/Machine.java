package org.adamalang.runtime.data.managed;

import org.adamalang.ErrorCodes;
import org.adamalang.common.Callback;
import org.adamalang.common.ErrorCodeException;
import org.adamalang.common.NamedRunnable;
import org.adamalang.runtime.data.FinderService;
import org.adamalang.runtime.data.Key;

import java.util.ArrayList;

public class Machine {
  private final Key key;
  private final Base base;
  private State state;
  private ArrayList<Action> actions;
  private boolean closed;
  private int pendingWrites;
  private Runnable cancelArchive;
  private int writesInFlight;

  public Machine(Key key, Base base) {
    this.key = key;
    this.base = base;
    this.state = State.Unknown;
    this.actions = null;
    this.closed = false;
    this.pendingWrites = 0;
    this.cancelArchive = null;
    this.writesInFlight = 0;
  }

  private void queue(Action action) {
    if (actions == null) {
      actions = new ArrayList<>();
    }
    actions.add(action);
  }

  private void failQueueWhileInExecutor(ErrorCodeException ex) {
    if (actions != null) {
      ArrayList<Action> tokill = actions;
      actions = null;
      for (Action action : tokill) {
        action.callback.failure(ex);
      }
    }
  }

  private void archive_Success() {
    base.executor.execute(new NamedRunnable("machine-archive-success") {
      @Override
      public void execute() throws Exception {
        cancelArchive = null;
        pendingWrites -= writesInFlight;
        writesInFlight = 0;
        if (pendingWrites > 0) {
          scheduleArchiveWhileInExecutor();
        }
      }
    });
  }

  private void archive_Failure(Exception ex) {
    base.executor.execute(new NamedRunnable("machine-archive-failure") {
      @Override
      public void execute() throws Exception {
        cancelArchive = null;
        scheduleArchiveWhileInExecutor();
      }
    });
  }

  private void archiveWhileInExecutor() {
    base.data.backup(key, new Callback<String>() {
      @Override
      public void success(String newArchiveKey) {
        base.finder.backup(key, newArchiveKey, base.target, new Callback<Void>() {
          @Override
          public void success(Void value) {
            archive_Success();
          }

          @Override
          public void failure(ErrorCodeException ex) {
            archive_Failure(ex);
          }
        });
      }

      @Override
      public void failure(ErrorCodeException ex) {
        archive_Failure(ex);
      }
    });
  }

  private void scheduleArchiveWhileInExecutor() {
    if (cancelArchive == null) {
      writesInFlight = pendingWrites;
      cancelArchive = base.executor.schedule(new NamedRunnable("machine-archive") {
        @Override
        public void execute() throws Exception {
          archiveWhileInExecutor();
        }
      }, 5 * 60 * 1000); // MAGIC
    }
  }

  private void find_FoundMachine(String foundMachine) {
    base.executor.execute(new NamedRunnable("machine-found-machine") {
      @Override
      public void execute() throws Exception {
        if (foundMachine.equals(base.target)) {
          state = State.OnMachine;
          ArrayList<Action> toact = actions;
          actions = null;
          for (Action action : toact) {
            action.action.run();
          }
          if (pendingWrites > 0) {
            scheduleArchiveWhileInExecutor();
          }
        } else {
          failQueueWhileInExecutor(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_WRONG_MACHINE));
        }
      }
    });
  }

  private void restore_Failed(ErrorCodeException ex) {
    base.executor.execute(new NamedRunnable("machine-restoring-failed") {
      @Override
      public void execute() throws Exception {
        state = State.Unknown;
        failQueueWhileInExecutor(ex);
      }
    });
  }

  private void find_Restore(String archiveKey) {
    base.executor.execute(new NamedRunnable("machine-found-archive") {
      @Override
      public void execute() throws Exception {
        state = State.Restoring;
        base.data.restore(key, archiveKey, new Callback<Void>() {
          @Override
          public void success(Void value) {
            base.finder.bind(key, base.region, base.target, new Callback<Void>() {
              @Override
              public void success(Void value) {
                find_FoundMachine(base.target);
              }

              @Override
              public void failure(ErrorCodeException ex) {
                /* note; this requires that restoration be idempotent as the index may fail to bind and multiple restorations may occur */
                restore_Failed(ex);
              }
            });
          }

          @Override
          public void failure(ErrorCodeException ex) {
            restore_Failed(ex);
          }
        });
      }
    });
  }

  private void find() {
    state = State.Finding;
    base.finder.find(key, new Callback<>() {
      @Override
      public void success(FinderService.Result found) {
        if (found.location == FinderService.Location.Machine) {
          find_FoundMachine(found.machine);
        } else {
          find_Restore(found.archiveKey);
        }
      }

      @Override
      public void failure(ErrorCodeException ex) {
        base.executor.execute(new NamedRunnable("machine-find-failure") {
          @Override
          public void execute() throws Exception {
            failQueueWhileInExecutor(ex);
          }
        });
      }
    });
  }

  public void open() {
    closed = false;
  }

  public void write(Action action) {
    if (closed) {
      action.callback.failure(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_WRITE_FAILED_CLOSED));
      return;
    }
    pendingWrites++;
    switch (state) {
      case Unknown:
        find();
      case Finding:
      case Restoring:
        queue(action);
        return;
      case OnMachine:
        action.action.run();
        scheduleArchiveWhileInExecutor();
        return;
    }
  }

  public void read(Action action) {
    if (closed) {
      action.callback.failure(new ErrorCodeException(ErrorCodes.MANAGED_STORAGE_READ_FAILED_CLOSED));
      return;
    }
    switch (state) {
      case Unknown:
        find();
      case Finding:
      case Restoring:
        queue(action);
        return;
      case OnMachine:
        action.action.run();
        return;
    }
  }

  public void close() {
    closed = true;
  }
}
