package org.megrez.server.http.netty

trait Route

case class ResourcePackage(packageName : String) extends Route

object Route {
  def resources(packageName : String) = ResourcePackage(packageName)
}