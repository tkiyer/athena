package org.tkiyer.athena.execution;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryExecutor {

    private final QueryQueue queryQueue;

    private final int threadPoolCoreSize;

    private final int threadPoolMaxSize;

    private final int queryQueueSize;

    private ThreadPoolExecutor threadPoolExecutor;

    private QueryTaskThreadFactory threadFactory;

    private QueryTaskRejectExecutionHandler rejectExecutionHandler;

    public QueryExecutor(int threadPoolCoreSize, int threadPoolMaxSize, int queryQueueSize) {
        this.threadPoolCoreSize = threadPoolCoreSize;
        this.threadPoolMaxSize = threadPoolMaxSize;
        this.queryQueueSize = queryQueueSize;
        this.queryQueue = new QueryQueue(queryQueueSize);
        this.threadFactory = new QueryTaskThreadFactory();
        this.rejectExecutionHandler = new QueryTaskRejectExecutionHandler();
        this.threadPoolExecutor = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize, 1L, TimeUnit.MINUTES, this.queryQueue, this.threadFactory, this.rejectExecutionHandler);
    }

    public void submit(QueryTask queryTask) {
        queryTask.accept();
        threadPoolExecutor.execute(queryTask);
    }

    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    @JsonGetter
    public int getCurrentQueryQueueSize() {
        return this.queryQueue.size();
    }

    @JsonGetter
    public int getCurrentCreatedThreadCount() {
        return this.threadFactory.threadCount.get();
    }

    @JsonGetter
    public int getCurrentRejectedQueryTaskCount() {
        return this.rejectExecutionHandler.rejectedTaskCount.get();
    }

    @JsonGetter
    public int getThreadPoolCoreSize() {
        return threadPoolCoreSize;
    }

    @JsonGetter
    public int getThreadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    @JsonGetter
    public int getQueryQueueSize() {
        return queryQueueSize;
    }

    private static class QueryTaskThreadFactory implements ThreadFactory {

        private ThreadGroup threadGroup;

        private final AtomicInteger threadCount = new AtomicInteger(0);

        private QueryTaskThreadFactory() {
            super();
            this.threadGroup = new ThreadGroup("ARTEMIS-QUERY");
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.threadGroup, r, nextThreadName());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

        private String nextThreadName() {
            return "ARTEMIS-QUERY-RUN-" + this.threadCount.incrementAndGet();
        }
    }

    private static class QueryTaskRejectExecutionHandler implements RejectedExecutionHandler {

        private final AtomicInteger rejectedTaskCount = new AtomicInteger(0);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            this.rejectedTaskCount.incrementAndGet();
            if (r instanceof QueryTask) {
                ((QueryTask) r).reject();
            }
        }
    }
}
