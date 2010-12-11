package org.megrez.server.rest.resources

import javax.ws.rs.{PathParam, Produces, GET, Path}
import javax.ws.rs.core.Context
import org.megrez.server.core.PipelineManager

@Path("pipelines")
class PipelineResource {

  @GET
  @Path("{name}")
  @Produces(Array("text/html"))
  def show(@PathParam("name") name: String, @Context pipelineManager: PipelineManager) = pipelineManager.addPipeline(name).toString
}
