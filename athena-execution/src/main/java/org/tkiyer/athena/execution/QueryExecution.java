package org.tkiyer.athena.execution;

import java.util.concurrent.Callable;

public class QueryExecution implements Callable<QuerySummary> {

    private final QueryContext context;

    public QueryExecution(QueryContext context) {
        this.context = context;
    }

    @Override
    public QuerySummary call() throws Exception {
        return context.signal();
    }
}
