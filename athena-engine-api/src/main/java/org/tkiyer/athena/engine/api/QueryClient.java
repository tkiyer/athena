package org.tkiyer.athena.engine.api;

import org.tkiyer.athena.engine.api.metadata.Catalog;
import org.tkiyer.athena.engine.api.metadata.Schema;
import org.tkiyer.athena.engine.api.metadata.Table;
import org.tkiyer.athena.engine.api.security.AthenaUser;

import java.io.Closeable;
import java.util.List;

public interface QueryClient extends Closeable {

    String getExecutionLog();

    void execute(AthenaUser athenaUser, QueryMetadata queryMetadata) throws ClientExecutionException;

    double progress();

    boolean canFetchResult();

    QueryResult fetchResult() throws ResultFetchException;

    List<Catalog> getCatalogs(AthenaUser athenaUser);

    List<Schema> getSchemas(AthenaUser athenaUser, Catalog catalog);

    List<Table> getTables(AthenaUser athenaUser, Schema schema);

    Table getTablePreview(AthenaUser athenaUser, Table table);
}
