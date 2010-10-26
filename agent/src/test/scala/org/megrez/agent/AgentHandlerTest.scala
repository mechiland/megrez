package org.megrez.agent

import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame
import actors.Actor._

class AgentHandlerTest extends HandlerTest with ShouldMatchers {
  describe("Agent handler") {
    it("should receive job assignment and send message to worker") {
      val event = mock[MessageEvent]
      //      val jobAssignment = "{\"pipelineId\" : \"pipeline\", \"vcs\" : {\"type\" : \"svn\", \"url\" : \"svn_url\"} }"
      //      val message = new DefaultWebSocketFrame()
      //      when(event.getMessage).thenReturn(null, Array[Any]())
      handler.messageReceived(context, event)
    }
  }

  var handler: AgentHandler = _

  override def beforeEach() {
    super.beforeEach
    handler = new AgentHandler(serverHandler, self)
  }

}