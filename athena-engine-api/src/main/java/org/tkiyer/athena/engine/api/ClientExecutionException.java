package org.tkiyer.athena.engine.api;

public class ClientExecutionException extends Exception {

    public ClientExecutionException() {
        super();
    }

    public ClientExecutionException(String message) {
        super(message);
    }

    public ClientExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientExecutionException(Throwable cause) {
        super(cause);
    }
}
