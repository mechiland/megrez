package org.megrez.server.http

trait Route {
  def isMatch(request : org.jboss.netty.handler.codec.http.HttpRequest) : Boolean
  def getHandler : Any
  def matchedMethod(request : org.jboss.netty.handler.codec.http.HttpRequest) : Method
}

case class Connect(val pattern : Any, val methods : Set[Method], val handler : Any) extends Route {
  override def isMatch(request : org.jboss.netty.handler.codec.http.HttpRequest) : Boolean = {
    (pattern equals request.getUri) && (matchedMethod(request) != None)
  }

  override def matchedMethod(request : org.jboss.netty.handler.codec.http.HttpRequest) : Method = {
	methods.find(_.toString.equals(request.getMethod().getName())).get
  }

  override def getHandler = handler 
}

object Route {
  class Path(val pattern : Any, val methods : Set[Method]) {
    def ->(handler : Any) = Connect(pattern, methods, handler)
  }

  def path(path : String) = new Path(path, Set(GET, PUT, DELETE, POST))

  def get(path : String) = new Path(path, Set(GET))

  def post(path: String) = new Path(path, Set(POST))

  def put(path: String) = new Path(path, Set(PUT))

  def delete(path: String) = new Path(path, Set(DELETE))
}