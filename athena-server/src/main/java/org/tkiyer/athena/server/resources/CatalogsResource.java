package org.tkiyer.athena.server.resources;

import com.google.inject.Inject;
import io.dropwizard.jersey.sessions.Session;
import org.tkiyer.artemis.ArtemisUser;
import org.tkiyer.artemis.api.QueryRepository;
import org.tkiyer.artemis.metadata.Catalog;
import org.tkiyer.artemis.utils.WebUtils;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Path("/api/catalogs")
public class CatalogsResource {

    private final QueryRepository repository;

    @Inject
    public CatalogsResource(QueryRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogsForUser(@Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            List<Catalog> catalogs = WebUtils.getCachedCatalogs(session);
            if (catalogs.isEmpty()) {
                catalogs = repository.getCatalogs(user);
                if (null == catalogs || catalogs.isEmpty()) {
                    catalogs = Collections.emptyList();
                } else {
                    WebUtils.cacheCatalogs(catalogs, session);
                }
            }
            return Response.ok(catalogs).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
