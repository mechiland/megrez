package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers

import Route._
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.scalatest.mock.MockitoSugar
import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, ChannelPipeline, Channel}
import org.mockito._
import org.mockito.Mockito._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, DefaultHttpRequest}
import scala.actors._
import scala.actors.Actor._

class ServerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("HTTP Server") {
    it("should delegate to handler if path matched") {
      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent"), Array[Any]())

      val server = new Server(
        path("/agent") -> self
      )
      
      server.messageReceived(context, event)
      
      receiveWithin(1000) {
        case _ : HttpRequest =>
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should delegate to handler if GET request path matched") {
      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent"), Array[Any]())

      val server = new Server(
        get("/agent") -> self
      )

      server.messageReceived(context, event)

      receiveWithin(1000) {
        case request : HttpRequest => request.method should be === GET
        case TIMEOUT => fail
        case _ => fail
      }
    }

	it("should delegate to handler if POST request path matched") {
      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/agent"), Array[Any]())

      val server = new Server(
        post("/agent") -> self
      )

      server.messageReceived(context, event)

      receiveWithin(1000) {
        case request : HttpRequest => request.method should be === POST
        case TIMEOUT => fail
        case _ => fail
      }
    }

  }

  var context: ChannelHandlerContext = _
  var pipeline: ChannelPipeline = _
  var channel: Channel = _

  override def beforeEach() {
    context = mock[ChannelHandlerContext]
    pipeline = mock[ChannelPipeline]
    channel = mock[Channel]
    when(context.getPipeline).thenReturn(pipeline)
    when(context.getChannel).thenReturn(channel)
  }

  def is(string: String) = org.mockito.Matchers.eq(string)

}