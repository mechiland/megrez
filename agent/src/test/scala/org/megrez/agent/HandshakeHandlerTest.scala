package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import java.net.URI

import actors.Actor
import actors.Actor._
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.scalatest.mock.MockitoSugar
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpHeaders.{Values, Names}
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, DefaultHttpResponse, HttpMethod, HttpRequest}
import org.jboss.netty.handler.codec.http.websocket.{DefaultWebSocketFrame, WebSocketFrame, WebSocketFrameDecoder, WebSocketFrameEncoder}

class HandshakeHandlerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Agent-Server handshake handler") {
    it("should send handshake message after channel connected") {
      val event = mock[ChannelStateEvent]
      when(event.getChannel).thenReturn(channel)      
      
      handler.channelConnected(context, event)

      val request = ArgumentCaptor.forClass(classOf[Any])

      verify(pipeline).replace(is("encoder"), is("ws-encoder"), isA(classOf[WebSocketFrameEncoder]))
      verify(channel).write(request.capture)

      request.getValue match {
        case http: HttpRequest =>
          http.getMethod should equal(HttpMethod.GET)
          http.getHeader(Names.UPGRADE) should equal(Values.WEBSOCKET)
          http.getHeader(Names.CONNECTION) should equal(Values.UPGRADE)
          http.getHeader(Names.CONNECTION) should equal(Values.UPGRADE)
          http.getHeader(Names.HOST) should equal("localhost:8080")
          http.getHeader(Names.ORIGIN) should equal("http://localhost")
        case _ => fail
      }
    }

    it("should send handshake websocket frame if confirm websocket") {
      val event = mock[MessageEvent]
      val response = new DefaultHttpResponse(HTTP_1_1, new HttpResponseStatus(101, "Web Socket Protocol Handshake"))
      response.addHeader(Names.UPGRADE, Values.WEBSOCKET)
      response.addHeader(Names.CONNECTION, Values.UPGRADE)
      response.addHeader(Names.WEBSOCKET_ORIGIN, "http://localhost")
      response.addHeader(Names.WEBSOCKET_LOCATION, "ws://localhost:8080")
      response.addHeader(Names.WEBSOCKET_PROTOCOL, "ws")

      when(event.getChannel).thenReturn(channel)
      when(event.getMessage).thenReturn(response, Array[Any]())

      handler.messageReceived(context, event)

      val request = ArgumentCaptor.forClass(classOf[Any])
      
      verify(pipeline).replace(is("decoder"), is("ws-decoder"), isA(classOf[WebSocketFrameDecoder]))
      verify(channel).write(request.capture)

      request.getValue match {
        case frame : WebSocketFrame =>
          frame.getTextData should be("megrez-agent:1.0")
        case _ => fail
      }
    }

    it ("should notify handler if server reponse megrez handshake") {
      val event = mock[MessageEvent]
      val frame = new DefaultWebSocketFrame("megrez-server:1.0")      

      when(event.getChannel).thenReturn(channel)
      when(event.getMessage).thenReturn(frame, Array[Any]())

      handler.messageReceived(context, event)

      verify(serverHandler).connected
    }

    it ("should call invalid server if server does not repsonse websocket handshake") {
      val event = mock[MessageEvent]
      val response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND)

      when(event.getChannel).thenReturn(channel)
      when(event.getMessage).thenReturn(response, Array[Any]())


      evaluating { handler.messageReceived(context, event) } should produce [NotMegrezServerException]
    }

    it ("should call invalid server if server does not repsonse megrez handshake") {
      val event = mock[MessageEvent]
      val frame = new DefaultWebSocketFrame("something")      

      when(event.getChannel).thenReturn(channel)
      when(event.getMessage).thenReturn(frame, Array[Any]())

      evaluating { handler.messageReceived(context, event) } should produce [NotMegrezServerException]
    }

    it ("should notify handler if not megrez server exception raised") {
      val event = mock[ExceptionEvent]
      val uri: URI = new URI("ws://localhost:8080")
      when(event.getCause).thenReturn(new NotMegrezServerException(uri))
      
      handler.exceptionCaught(context, event)

      verify(serverHandler).invalidServer(same(uri))
    }    
  }

  var context : ChannelHandlerContext = _
  var pipeline : ChannelPipeline = _
  var channel : Channel = _
  var serverHandler : ServerHandler = _
  var handler : HandshakeHandler = _

  override def beforeEach() {
    context = mock[ChannelHandlerContext]
    pipeline = mock[ChannelPipeline]
    channel = mock[Channel]
    when(context.getPipeline).thenReturn(pipeline)
    when(context.getChannel).thenReturn(channel)
    
    serverHandler = mock[ServerHandler]
    handler = new HandshakeHandler(new URI("ws://localhost:8080/"), serverHandler)
  }

  def is(string : String) = org.mockito.Matchers.eq(string)
}