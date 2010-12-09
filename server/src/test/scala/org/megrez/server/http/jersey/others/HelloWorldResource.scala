package org.megrez.server.http.jersey.others

import javax.ws.rs.{Produces, GET, Path}

@Path("/helloworld2")
class HelloWorldResource {
  @GET
  @Produces(Array("text/plain"))
  def message = "hello world 2"
}