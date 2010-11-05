package org.megrez.server.http

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.megrez.server.{Trigger, Pipeline, PipelineManager}
import org.mockito.Mockito._
import org.mockito.Matchers._
import actors.Actor._
import actors.{TIMEOUT, Actor}

class PipelineControllerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("receive request") {
    it("handle POST request") {
      val pipelineController: PipelineController = new PipelineController(mockPipelineManager)
      pipelineController ! Request(Method.GET, "/pipelines",
        """{"pipeline" : {"name" : "pipeline1", "vcs" : {"type" : "svn", "url" : "svn_url"}}}""")
      receiveWithin(1000) {
        case "TRIGGER START pipeline" =>
        case TIMEOUT =>
        case _ => fail
      }
    }
  }

  private def mockPipelineManager: PipelineManager = {
    object Context {
      val triggerFactory = mock[Pipeline => Trigger] 
    }
    when(Context.triggerFactory.apply(any(classOf[Pipeline]))).thenReturn(new ActorBasedTrigger("pipeline", self))
    new PipelineManager(Context)
  }

  class ActorBasedTrigger(val name: String, val actor: Actor) extends Trigger {
    val pipeline: Pipeline = null
    val target: Actor = null

    def start {
      actor ! ("TRIGGER START " + name)
    }

    def stop {
      actor ! ("TRIGGER STOP " + name)
    }
  }
}