package org.megrez.agent

import java.util.concurrent.Executors

import org.jboss.netty.bootstrap._
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.websocket._
import java.net._

import actors.Actor

object Main {
  def main(args: Array[String]) {
    val serverConnection = new Server(new URI(args.first))
    serverConnection.connect
  }
}

class NotMergezServerException(val uri: URI) extends Exception

class Server(val server: URI) extends ServerHandler {
  val handshakeHandler = new HandshakeHandler(server, this) 
  val holder = new HandlerHolder(handshakeHandler) 

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
    bootstrap.connect(new InetSocketAddress(server.getHost, server.getPort))
  }

  def megrezServerConnected() {
    
  }

  def invalidMegrezServer(uri: URI) {

  }
}