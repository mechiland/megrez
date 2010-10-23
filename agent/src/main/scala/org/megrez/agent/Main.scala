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
    val serverConnection = new ServerConnection(new URI(args.first))
    serverConnection.connect
  }
}

abstract trait WorkerController {
  def serverConnected()
}

class ServerConnection(val server: URI) extends SimpleChannelUpstreamHandler with WorkerController {
  private val bootstrap = new ClientBootstrap(
    new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpResponseDecoder())
      pipeline.addLast("encoder", new HttpRequestEncoder())
      pipeline.addLast("ws-handler", ServerConnection.this)
      pipeline
    }
  })

  override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    event.getChannel.write(handshakeRequest(server))
    context.getPipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder())
  }

  override def channelDisconnected(context : ChannelHandlerContext, event : ChannelStateEvent) {
    println(event)
  }  

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case response: HttpResponse => handleHandshakeResponse(context, response, event.getChannel)
      case frame: WebSocketFrame =>
    }
  }

  override def exceptionCaught(context : ChannelHandlerContext, event: ExceptionEvent) {    
  }
  
  private def handshakeRequest(server: URI) = {
    val request = new DefaultHttpRequest(HTTP_1_1, GET, server.getPath)
    request.addHeader(Names.UPGRADE, WEBSOCKET)
    request.addHeader(CONNECTION, Values.UPGRADE)
    request.addHeader(HOST, if (server.getPort == 80) server.getHost else server.getHost + ":" + server.getPort)
    request.addHeader(ORIGIN, "http://" + server.getHost)    
    request
  }

  private def handleHandshakeResponse(context: ChannelHandlerContext, response: HttpResponse, channel: Channel) {
    val validStatus = response.getStatus.getCode == 101 && response.getStatus.getReasonPhrase == "Web Socket Protocol Handshake"
    val validHeaders = response.getHeader(Names.UPGRADE) == WEBSOCKET && response.getHeader(CONNECTION) == Values.UPGRADE
    if (!validStatus || !validHeaders) throw new Exception()
    context.getPipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder())
    serverConnected
  }

  def connect() {
    bootstrap.connect(new InetSocketAddress(server.getHost, server.getPort))
  }

  def serverConnected() {    
  }
}