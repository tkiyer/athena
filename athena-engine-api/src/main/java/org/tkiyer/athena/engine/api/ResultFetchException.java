package org.tkiyer.athena.engine.api;

public class ResultFetchException extends Exception {

    public ResultFetchException() {
        super();
    }

    public ResultFetchException(String message) {
        super(message);
    }

    public ResultFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultFetchException(Throwable cause) {
        super(cause);
    }
}
