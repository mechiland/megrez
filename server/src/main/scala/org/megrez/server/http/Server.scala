package org.megrez.server.http

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors._
import javax.ws.rs.ext.RuntimeDelegate
import com.sun.jersey.api.core.PackagesResourceConfig
import org.jboss.netty.channel._
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.jboss.netty.handler.codec.http.websocket.{WebSocketFrameDecoder, WebSocketFrameEncoder}
import org.jboss.netty.handler.codec.http.HttpHeaders.{Names, Values}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpVersion._

class Server(val routes: Route*) {
  import scala.collection.JavaConversions._
  private def packageConfig = Map("com.sun.jersey.config.property.packages" -> packages)

  private def packages = routes.map(_ match {
    case ResourcePackage(packageName) => packageName
    case _ => ""
  }).filter(!_.isEmpty).mkString("", ",", ",org.megrez.server.http")

  private val jerseyHandler = RuntimeDelegate.getInstance.createEndpoint(new PackagesResourceConfig(packageConfig), classOf[ChannelUpstreamHandler])

  private val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(newCachedThreadPool(), newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpRequestDecoder())
      pipeline.addLast("aggregator", new HttpChunkAggregator(65536))
      pipeline.addLast("encoder", new HttpResponseEncoder())
      pipeline.addLast("handler", Handler)
      pipeline
    }
  })

  object Handler extends SimpleChannelUpstreamHandler {
    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      e.getMessage match {
        case request: HttpRequest =>
          findWebSocket(request) match {
            case Some(route : WebSocket) =>
              handleWebSocketHandshake(request, route, ctx, jerseyHandler.handleUpstream(ctx, e))
            case _ => jerseyHandler.handleUpstream(ctx, e)
          }
        case _ =>
      }

    }

    private val WebSocketHandshake = new HttpResponseStatus(101, "Web Socket Protocol Handshake")

    private def handleWebSocketHandshake(request: HttpRequest, route: WebSocket, context: ChannelHandlerContext, op :  => Unit) =
      if (Values.UPGRADE == request.getHeader(CONNECTION) && WEBSOCKET == request.getHeader(Names.UPGRADE)) {
        val response = new DefaultHttpResponse(HTTP_1_1, WebSocketHandshake)
        response.addHeader(Names.UPGRADE, WEBSOCKET)
        response.addHeader(CONNECTION, Values.UPGRADE)
        response.addHeader(WEBSOCKET_ORIGIN, request.getHeader(ORIGIN))
        response.addHeader(WEBSOCKET_LOCATION, request.getHeader(ORIGIN))
        response.addHeader(WEBSOCKET_LOCATION, "ws://" + request.getHeader(HOST) + request.getUri)
        response.addHeader(WEBSOCKET_PROTOCOL, "ws")
        val channel = context.getChannel
        val pipeline = channel.getPipeline
        pipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder)
        channel.write(response).addListener(new ChannelFutureListener() {
          override def operationComplete(channelFuture: ChannelFuture) {
            pipeline.remove("aggregator")
            pipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder)
            pipeline.replace("handler", "ws-handler", route.factory(channel, route.actor))
          }
        })
      } else {
        op
      }
  }

  private def findWebSocket(request : HttpRequest) = routes.find(_ match {
    case WebSocket(path, _, _) => request.getUri == path
    case _ => false
  })

  private var channel: Channel = _

  def start(port: Int) {
    channel = bootstrap.bind(new InetSocketAddress(port))
  }

  def shutdown {
    if (channel != null) channel.close.awaitUninterruptibly(30, TimeUnit.SECONDS)
    if (bootstrap != null) bootstrap.releaseExternalResources
  }
}