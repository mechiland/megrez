package org.megrez.agent

import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import org.jboss.netty.channel.MessageEvent
import actors.Actor._
import actors._
import org.mockito._
import org.jboss.netty.handler.codec.http.websocket.{WebSocketFrame, DefaultWebSocketFrame}
import org.megrez.vcs.Subversion
import org.megrez.{JobCompleted, JobAssignment}
import org.megrez.util.JSON

class AgentHandlerTest extends HandlerTest with ShouldMatchers {
  describe("Agent handler") {
    it("should receive job assignment and send message to worker and repsone to server") {
      val event = mock[MessageEvent]
      val pipelineJson = """{"type" : "assignment", "pipeline" : "pipeline", "materials" : [{ "material" : {"type" : "svn", "url" : "svn_url", "dest" : "dest"}, "workset" : {"revision" : 1} }], "job" : {"name" : "unit test", "resources" :[], "tasks" : [{ "type" : "cmd", "command": "ls"}] } }"""
      val message = new DefaultWebSocketFrame(pipelineJson)
      when(event.getMessage).thenReturn(message, Array[Any]())      
      handler.messageReceived(context, event)

      receiveWithin(1000) {
        case (actor : Actor, assignment : JobAssignment) =>
          assignment.pipeline should equal("pipeline")
          assignment.materials should have size(1)
          val (material, workset) = assignment.materials.head
          material.source match {
            case subversion : Subversion =>
              subversion.url should equal("svn_url")
            case _ => fail
          }
          workset should equal(Some(1))          
          actor ! new JobCompleted
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case TIMEOUT =>
        case a : Any => println(a); fail
      }

      var response = ArgumentCaptor.forClass(classOf[Any])

      verify(channel).write(response.capture)
      response.getValue match {
        case frame : WebSocketFrame =>
          frame.getTextData should equal(JSON.write(JobCompleted()))
        case _ => fail
      }
    }
  }

  var handler: AgentHandler = _

  override def beforeEach() {
    super.beforeEach
    handler = new AgentHandler(serverHandler, self)
    handler ! channel
  }

}