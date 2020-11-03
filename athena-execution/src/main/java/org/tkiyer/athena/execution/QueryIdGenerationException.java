package org.tkiyer.athena.execution;

public class QueryIdGenerationException extends RuntimeException {

    public QueryIdGenerationException() {
        super();
    }

    public QueryIdGenerationException(String message) {
        super(message);
    }

    public QueryIdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryIdGenerationException(Throwable cause) {
        super(cause);
    }

    protected QueryIdGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
