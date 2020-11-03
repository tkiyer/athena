package org.tkiyer.athena.server.support;

import org.tkiyer.athena.engine.api.QueryEngine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class QueryEngineManager {

    private final ConcurrentMap<String, QueryEngine> queryEngines = new ConcurrentHashMap<>(5);

    public synchronized void addQueryEngine(QueryEngine queryEngine, Supplier<ClassLoader> duplicateQueryEngineClassLoaderFactory) {
        requireNonNull(queryEngine, "queryEngine is null");
        requireNonNull(duplicateQueryEngineClassLoaderFactory, "duplicateQueryEngineClassLoaderFactory is null");
        QueryEngine existingQueryEngine = queryEngines.putIfAbsent(queryEngine.getName(), queryEngine);
        checkArgument(existingQueryEngine == null, "QueryEngine '%s' is already registered", queryEngine.getName());
    }
}
