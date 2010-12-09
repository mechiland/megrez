package org.megrez.server.http.netty.resources

import javax.ws.rs.{Produces, GET, Path}

@Path("/helloworld")
class HelloWorldResource {
  @GET
  @Produces(Array("text/plain"))
  def message = "hello world"  
}