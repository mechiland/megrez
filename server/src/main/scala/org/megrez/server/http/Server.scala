package org.megrez.server.http

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http.{HttpResponseDecoder, HttpRequestEncoder}
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame
import actors.Actor

class Server(val routes : Route*) extends SimpleChannelUpstreamHandler {
  private val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(newCachedThreadPool(), newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpResponseDecoder())
      pipeline.addLast("encoder", new HttpRequestEncoder())
      pipeline.addLast("handler", Server.this)
      pipeline
    }
  })
  
  override def messageReceived(context : ChannelHandlerContext, event : MessageEvent) {
    event.getMessage match {
      case request : org.jboss.netty.handler.codec.http.HttpRequest =>
        routes.find(_ isMatch request) match {
          case Some(route : Route) =>
            route.handler ! Request(route matchedMethod request)
          case None =>
        }
      case frame   : WebSocketFrame =>
    }
  }
}



