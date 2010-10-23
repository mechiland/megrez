package org.megrez.agent

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap._
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.websocket._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpVersion._

abstract trait WebSocketServer {
  def handleHttpRequest(context: ChannelHandlerContext, http: HttpRequest)
}

abstract class TestServer extends SimpleChannelUpstreamHandler with WebSocketServer {
  val bootstrap = new ServerBootstrap(
    new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline();
      pipeline.addLast("decoder", new HttpRequestDecoder)
      pipeline.addLast("aggregator", new HttpChunkAggregator(65536))
      pipeline.addLast("encoder", new HttpResponseEncoder)
      pipeline.addLast("handler", TestServer.this);
      pipeline
    }
  });

  def start() {
    bootstrap.bind(new InetSocketAddress(8080));
  }

  def shutdown {
    bootstrap.bind.close
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case http: HttpRequest => handleHttpRequest(context, http)
    }
  }
}

trait ResponseHandshake extends WebSocketServer {
  override def handleHttpRequest(context: ChannelHandlerContext, request: HttpRequest) {
    if (request.getHeader(CONNECTION) == Values.UPGRADE && request.getHeader(Names.UPGRADE) == WEBSOCKET) {
      val response = new DefaultHttpResponse(HTTP_1_1, new HttpResponseStatus(101, "Web Socket Protocol Handshake"))
      response.addHeader(Names.UPGRADE, WEBSOCKET)
      response.addHeader(CONNECTION, Values.UPGRADE)
      response.addHeader(WEBSOCKET_ORIGIN, request.getHeader(ORIGIN))
      response.addHeader(WEBSOCKET_LOCATION, "ws://localhost:8080")
      response.addHeader(WEBSOCKET_PROTOCOL, if (request.getHeaderNames.contains(WEBSOCKET_PROTOCOL)) request.getHeader(WEBSOCKET_PROTOCOL) else "ws")

      val pipeline = context.getChannel.getPipeline
      pipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder)
      context.getChannel.write(response)
      pipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder)
    }
  }
}