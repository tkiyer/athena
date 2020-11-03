package org.tkiyer.athena.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class AthenaConfiguration extends Configuration {

    @JsonProperty
    private long workerId = 11L;

    @JsonProperty
    private long dataCenterId = 20L;

    private SecurityConfiguration security;

    private QueryConfiguration query;

    private QueryEngineConfiguration queryEngine;

    public SecurityConfiguration getSecurity() {
        return security;
    }

    public void setSecurity(SecurityConfiguration security) {
        this.security = security;
    }

    public QueryConfiguration getQuery() {
        return query;
    }

    public void setQuery(QueryConfiguration query) {
        this.query = query;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public QueryEngineConfiguration getQueryEngine() {
        return queryEngine;
    }

    public void setQueryEngine(QueryEngineConfiguration queryEngine) {
        this.queryEngine = queryEngine;
    }
}
