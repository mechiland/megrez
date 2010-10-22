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
import java.net.{URI, InetSocketAddress, URL}

object Main {
  def main(args: Array[String]) {
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()))
    val server = new URI(args.first)
    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      override def getPipeline() = {
        val pipeline = Channels.pipeline()
        pipeline.addLast("decoder", new HttpResponseDecoder())
        pipeline.addLast("encoder", new HttpRequestEncoder())
        pipeline.addLast("ws-handler", new WebSocketClientHandler(server))
        pipeline
      }
    })

    bootstrap.connect(new InetSocketAddress(server.getHost(), server.getPort()))
  }

  class WebSocketClientHandler(val server: URI) extends SimpleChannelUpstreamHandler {
    private var worker : Worker = _
    
    override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
      event.getChannel.write(handshakeRequest(server))
      context.getPipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder())
    }

    override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {      
      event.getMessage match {
        case response: HttpResponse => handleHandshakeResponse(context, response, event.getChannel)
        case frame: WebSocketFrame =>
      }
    }

    private def handshakeRequest(server: URI) = {
      val request = new DefaultHttpRequest(HTTP_1_1, GET, server.getPath)
      request.addHeader(Names.UPGRADE, WEBSOCKET)
      request.addHeader(CONNECTION, Values.UPGRADE)
      request.addHeader(HOST,  if (server.getPort == 80) server.getHost else server.getHost + ":" + server.getPort)
      request.addHeader(ORIGIN, "http://" + server.getHost)
      request
    }

    private def handleHandshakeResponse(context: ChannelHandlerContext, response: HttpResponse, channel : Channel) {
      val validStatus = response.getStatus.getCode == 101 && response.getStatus.getReasonPhrase == "Web Socket Protocol Handshake"
      val validHeaders = response.getHeader(Names.UPGRADE) == WEBSOCKET && response.getHeader(CONNECTION) == Values.UPGRADE
      if (!validStatus || !validHeaders) throw new Exception()
      context.getPipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder())
      worker = new Worker()
      worker.start
    }    
  }
}