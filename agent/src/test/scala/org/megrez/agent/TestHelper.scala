package org.megrez.agent

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
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import collection.mutable.Queue
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.buffer.ChannelBuffers
import java.net.{URI, InetSocketAddress}
import actors.{TIMEOUT, Actor}
import scala.actors.Actor._
import org.scalatest.{Spec, Assertions, BeforeAndAfterEach}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

abstract class Behaviour
abstract trait HttpBehaviour extends Behaviour {
  def handleHttpRequest(context: ChannelHandlerContext, http: HttpRequest)
}
abstract trait WebSocketBehaviour extends Behaviour {
  def handleWebSocketFrame(context: ChannelHandlerContext, request: WebSocketFrame)
}

class WebSocketClient(val handler: SimpleChannelUpstreamHandler) {
  val bootstrap = new ClientBootstrap(
    new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpResponseDecoder())
      pipeline.addLast("encoder", new HttpRequestEncoder())
      pipeline.addLast("ws-handler", handler)
      pipeline
    }
  })

  bootstrap.connect(new InetSocketAddress("localhost", 8080))
}

class WebSocketServer extends SimpleChannelUpstreamHandler {
  val httpBehaviours: Queue[HttpBehaviour] = Queue[HttpBehaviour]()
  val websocketBehaviours: Queue[WebSocketBehaviour] = Queue[WebSocketBehaviour]()

  val bootstrap = new ServerBootstrap(
    new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline();
      pipeline.addLast("decoder", new HttpRequestDecoder)
      pipeline.addLast("aggregator", new HttpChunkAggregator(65536))
      pipeline.addLast("encoder", new HttpResponseEncoder)
      pipeline.addLast("handler", WebSocketServer.this);
      pipeline
    }
  });

  private var channel: Channel = _

  def start {
    channel = bootstrap.bind(new InetSocketAddress(8080));
  }

  def shutdown {
    channel.close.awaitUninterruptibly
  }

  private var webSocketChannel: Channel = _

  override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
  }

  def send(socket: WebSocketFrame) {
    webSocketChannel.write(socket)
  }

  def response(behaviours: Behaviour*) {
    behaviours.foreach(_ match {
      case http: HttpBehaviour => httpBehaviours.enqueue(http)
      case websocket: WebSocketBehaviour => websocketBehaviours.enqueue(websocket)
    })
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case http: HttpRequest => handleHttpRequest(context, http)
      case websocket: WebSocketFrame => handleWebSocketFrame(context, websocket)
    }
  }

  private def handleHttpRequest(context: ChannelHandlerContext, request: HttpRequest) {
    httpBehaviours.dequeue.handleHttpRequest(context, request)
  }

  private def handleWebSocketFrame(context: ChannelHandlerContext, request: WebSocketFrame) {
    webSocketChannel = context.getChannel
    websocketBehaviours.dequeue.handleWebSocketFrame(context, request)
  }
  override def exceptionCaught(context: ChannelHandlerContext, event: ExceptionEvent) {
    event.getCause match {
      case exception: NotMegrezServerException =>
      case exception :NoSuchElementException =>
    }
  }
}

trait ActorBasedServerHandlerMixin extends ServerHandler {
  var actor: Actor = _

  override def connected(channel : Channel) {
    super.connected(channel)
    actor ! "CONNECTED"
  }

  override def disconnected() {
    super.disconnected
    actor ! "DISCONNECTED"
  }

  override def invalidServer(uri: URI) {
    super.invalidServer(uri)
    actor ! "NOT A MEGREZ SERVER"
  }
}

object WebSocketHandshake extends HttpBehaviour {
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

object Forbidden extends HttpBehaviour {
  override def handleHttpRequest(context: ChannelHandlerContext, request: HttpRequest) {
    if (request.getHeader(CONNECTION) == Values.UPGRADE && request.getHeader(Names.UPGRADE) == WEBSOCKET) {
      val response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN)

      response.setContent(ChannelBuffers.copiedBuffer(response.getStatus.toString, CharsetUtil.UTF_8))
      setContentLength(response, response.getContent.readableBytes)
      context.getChannel.write(response)
    }
  }
}

object MegrezHandshake extends WebSocketBehaviour {
  var actor: Actor = _

  override def handleWebSocketFrame(context: ChannelHandlerContext, request: WebSocketFrame) {
    if (request.getTextData == "megrez-agent:1.0") {
      context.getChannel.write(new DefaultWebSocketFrame("megrez-server:1.0"))
      if (actor != null) actor ! "MEGREZ HANDSHAKE"
    }
  }
}

object CloseAfterMegrezHandshake extends WebSocketBehaviour {
  override def handleWebSocketFrame(context: ChannelHandlerContext, request: WebSocketFrame) {
    if (request.getTextData == "megrez-agent:1.0") {
      val future = context.getChannel.write(new DefaultWebSocketFrame("megrez-server:1.0"))
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }
}

object Something extends WebSocketBehaviour {
  override def handleWebSocketFrame(context: ChannelHandlerContext, request: WebSocketFrame) {
    if (request.getTextData == "megrez-agent:1.0") {
      context.getChannel.write(new DefaultWebSocketFrame("something"))
    }
  }
}

object ReceiveResponse extends WebSocketBehaviour {
  var actor: Actor = _
  
  override def handleWebSocketFrame(context: ChannelHandlerContext, request: WebSocketFrame) {
    actor ! request.getTextData
  }
}

trait ServerIntegration extends Spec with BeforeAndAfterEach {
  def connect = {
    val connection = new Server(new URI("ws://localhost:8080/"), 500, if (worker != null) worker else actor {}) with ActorBasedServerHandlerMixin
    connection.actor = self
    connection.connect
  }

  def expect(Message: String, timeout: Int) {
    receiveWithin(timeout) {
      case Message =>
      case TIMEOUT => fail("Timeout expecting " + Message)
      case _ => fail
    }
  }

  var server: WebSocketServer = _
  var worker: Actor = _

  override def beforeEach() {
    server = new WebSocketServer
  }

  override def afterEach() {
    server.shutdown
  }
}

trait HandlerTest extends Spec with BeforeAndAfterEach with MockitoSugar {
  var context: ChannelHandlerContext = _
  var pipeline: ChannelPipeline = _
  var channel: Channel = _
  var serverHandler: ServerHandler = _

  override def beforeEach() {
    context = mock[ChannelHandlerContext]
    pipeline = mock[ChannelPipeline]
    channel = mock[Channel]
    when(context.getPipeline).thenReturn(pipeline)
    when(context.getChannel).thenReturn(channel)

    serverHandler = mock[ServerHandler]
  }

  def is(string: String) = org.mockito.Matchers.eq(string)
}

