package org.megrez.agent

import java.net.URL
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


object Main {
  def main(args: Array[String]) {
    val bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()))
  }

  object PipelineFactory extends ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline();
      pipeline.addLast("decoder", new HttpResponseDecoder());
      pipeline.addLast("encoder", new HttpRequestEncoder());
//      pipeline.addLast("ws-handler", WebSocketClientHandler);
      pipeline
    }
  }

  class WebSocketClientHandler(val url: URL) extends SimpleChannelUpstreamHandler {
    private var channel: Channel = _    

    override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
      channel = event.getChannel
      channel.write(handshakeRequest(url))
      context.getPipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder())
    }

    override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
      event.getMessage match {
        case response: HttpResponse => handleHandshakeResponse(context, response)
        case frame: WebSocketFrame =>
      }
    }

    private def handshakeRequest(url: URL): DefaultHttpRequest = {
      val request = new DefaultHttpRequest(HTTP_1_1, GET, url.getPath)
      request.addHeader(Names.UPGRADE, WEBSOCKET)
      request.addHeader(CONNECTION, Values.UPGRADE)
      request.addHeader(HOST, url.getHost)
      request.addHeader(ORIGIN, "http://" + url.getHost)
      request
    }

    private def handleHandshakeResponse(context: ChannelHandlerContext, response: HttpResponse) {
      val validStatus = response.getStatus.getCode == 101 && response.getStatus.getReasonPhrase == "Web Socket Protocol Handshake"
      val validHeaders = response.getHeader(Names.UPGRADE) == WEBSOCKET && response.getHeader(CONNECTION) == Values.UPGRADE
      if (!validStatus || !validHeaders) throw new Exception()
      context.getPipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder())
    }
  }
}