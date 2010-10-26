package org.megrez.agent

import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame
import actors.Actor._
import actors._

class AgentHandlerTest extends HandlerTest with ShouldMatchers {
  describe("Agent handler") {
    it("should receive job assignment and send message to worker") {
      val event = mock[MessageEvent]
      val pipelineJson =
                 """{"pipeline" : {"id" : "pipeline", "vcs" : {"type" : "svn", "url" : "svn_url"}},
                     "workSet"  : {"revision" : "100"},
                     "job"      : {"tasks" : [] } }"""
      val message = new DefaultWebSocketFrame(pipelineJson)
      when(event.getMessage).thenReturn(message, Array[Any]())
      handler.messageReceived(context, event)

      receiveWithin(1000) {
        case assignment : JobAssignment =>
          assignment.pipelineId should equal("pipeline")
          assignment.versionControl.toString should equal("svn : svn_url")
          assignment.workSet should equal(100)
          assignment.job.tasks.size should equal(0)
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }

  var handler: AgentHandler = _

  override def beforeEach() {
    super.beforeEach
    handler = new AgentHandler(serverHandler, self)
  }

}