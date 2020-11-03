package org.tkiyer.athena.engine.api;

public interface QueryEngine {

    String getName();

    boolean isAvailable();

    QueryClient newQueryClient();
}
