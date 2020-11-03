package org.tkiyer.athena.server.resources;

import com.google.inject.Inject;
import io.dropwizard.jersey.sessions.Session;
import org.tkiyer.artemis.ArtemisUser;
import org.tkiyer.artemis.api.QueryRepository;
import org.tkiyer.artemis.api.QueryResult;
import org.tkiyer.artemis.api.ResultFetchException;
import org.tkiyer.artemis.execution.QueryId;
import org.tkiyer.artemis.execution.QuerySummary;
import org.tkiyer.artemis.utils.WebUtils;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/queries")
public class QueriesResource {

    private final QueryRepository repository;

    @Inject
    public QueriesResource(QueryRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueriesForUser(@Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @DELETE
    @Path("{queryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelQuery(@PathParam("queryId") String queryId,
                                @Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            QueryId cancelQueryId = repository.cancelQuery(QueryId.of(queryId));
            return Response.ok(new ExecutionSuccess(cancelQueryId)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{queryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuerySummary(@PathParam("queryId") String queryId,
                                    @Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            QuerySummary summary = repository.getQuerySummary(QueryId.of(queryId));
            return Response.ok(summary).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{queryId}/result")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueryResult(@PathParam("queryId") String queryId,
                                   @Session HttpSession session) {
        ArtemisUser user = WebUtils.getLoginUser(session);
        if (null != user) {
            try {
                QueryResult result = repository.getQueryResult(QueryId.of(queryId));
                return Response.ok(result).build();
            } catch (ResultFetchException e) {
                return Response.serverError().entity(e).build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
