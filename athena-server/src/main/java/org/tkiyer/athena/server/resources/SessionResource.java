package org.tkiyer.athena.server.resources;

import org.tkiyer.artemis.freemarker.LoginView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

@Path("/")
public class SessionResource
{

    @GET
    public Response redirectToApp()
    {
        return Response.temporaryRedirect(URI.create("/app"))
                .status(Response.Status.MOVED_PERMANENTLY)
                .build();
    }

    @GET
    @Path("/login")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public LoginView getLogin()
    {
        return new LoginView();
    }

    @POST
    @Path("/login")
    public void doLogin(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @FormParam("username") String username,
            @FormParam("password") String password)
            throws IOException
    {
        response.sendRedirect(response.encodeRedirectURL("/app"));
    }

    @GET
    @Path("/postLogin")
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    public Response getLoginNoRemember()
    {
        return Response.temporaryRedirect(URI.create("/app")).cookie(new NewCookie("rememberMe", null)).build();
    }
}
