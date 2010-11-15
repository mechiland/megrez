package org.megrez.server.http

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors._
import org.jboss.netty.channel._
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.util._
import websocket.{DefaultWebSocketFrame, WebSocketFrameDecoder, WebSocketFrameEncoder, WebSocketFrame}
import org.megrez.server.{ToAgentManager, AgentHandler}
import actors.Actor
import org.megrez.AgentMessage
import org.megrez.util.{JSON, Logging}
class Server(val routes: Route*) extends SimpleChannelUpstreamHandler with Logging {
  private val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(newCachedThreadPool(), newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpRequestDecoder())
      pipeline.addLast("aggregator", new HttpChunkAggregator(65536))
      pipeline.addLast("encoder", new HttpResponseEncoder())
      pipeline.addLast("handler", Server.this)
      pipeline
    }
  })


  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case request: HttpRequest =>
        routes.find(_ matches request) match {
          case Some(route: WebSocketRoute) =>
            handleWebSocketHandshake(request, route, context)
          case Some(route: HttpRoute) =>
            val content = request.getContent
            val response = route.handler !? Request(route matchedMethod request, request.getUri, content.toString(CharsetUtil.UTF_8))
            response match {
              case HttpResponse.OK =>
                event.getChannel.write(new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK)).addListener(ChannelFutureListener.CLOSE)
              case HttpResponse.ERROR =>
                event.getChannel.write(new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)).addListener(ChannelFutureListener.CLOSE)
            }
          case None =>
            context.getChannel.write(new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND)).addListener(ChannelFutureListener.CLOSE)
        }
      case frame: WebSocketFrame => println("here" + frame)
      case _ => error("unhandle request " + event.getMessage)
    }
  }

  private val WebSocketHandshake = new HttpResponseStatus(101, "Web Socket Protocol Handshake")

  private def handleWebSocketHandshake(request: HttpRequest, route: WebSocketRoute, context: ChannelHandlerContext) =
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
          pipeline.replace("handler", "ws-handler", route.websocketHandler(channel))
        }
      })
    } else {

    }


  private var channel: Channel = _

  def start(port: Int) {
    channel = bootstrap.bind(new InetSocketAddress(port));
  }

  def shutdown() {
    channel.close.awaitUninterruptibly
  }
}

class WebSocketHandler(val channel: Channel, handler: Actor) extends SimpleChannelUpstreamHandler {
  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case frame: WebSocketFrame =>
        if (!frame.isBinary) handler ! frame.getTextData
      case _ =>
    }
  }

  handler ! channel
}

class AgentWebSocketHandler(val channel: Channel, agentManager: Actor) extends SimpleChannelUpstreamHandler with AgentHandler with Logging {
  private val MegrezAgentHandshake = "megrez-agent:1.0"
  private var agent: Actor = _

  override def assignAgent(actor: Actor) {
    agent = actor
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case frame: WebSocketFrame =>
        frame.getTextData match {
          case MegrezAgentHandshake =>
            send("megrez-server:1.0")
            agentManager ! ToAgentManager.RemoteAgentConnected(this)
          case message: String =>
            agent ! JSON.read[AgentMessage](message)
        }
      case _ =>
    }
  }

  def send(message: String) {
    channel.write(new DefaultWebSocketFrame(message))
  }
}




