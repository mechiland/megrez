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

class Route(val pattern: String, val handler: Actor) {
  def matches(request: HttpRequest): Boolean = request.getUri.matches(pattern)
}

case class HttpRoute(override val pattern: String, val methods: Set[Method], override val handler: Actor) extends Route(pattern, handler) {
  override def matches(request: HttpRequest): Boolean = super.matches(request) && (matchedMethod(request) != None)

  def matchedMethod(request: HttpRequest): Method = {
    methods.find(_.toString.equals(request.getMethod().getName())).get
  }
}

case class WebSocketRoute(override val pattern: String, override val handler: Actor) extends Route(pattern, handler)

object Route {
  class Http(val pattern: String, val methods: Set[Method]) {
    def ->(handler: Actor) = HttpRoute(pattern, methods, handler)
  }

  class WebSocket(val pattern: String) {
    def ->(handler: Actor) = WebSocketRoute(pattern, handler)
  }

  def path(path: String) = new Http(path, Set(GET, PUT, DELETE, POST))

  def get(path: String) = new Http(path, Set(GET))

  def post(path: String) = new Http(path, Set(POST))

  def put(path: String) = new Http(path, Set(PUT))

  def delete(path: String) = new Http(path, Set(DELETE))

  def websocket(path: String) = new WebSocket(path)
}