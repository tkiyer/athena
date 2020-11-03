package org.tkiyer.athena.engine.api;

public class EngineNotFoundException extends RuntimeException {

    public EngineNotFoundException() {
        super();
    }

    public EngineNotFoundException(String message) {
        super(message);
    }

    public EngineNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineNotFoundException(Throwable cause) {
        super(cause);
    }
}
