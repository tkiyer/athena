package org.tkiyer.athena.execution;

import java.util.concurrent.FutureTask;

public class QueryTask extends FutureTask<QuerySummary> {

    private final QueryContext context;

    public QueryTask(QueryContext context) {
        super(new QueryExecution(context));
        this.context = context;
    }

    @Override
    protected void set(QuerySummary querySummary) {
        super.set(querySummary);
        context.finish(querySummary);
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        context.fail(t);
    }

    protected void accept() {
        context.accept(this);
    }

    protected void onQueue() {
        context.queue();
    }

    protected void reject() {
        context.reject();
    }

    protected boolean cancel() {
        return cancel(true);
    }
}
