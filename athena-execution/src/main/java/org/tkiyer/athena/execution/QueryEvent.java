package org.tkiyer.athena.execution;

import org.tkiyer.athena.engine.api.QueryMetadata;

import java.io.Serializable;

public class QueryEvent implements Serializable {

    private final QueryMetadata queryMetadata;

    private final QueryState oldQueryState;

    private final QueryState newQueryState;

    public QueryEvent(QueryMetadata queryMetadata, QueryState oldQueryState, QueryState newQueryState) {
        this.queryMetadata = queryMetadata;
        this.oldQueryState = oldQueryState;
        this.newQueryState = newQueryState;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public QueryState getOldQueryState() {
        return oldQueryState;
    }

    public QueryState getNewQueryState() {
        return newQueryState;
    }
}
