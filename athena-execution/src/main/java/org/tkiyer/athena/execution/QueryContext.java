package org.tkiyer.athena.execution;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkiyer.athena.engine.api.ClientExecutionException;
import org.tkiyer.athena.engine.api.QueryClient;
import org.tkiyer.athena.engine.api.QueryEngine;
import org.tkiyer.athena.engine.api.QueryMetadata;
import org.tkiyer.athena.engine.api.QueryResult;
import org.tkiyer.athena.engine.api.ResultFetchException;
import org.tkiyer.athena.engine.api.security.AthenaUser;

import java.io.IOException;
import java.util.*;

public class QueryContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryContext.class);

    private final Object lock = new Object();

    private final List<QueryEventListener> queryEventListeners = new ArrayList<>(10);

    private final AthenaUser athenaUser;

    private final QueryId queryId;

    private QueryState queryState = QueryState.DEFINE;

    private final QueryMetric queryMetric = new QueryMetric();

    private final QueryMetadata queryMetadata;

    private final QueryEngine queryEngine;

    private QueryClient queryClient;

    private QueryTask queryTask = null;

    private QuerySummary finishedQuerySummary = null;

    private Throwable errorThrowable = null;

    public QueryContext(AthenaUser athenaUser, QueryMetadata queryMetadata, QueryEngine queryEngine, QueryId queryId) {
        this.athenaUser = athenaUser;
        this.queryMetadata = queryMetadata;
        this.queryEngine = queryEngine;
        this.queryId = queryId;
        addQueryEventListener(event -> {
            if (event.getNewQueryState().isDone()) {
                LOGGER.debug(String.format("Query[%s] is done.", this.queryId));
                this.finishedQuerySummary = createSummary();
                this.queryMetric.endAllDurationTime();
            }
        });
    }

    public QuerySummary signal() throws SignalQueryException {
        this.queryMetric.getQueuedTime().end();
        this.queryMetric.getExecutionTime().begin();
        compareAndSetQueryState(QueryState.RUNNING, QueryState.ACCEPTED, QueryState.QUEUED);
        this.queryClient = this.queryEngine.newQueryClient();
        try {
            this.queryClient.execute(this.athenaUser, this.queryMetadata);
        } catch (ClientExecutionException e) {
            throw new SignalQueryException(String.format("Error signal query[%s].", this.queryId), e);
        }
        return ofSummary();
    }

    public void cancel() {
        try {
            if (this.queryTask.cancel()) {
                closeQueryClient();
                // set state
                compareAndSetQueryState(QueryState.CANCELED, QueryState.NONE_TERMINAL_QUERY_STATES.toArray(new QueryState[0]));
            } else {
                throw new CancelQueryException(String.format("Query[%s] cannot be canceled!", this.queryId));
            }
        } catch (Throwable t) {
            fail(t);
        }
    }

    public QueryResult fetchResult() throws ResultFetchException {
        if (this.queryClient.canFetchResult()) {
            return this.queryClient.fetchResult();
        }
        throw new ResultFetchException(String.format("Cannot fetch query[%s] result, current state is %s", this.queryId, this.queryState));
    }

    public QuerySummary ofSummary() {
        return this.queryState.isDone() ? this.finishedQuerySummary : createSummary();
    }

    public void addQueryEventListener(QueryEventListener listener) {
        Objects.requireNonNull(listener, "QueryEventListener is null.");
        synchronized (lock) {
            if (!this.queryState.isDone()) {
                this.queryEventListeners.add(listener);
            }
        }
        // state machine will never transition from a terminal state, so fire state change immediately
        if (this.queryState.isDone()) {
            listener.onStateChanged(new QueryEvent(this.queryMetadata, this.queryState, this.queryState));
        }
    }

    protected void accept(final QueryTask task) {
        this.queryTask = task;
        this.queryMetric.getElapsedTime().begin();
        compareAndSetQueryState(QueryState.ACCEPTED, QueryState.DEFINE);
    }

    protected void queue() {
        this.queryMetric.getQueuedTime().begin();
        compareAndSetQueryState(QueryState.QUEUED, QueryState.ACCEPTED);
    }

    protected void fail(Throwable t) {
        this.errorThrowable = t;
        closeQueryClient();
        compareAndSetQueryState(QueryState.FAILED, QueryState.NONE_TERMINAL_QUERY_STATES.toArray(new QueryState[0]));
    }

    protected void reject() {
        compareAndSetQueryState(QueryState.REJECTED, QueryState.ACCEPTED);
    }

    protected void finish(QuerySummary summary) {
        compareAndSetQueryState(QueryState.FINISHED, QueryState.RUNNING);
    }

    protected void fireStateChangeListener(QueryEvent event, List<QueryEventListener> listeners) {
        checkState(!Thread.holdsLock(lock), "Can not fire state change event while holding the lock");
        Objects.requireNonNull(event, "QueryStateEvent is null.");
        LOGGER.debug(String.format("Fire state change from [%s] to [%s] listeners(%s).", event.getOldQueryState(), event.getNewQueryState(), listeners.size()));
        for (QueryEventListener listener : listeners) {
            try {
                listener.onStateChanged(event);
            } catch (Throwable e) {
                LOGGER.error(String.format("Error notifying state from %s to %s change listener for %s", event.getOldQueryState(), event.getNewQueryState(), event.toString()), e);
            }
        }
    }

    private void closeQueryClient() {
        if (null != this.queryClient) {
            try {
                this.queryClient.close();
            } catch (IOException e) {
                LOGGER.warn("Query client close has some error.", e);
            }
        }
    }

    private QuerySummary createSummary() {
        double progress = 1.00D;
        String executionLog = "";
        if (null != this.queryClient) {
            progress = this.queryClient.progress();
            executionLog = this.queryClient.getExecutionLog();
        }
        return new QuerySummary(this.queryId, this.queryMetadata, this.queryMetric, progress, this.queryState, executionLog, getFailureInfo());
    }

    private String getFailureInfo() {
        return null == this.errorThrowable ? null : ExceptionUtils.getStackTrace(this.errorThrowable);
    }

    private void compareAndSetQueryState(QueryState newQueryState, QueryState... expectedQueryStates) {
        checkState(!Thread.holdsLock(lock), "Can not set state while holding the lock.");
        Objects.requireNonNull(newQueryState, "New query state is null.");
        Objects.requireNonNull(expectedQueryStates, "Expected query state is null.");

        QueryState oldQueryState;
        List<QueryEventListener> fireListeners;
        synchronized (lock) {
            if (this.queryState == newQueryState || this.queryState == QueryState.CANCELED) {
                return;
            }
            boolean containsExpectedState = false;
            for (QueryState checkState : expectedQueryStates) {
                if (checkState == this.queryState) {
                    containsExpectedState = true;
                    break;
                }
            }
            checkState(containsExpectedState, String.format("State is %s must in %s.", this.queryState.toString(), Arrays.toString(expectedQueryStates)));
            checkState(!this.queryState.isDone(), String.format("Query state can not transition from %s to %s", this.queryState, newQueryState));
            oldQueryState = this.queryState;
            this.queryState = newQueryState;
            LOGGER.debug(String.format("Compare current state[%s] expected[%s], set new state[%s]", this.queryState, Arrays.toString(expectedQueryStates), newQueryState));
            // TODO log query summary info when state changed.
            // copy listeners
            fireListeners = Collections.unmodifiableList(new ArrayList<>(this.queryEventListeners));
            // if terminal state, clear listeners.
            if (this.queryState.isDone()) {
                this.queryEventListeners.clear();
            }
            // release lock, notify all.
            lock.notifyAll();
        }
        // fire state changed event listeners.
        fireStateChangeListener(new QueryEvent(this.queryMetadata, oldQueryState, newQueryState), fireListeners);
    }

    private void checkState(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }
}
