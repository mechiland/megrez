package org.megrez.server.http

trait Route {
  def isMatch(request : String) : Boolean
  def getHandler : Any
}
case class Connect(val pattern : Any, val handler : Any) extends Route {
  override def isMatch(request : String) : Boolean = {
    true
  }

  override def getHandler = handler 
}

object Route {
  class Path(val pattern : Any, val methods : Set[Method]) {
    def ->(handler : Any) = Connect(pattern, handler)
  }

  def path(path : String) = new Path(path, Set(GET, PUT, DELETE, POST))

  def get(path : String) = new Path(path, Set(GET))
//  def path(path : String, methods : Method*) = new Path(path, Set(methods))
}