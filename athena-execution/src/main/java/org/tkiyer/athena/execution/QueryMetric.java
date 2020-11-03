package org.tkiyer.athena.execution;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryMetric {

    private QueryDurationTime queuedTime;

    private QueryDurationTime analysisTime;

    private QueryDurationTime executionTime;

    private QueryDurationTime elapsedTime;

    public QueryMetric() {
        this(new QueryDurationTime(), new QueryDurationTime(), new QueryDurationTime(), new QueryDurationTime());
    }

    public QueryMetric(QueryDurationTime queuedTime, QueryDurationTime analysisTime, QueryDurationTime executionTime, QueryDurationTime elapsedTime) {
        this.queuedTime = queuedTime;
        this.analysisTime = analysisTime;
        this.executionTime = executionTime;
        this.elapsedTime = elapsedTime;
    }

    @JsonGetter
    public QueryDurationTime getQueuedTime() {
        return queuedTime;
    }

    @JsonGetter
    public QueryDurationTime getAnalysisTime() {
        return analysisTime;
    }

    @JsonGetter
    public QueryDurationTime getExecutionTime() {
        return executionTime;
    }

    @JsonGetter
    public QueryDurationTime getElapsedTime() {
        return elapsedTime;
    }

    public void endAllDurationTime() {
        this.queuedTime.end();
        this.analysisTime.end();
        this.executionTime.end();
        this.elapsedTime.end();
    }
}
