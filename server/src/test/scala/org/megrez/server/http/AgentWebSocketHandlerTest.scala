package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

import scala.actors.Actor._
import org.mockito.Mockito._
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame
import actors.TIMEOUT
import org.megrez.server.ToAgentManager
import org.jboss.netty.channel.{Channel, MessageEvent, ChannelPipeline, ChannelHandlerContext}
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.megrez.util.JSON
import org.megrez.JobCompleted

class AgentWebSocketHandlerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Agent WebSocket handler") {
    it("should notify agent connected") {
      val handler = new AgentWebSocketHandler(channel, self)

      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultWebSocketFrame("megrez-agent:1.0"), Array[Any]())

      handler.messageReceived(context, event)

      receiveWithin(1000) {
        case message: ToAgentManager.RemoteAgentConnected =>
          message.handler should be === (handler)
          message.handler.assignAgent(self)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should send message to handler") {
      val handler = new AgentWebSocketHandler(channel, self)

      val event = mock[MessageEvent]
      when(event.getMessage).thenReturn(new DefaultWebSocketFrame("megrez-agent:1.0"), Array[Any]())

      handler.messageReceived(context, event)

      receiveWithin(1000) {
        case message: ToAgentManager.RemoteAgentConnected =>
          message.handler should be === (handler)
          message.handler.assignAgent(self)
        case TIMEOUT => fail
        case other: Any => println(other); fail
      }

      val newMessage = mock[MessageEvent]
      when(newMessage.getMessage).thenReturn(new DefaultWebSocketFrame(JSON.write(JobCompleted())), Array[Any]())

      handler.messageReceived(context, newMessage)

      receiveWithin(1000) {
        case _ : JobCompleted =>
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

}