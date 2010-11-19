package org.megrez.server.http

import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http.websocket._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponse._
import actors.Actor
import java.net.{URI, InetSocketAddress}

class WebSocketClient(val server: URI, val test : Actor) extends SimpleChannelUpstreamHandler {
  val bootstrap = new ClientBootstrap(
    new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpResponseDecoder())
      pipeline.addLast("encoder", new HttpRequestEncoder())
      pipeline.addLast("ws-handler", WebSocketClient.this)
      pipeline
    }
  })

  val future = bootstrap.connect(new InetSocketAddress("localhost", 8080))

  private var channel: Channel = _

  override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    context.getChannel.write(handshakeRequest).addListener(new ChannelFutureListener() {
      override def operationComplete(future: ChannelFuture) {
        context.getPipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder())
      }
    })    
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case response: org.jboss.netty.handler.codec.http.HttpResponse => handleWebSocketHandshake(context, response, event.getChannel)
      case frame: WebSocketFrame => handleMegrezHandshake(context, frame, event.getChannel)
    }
  }


  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause.printStackTrace
  }

  private def handleWebSocketHandshake(context: ChannelHandlerContext, response: org.jboss.netty.handler.codec.http.HttpResponse, channel: Channel) {
    val validStatus = response.getStatus.getCode == 101 && response.getStatus.getReasonPhrase == "Web Socket Protocol Handshake"
    val validHeaders = response.getHeader(Names.UPGRADE) == WEBSOCKET && response.getHeader(CONNECTION) == Values.UPGRADE
    if (!validStatus || !validHeaders) throw new Exception()
    channel.getPipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder())
    channel.write(new DefaultWebSocketFrame("megrez-agent:1.0"))
    this.channel = channel
  }

  private def handleMegrezHandshake(context: ChannelHandlerContext, response: WebSocketFrame, channel: Channel) {
    if (response.getTextData != "megrez-server:1.0") test ! (this, response.getTextData)
  }

  private def handshakeRequest = {
    val request = new DefaultHttpRequest(HTTP_1_1, GET, server.getPath)
    request.addHeader(Names.UPGRADE, WEBSOCKET)
    request.addHeader(CONNECTION, Values.UPGRADE)
    request.addHeader(HOST, if (server.getPort == 80) server.getHost else server.getHost + ":" + server.getPort)
    request.addHeader(ORIGIN, "http://" + server.getHost)
    request
  }

  def shutdown {
    future.addListener(ChannelFutureListener.CLOSE)
  }
}

