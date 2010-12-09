package org.megrez.server.http

import actors.Actor
import org.jboss.netty.channel.{Channel, ChannelUpstreamHandler}

trait Route   

case class ResourcePackage(packageName : String) extends Route
case class WebSocket(path: String, factory:(Channel , Actor) => ChannelUpstreamHandler, actor : Actor) extends Route

object Route {
  def resources(packageName : String) = ResourcePackage(packageName)
  def websocket(path: String, factory:(Channel , Actor) => ChannelUpstreamHandler, actor : Actor) = WebSocket(path, factory, actor)
}