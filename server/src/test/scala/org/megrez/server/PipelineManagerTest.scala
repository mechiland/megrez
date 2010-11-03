package org.megrez.server

import org.scalatest.Spec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import org.mockito.Matchers._
import actors.{TIMEOUT, Actor}
import actors.Actor._

class PipelineManagerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("Pipeline manager") {
    it("should launch trigger when new pipeline added") {
      val factory = mock[Pipeline => Trigger]

      when(factory.apply(any(classOf[Pipeline]))).thenReturn(new ActorBasedTrigger("pipeline", self))

      val manager = new PipelineManager(factory)
      manager ! AddPipeline(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER START pipeline" =>
        case TIMEOUT =>
        case _ => fail
      }
    }

    it("should shutdown trigger and start new one when pipeline config changed") {

      val factory = mock[Pipeline => Trigger]

      when(factory.apply(any(classOf[Pipeline]))).thenReturn(new ActorBasedTrigger("pipeline1", self),
        new ActorBasedTrigger("pipeline2", self))

      val manager = new PipelineManager(factory)
      manager ! AddPipeline(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER START pipeline1" =>
        case TIMEOUT =>
        case _ => fail
      }
      
      manager ! PipelineChanged(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER STOP pipeline1" =>
        case TIMEOUT =>
        case _ => fail
      }

      receiveWithin(1000) {
        case "TRIGGER START pipeline2" =>
        case TIMEOUT =>
        case _ => fail
      }
    }
  }

  class ActorBasedTrigger(val name: String, val actor : Actor) extends Trigger {
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