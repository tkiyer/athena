package org.tkiyer.athena.execution;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueryQueue extends LinkedBlockingQueue<Runnable> {

    public QueryQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(Runnable r, long timeout, TimeUnit unit) throws InterruptedException {
        boolean isOffered = super.offer(r, timeout, unit);
        if (isOffered) {
            if (r instanceof QueryTask) {
                ((QueryTask) r).onQueue();
            }
        }
        return isOffered;
    }

    @Override
    public boolean offer(Runnable r) {
        boolean isOffered = super.offer(r);
        if (isOffered) {
            if (r instanceof QueryTask) {
                ((QueryTask) r).onQueue();
            }
        }
        return isOffered;
    }
}
