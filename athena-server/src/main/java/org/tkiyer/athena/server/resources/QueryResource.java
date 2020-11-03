package org.tkiyer.athena.server.resources;

import com.google.inject.Inject;
import io.dropwizard.jersey.sessions.Session;
import org.tkiyer.artemis.ArtemisUser;
import org.tkiyer.artemis.api.QueryRepository;
import org.tkiyer.artemis.execution.QueryId;
import org.tkiyer.artemis.execution.QueryMetadata;
import org.tkiyer.artemis.utils.WebUtils;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/api/query")
public class QueryResource {

    private final QueryRepository repository;

    @Inject
    public QueryResource(QueryRepository repository) {
        this.repository = repository;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executeQuery(QueryMetadata metadata, @Session HttpSession session) throws IOException {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            QueryId queryId = repository.submitQuery(user, metadata);
            return Response.ok(new org.tkiyer.artemis.resources.ExecutionSuccess(queryId)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
