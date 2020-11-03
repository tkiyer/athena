package org.tkiyer.athena.server.resources;

import com.google.inject.Inject;
import io.dropwizard.jersey.sessions.Session;
import org.tkiyer.artemis.ArtemisUser;
import org.tkiyer.artemis.api.QueryRepository;
import org.tkiyer.artemis.metadata.Schema;
import org.tkiyer.artemis.metadata.Table;
import org.tkiyer.artemis.utils.WebUtils;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Path("/api/catalogs/{catalog}/schemas/{schema}/tables")
public class TablesResource {

    private final QueryRepository repository;

    @Inject
    public TablesResource(QueryRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogSchemaTablesForUser(@PathParam("catalog") String catalog,
                                                  @PathParam("schema") String schema,
                                                  @Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            Schema schemaWrapper = WebUtils.wrapSchema(catalog, schema, session);
            List<Table> tables = schemaWrapper.getTables();
            if (null == tables || tables.isEmpty()) {
                tables = repository.getCatalogSchemaTablesForUser(user, schemaWrapper);
                if (null == tables || tables.isEmpty()) {
                    tables = Collections.emptyList();
                } else {
                    WebUtils.cacheTables(schemaWrapper, tables, session);
                }
            }
            return Response.ok(tables).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{table}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTablePreviewForUser(@PathParam("catalog") String catalog,
                                           @PathParam("schema") String schema,
                                           @PathParam("table") String table,
                                           @Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            Table tableWrapper = WebUtils.wrapTable(catalog, schema, table, session);
            if (null == tableWrapper.getColumns() || tableWrapper.getColumns().isEmpty()) {
                Table t = repository.getTablePreviewForUser(user, tableWrapper);
                if (null == t.getColumns() || t.getColumns().isEmpty()) {
                    t.setColumns(Collections.emptyList());
                } else {
                    WebUtils.cacheTable(t, session);
                }
                return Response.ok(t).build();
            } else {
                return Response.ok(tableWrapper).build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
