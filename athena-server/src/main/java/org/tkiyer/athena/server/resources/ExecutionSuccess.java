package org.tkiyer.athena.server.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.tkiyer.artemis.execution.QueryId;

public class ExecutionSuccess {

    @JsonProperty
    public final QueryId queryId;

    public ExecutionSuccess(QueryId queryId) {
        this.queryId = queryId;
    }

    public QueryId getQueryId() {
        return queryId;
    }
}
