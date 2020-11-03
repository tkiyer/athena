package org.tkiyer.athena.execution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.tkiyer.athena.engine.api.QueryMetadata;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuerySummary {

    @JsonProperty
    private final String queryId;

    @JsonProperty
    private final String groupId;

    @JsonProperty
    private final QueryState state;

    @JsonProperty
    private final QueryMetadata metadata;

    @JsonProperty
    private final QueryMetric metric;

    @JsonProperty
    private final double progress;

    @JsonProperty
    private final String executionLog;

    @JsonProperty
    private final String failureInfo;

    public QuerySummary(QueryId queryId, QueryMetadata metadata, QueryMetric queryMetric, double progress, QueryState state, String executionLog, String failureInfo) {
        this.queryId = queryId.toString();
        this.groupId = metadata.getGroupId();
        this.metadata = metadata;
        this.progress = progress;
        this.metric = queryMetric;
        this.state = state;
        this.executionLog = executionLog;
        this.failureInfo = failureInfo;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getGroupId() {
        return groupId;
    }

    public QueryState getState() {
        return state;
    }

    public double getProgress() {
        return progress;
    }

    public QueryMetadata getMetadata() {
        return metadata;
    }

    public QueryMetric getMetric() {
        return metric;
    }

    public String getExecutionLog() {
        return executionLog;
    }

    public String getFailureInfo() {
        return failureInfo;
    }
}
