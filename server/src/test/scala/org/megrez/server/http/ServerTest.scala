package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers

import org.scalatest.{BeforeAndAfterEach, Spec}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, ChannelPipeline, Channel}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, DefaultHttpRequest}
import org.jboss.netty.buffer._
import org.jboss.netty.util._

import scala.actors._
import scala.actors.Actor._
import Method._
import Route._

class ServerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("HTTP Server") {
    it("should delegate to handler if path matched") {
      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent"), Array[Any]())

      val server = new Server(
        path("/agent") -> actor {
          react {
            case _: Request =>
              reply(HttpResponse.OK)
          }
        }
        )

      server.messageReceived(context, event)
    }

    it("should delegate to handler if GET request path matched") {
      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent"), Array[Any]())

      val server = new Server(
        get("/agent") -> actor {
          react {
            case request: Request =>
              request.method should be === GET
              request.uri should be === "/agent"
              reply(HttpResponse.OK)
          }
        }
        )

      server.messageReceived(context, event)
    }

    it("should delegate to handler if POST request path matched") {
      val event = mock[MessageEvent]
      var httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/pipeline")
      httpRequest.setContent(ChannelBuffers.copiedBuffer("testContent", CharsetUtil.UTF_8));
      when(event.getMessage).thenReturn(httpRequest, Array[Any]())

      val server = new Server(
        post("/pipeline") -> actor {
          react {
            case request: Request =>
              request.method should be === POST
              request.uri should be === "/pipeline"
              request.content should be === "testContent"
              reply(HttpResponse.OK)
          }
        }
        )

      server.messageReceived(context, event)
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