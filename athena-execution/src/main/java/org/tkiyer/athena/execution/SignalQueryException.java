package org.tkiyer.athena.execution;

public class SignalQueryException extends Exception {

    public SignalQueryException() {
        super();
    }

    public SignalQueryException(String message) {
        super(message);
    }

    public SignalQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignalQueryException(Throwable cause) {
        super(cause);
    }

    protected SignalQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
