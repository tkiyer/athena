package org.tkiyer.athena.server.repository;

import org.tkiyer.athena.engine.api.QueryClient;
import org.tkiyer.athena.engine.api.QueryEngine;
import org.tkiyer.athena.engine.api.QueryMetadata;
import org.tkiyer.athena.engine.api.QueryResult;
import org.tkiyer.athena.engine.api.ResultFetchException;
import org.tkiyer.athena.engine.api.metadata.Catalog;
import org.tkiyer.athena.engine.api.metadata.Schema;
import org.tkiyer.athena.engine.api.metadata.Table;
import org.tkiyer.athena.engine.api.security.AthenaUser;
import org.tkiyer.athena.execution.*;
import org.tkiyer.athena.server.config.AthenaConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class QueryRepository {

    private QueryEngineFactory queryEngineFactory;

    private QueryExecutor queryExecutor;

    private QueryIdGenerator queryIdGenerator;

    private ConcurrentHashMap<QueryId, QueryContext> activeQueries = new ConcurrentHashMap<>();

    public QueryRepository(AthenaConfiguration athenaConfiguration) {
        this.queryEngineFactory = new QueryEngineFactory(artemisConfiguration);
        this.queryExecutor = new QueryExecutor(
                athenaConfiguration.getQuery().getThreadPoolCoreSize(),
                athenaConfiguration.getQuery().getThreadPoolMaxSize(),
                athenaConfiguration.getQuery().getQueryQueueSize()
        );
        this.queryIdGenerator = new QueryIdGenerator(
                athenaConfiguration.getWorkerId(),
                athenaConfiguration.getDataCenterId()
        );
    }

    public List<Catalog> getCatalogs(AthenaUser athenaUser) {
        List<Catalog> r = new ArrayList<>();
        for (Engine engine : Engine.values()) {
            QueryEngine queryEngine = queryEngineFactory.getQueryEngine(engine);
            if (queryEngine.isAvailable()) {
                QueryClient client = queryEngine.newQueryClient();
                r.addAll(client.getCatalogs(athenaUser));
            }
        }
        return r;
    }

    public List<Schema> getCatalogSchemasForUser(AthenaUser athenaUser, Catalog catalog) {
        QueryEngine queryEngine = queryEngineFactory.getQueryEngine(catalog.getEngine());
        if (queryEngine.isAvailable()) {
            QueryClient client = queryEngine.newQueryClient();
            return client.getSchemas(artemisUser, catalog);
        }
        return null;
    }

    public List<Table> getCatalogSchemaTablesForUser(AthenaUser athenaUser, Schema schema) {
        QueryEngine queryEngine = queryEngineFactory.getQueryEngine(schema.getCatalog().getEngine());
        if (queryEngine.isAvailable()) {
            QueryClient client = queryEngine.newQueryClient();
            return client.getTables(artemisUser, schema);
        }
        return null;
    }

    public Table getTablePreviewForUser(AthenaUser athenaUser, Table table) {
        QueryEngine queryEngine = queryEngineFactory.getQueryEngine(table.getSchema().getCatalog().getEngine());
        if (queryEngine.isAvailable()) {
            QueryClient client = queryEngine.newQueryClient();
            return client.getTablePreview(artemisUser, table);
        }
        return null;
    }

    public QueryId submitQuery(AthenaUser athenaUser, QueryMetadata queryMetadata) {
        QueryId queryId = this.queryIdGenerator.nextId();
        QueryContext context = new QueryContext(
                artemisUser,
                queryMetadata,
                queryEngineFactory.getQueryEngine(Engine.valueOf(queryMetadata.getEngineName())),
                queryId
        );
        activeQueries.putIfAbsent(queryId, context);
        this.queryExecutor.submit(new QueryTask(context));
        return queryId;
    }

    public QueryId cancelQuery(QueryId queryId) {
        QueryContext context = activeQueries.get(queryId);
        context.cancel();
        activeQueries.remove(queryId);
        return queryId;
    }

    public QuerySummary getQuerySummary(QueryId queryId) {
        return activeQueries.get(queryId).ofSummary();
    }

    public QueryResult getQueryResult(QueryId queryId) throws ResultFetchException {
        return activeQueries.get(queryId).fetchResult();
    }
}
