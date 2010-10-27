package org.megrez.server.http

import org.jboss.netty.handler.codec.http.HttpRequest
import actors.Actor

object Method extends Enumeration {
  type Method = Value
  val GET = Value("GET")
  val PUT = Value("PUT")
  val DELETE = Value("DELETE")
  val POST = Value("POST")
}

import Method._

class Route(val pattern : String, val handler : Actor) {
  def matches(request: HttpRequest): Boolean = request.getUri.matches(pattern)
}

case class HttpRoute(override val pattern: String, val methods: Set[Method], override val handler: Actor) extends Route(pattern, handler) {
  override def matches(request: HttpRequest): Boolean = super.matches(request) && (matchedMethod(request) != None)

  def matchedMethod(request: HttpRequest): Method = {
    methods.find(_.toString.equals(request.getMethod().getName())).get
  }
}

object Route {  
  class Path(val pattern: String, val methods: Set[Method]) {
    def ->(handler: Actor) = HttpRoute(pattern, methods, handler)
  }

  def path(path: String) = new Path(path, Set(GET, PUT, DELETE, POST))

  def get(path: String) = new Path(path, Set(GET))

  def post(path: String) = new Path(path, Set(POST))

  def put(path: String) = new Path(path, Set(PUT))

  def delete(path: String) = new Path(path, Set(DELETE))
}