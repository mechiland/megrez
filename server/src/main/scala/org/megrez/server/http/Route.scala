package org.megrez.server.http

import org.jboss.netty.handler.codec.http.HttpRequest
import actors.Actor

abstract class Route(val handler : Actor) {
  def isMatch(request: HttpRequest): Boolean 

  def matchedMethod(request: HttpRequest): Method
}

case class Connect(val pattern: String, val methods: Set[Method], override val handler: Actor) extends Route(handler) {
  override def isMatch(request: HttpRequest): Boolean = {
    (request.getUri.matches(pattern)) && (matchedMethod(request) != None)
  }

  override def matchedMethod(request: HttpRequest): Method = {
    methods.find(_.toString.equals(request.getMethod().getName())).get
  }
}

object Route {
  class Path(val pattern: String, val methods: Set[Method]) {
    def ->(handler: Actor) = Connect(pattern, methods, handler)
  }

  def path(path: String) = new Path(path, Set(GET, PUT, DELETE, POST))

  def get(path: String) = new Path(path, Set(GET))

  def post(path: String) = new Path(path, Set(POST))

  def put(path: String) = new Path(path, Set(PUT))

  def delete(path: String) = new Path(path, Set(DELETE))
}