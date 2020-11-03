package org.tkiyer.athena.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class QueryConfiguration {

    @JsonProperty
    @NotNull
    private int threadPoolCoreSize = 20;

    @JsonProperty
    @NotNull
    private int threadPoolMaxSize = 200;

    @JsonProperty
    @NotNull
    private int queryQueueSize = 10000;

    public int getThreadPoolCoreSize() {
        return threadPoolCoreSize;
    }

    public void setThreadPoolCoreSize(int threadPoolCoreSize) {
        this.threadPoolCoreSize = threadPoolCoreSize;
    }

    public int getThreadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    public void setThreadPoolMaxSize(int threadPoolMaxSize) {
        this.threadPoolMaxSize = threadPoolMaxSize;
    }

    public int getQueryQueueSize() {
        return queryQueueSize;
    }

    public void setQueryQueueSize(int queryQueueSize) {
        this.queryQueueSize = queryQueueSize;
    }
}
