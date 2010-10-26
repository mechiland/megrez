package org.megrez.agent

import java.util.concurrent.Executors

import org.jboss.netty.bootstrap._
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.handler.codec.http._
import java.net._
import actors.Actor


class Server(val server: URI, val reconnectAfter : Long, val worker : Actor) extends ServerHandler {
  val handshakeHandler = new HandshakeHandler(server, this)
  val agentHandler = new AgentHandler(this, worker)  
  val holder = new HandlerHolder(handshakeHandler)

  private var channel : ChannelFuture = _

  val bootstrap = new ClientBootstrap(
    new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpResponseDecoder())
      pipeline.addLast("encoder", new HttpRequestEncoder())
      pipeline.addLast("ws-handler", holder)
      pipeline
    }
  })

  def connect() {
    channel = bootstrap.connect(new InetSocketAddress(server.getHost, server.getPort))
  }

  override def connected() {    
    holder.handler = agentHandler 
  }

  override def disconnected() {
    holder.handler = handshakeHandler
    Thread.sleep(reconnectAfter)
    connect
  }

  override def invalidServer(uri: URI) {    
  }

  def shutdown() {
    channel.addListener(ChannelFutureListener.CLOSE)
    bootstrap.releaseExternalResources
  }
}

trait ServerHandler {
  def connected() {}
  def disconnected() {}
  def invalidServer(uri: URI) {}
}