package org.megrez.server.rest.resources

import javax.ws.rs.{PathParam, Produces, GET, Path}

@Path("pipelines")
class PipelineResource {

  @GET
  @Path("{name}")
  @Produces(Array("text/html"))
  def show(@PathParam("name") name: String) = name
}
