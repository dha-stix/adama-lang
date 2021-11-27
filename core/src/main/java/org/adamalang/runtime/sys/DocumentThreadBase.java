package org.adamalang.runtime.sys;

import org.adamalang.runtime.contracts.*;

import java.util.HashMap;

/** This defines the state required within a thread to run a document. As Documents run in isolated
 * thread without synchronization, access to a durable living document must be access via this base.
 */
public class DocumentThreadBase {
    public final DataService service;
    public final SimpleExecutor executor;
    public final HashMap<Key, DurableLivingDocument> map;
    public final TimeSource time;
    private int millisecondsForCleanupCheck;
    private int millisecondsAfterLoadForReconciliation;

    public DocumentThreadBase(DataService service, SimpleExecutor executor, TimeSource time) {
        this.service = service;
        this.executor = executor;
        this.time = time;
        this.map = new HashMap<>();
        this.millisecondsForCleanupCheck = 2500;
        this.millisecondsAfterLoadForReconciliation = 2500;
    }

    public int getMillisecondsForCleanupCheck() {
        return millisecondsForCleanupCheck;
    }

    public void setMillisecondsForCleanupCheck(int ms) {
        this.millisecondsForCleanupCheck = ms;
    }

    public int getMillisecondsAfterLoadForReconciliation() {
        return millisecondsAfterLoadForReconciliation;
    }

    public void setMillisecondsAfterLoadForReconciliation(int ms) {
        this.millisecondsAfterLoadForReconciliation = ms;
    }
}