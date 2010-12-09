package org.megrez.server.http.netty.resources

import javax.ws.rs.{Produces, GET, Path}
import java.lang.{String, Class}
import javax.ws.rs.core.{MultivaluedMap, MediaType}
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import java.io.{OutputStreamWriter, OutputStream}
import javax.ws.rs.ext.{Provider, MessageBodyWriter}
import reflect.BeanProperty

@Path("/people/lijian")
class PersonResource {  
  @GET  
  @Produces(Array("text/html"))
  def lijian = Person("lijian")
}

case class Person(@BeanProperty name : String)
