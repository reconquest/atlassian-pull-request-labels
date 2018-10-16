package io.reconquest.bitbucket.rest;

//import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/pull")
public class PullRequestLabels {
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response list()
    {
       return Response.ok(new PullRequestLabelsModel("list of somehting")).build();
    }

    //@GET
    //@Produces({MediaType.APPLICATION_JSON})
    //public Response getPullRequests()
    //{
    //   return Response.ok(new PullRequestLabelsModel("pull requests a b c")).build();
    //}
}
