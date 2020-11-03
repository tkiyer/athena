package org.tkiyer.athena.execution;

public class CancelQueryException extends RuntimeException {

    public CancelQueryException() {
        super();
    }

    public CancelQueryException(String message) {
        super(message);
    }

    public CancelQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelQueryException(Throwable cause) {
        super(cause);
    }
}
