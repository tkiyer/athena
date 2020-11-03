package org.tkiyer.athena.server.resources;

import com.google.inject.Inject;
import io.dropwizard.jersey.sessions.Session;
import org.tkiyer.artemis.ArtemisUser;
import org.tkiyer.artemis.api.QueryRepository;
import org.tkiyer.artemis.metadata.Catalog;
import org.tkiyer.artemis.metadata.Schema;
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

@Path("/api/catalogs/{catalog}/schemas")
public class SchemasResource {

    private final QueryRepository repository;

    @Inject
    public SchemasResource(QueryRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogSchemasForUser(@PathParam("catalog") String catalog,
                                             @Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            Catalog catalogWrapper = WebUtils.wrapCatalog(catalog, session);
            List<Schema> schemas = catalogWrapper.getSchemas();
            if (null == schemas || schemas.isEmpty()) {
                schemas = repository.getCatalogSchemasForUser(user, catalogWrapper);
                if (null == schemas || schemas.isEmpty()) {
                    schemas = Collections.emptyList();
                } else {
                    WebUtils.cacheSchemas(catalogWrapper, schemas, session);
                }
            }
            return Response.ok(schemas).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

}
