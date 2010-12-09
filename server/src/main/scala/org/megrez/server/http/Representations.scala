package org.megrez.server.http

import collection.mutable.HashMap
import java.lang.{String, Class}
import javax.ws.rs.core.{MultivaluedMap, MediaType}
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import io.Source
import org.antlr.stringtemplate.{AutoIndentWriter, StringTemplate}
import java.io.{OutputStreamWriter, OutputStream, File}
import javax.ws.rs.ext.{Provider, MessageBodyWriter}
import javax.ws.rs.Produces

@Provider
@Produces(Array("*/*"))
class Representations extends MessageBodyWriter[Any] {
  def writeTo(resource: Any, resourceClass: Class[_], p3: Type, p4: Array[Annotation], p5: MediaType, p6: MultivaluedMap[String, AnyRef], stream: OutputStream) {    
    val template = Representations.getTemplate(resource, resourceClass)
    val writer = new OutputStreamWriter(stream)
    template.write(new AutoIndentWriter(writer))
    writer.flush
  }

  def getSize(p1: Any, p2: Class[_], p3: Type, p4: Array[Annotation], p5: MediaType) = -1L

  def isWriteable(resourceClass: Class[_], genericType: Type, annotations: Array[Annotation], mediaType: MediaType) = Representations.has(resourceClass)  
}

object Representations {
  private val map = new HashMap[Class[_],File]()

  def register[T](dir : File)(implicit manifest: Manifest[T]) {
      map.put(manifest.erasure, dir)
  }

  def has(c : Class[_]) = {
    map.contains(c)
  }

  def getTemplate(resource: Any, resourceClass: Class[_]) = {    
    val template = new StringTemplate(Source.fromFile(new File(map(resourceClass), "GET.html")).mkString)
    template.setAttribute("it", resource)
    template
  }

}