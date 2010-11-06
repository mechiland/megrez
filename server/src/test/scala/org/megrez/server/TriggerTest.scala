package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import actors.Actor._
import org.scalatest.mock.MockitoSugar
import org.megrez.ChangeSource
import org.mockito.Mockito._

class TriggerTest extends Spec with ShouldMatchers with MockitoSugar{
  describe("Trigger") {
    it("should trigger ") {
      val source = mock[ChangeSource]
      val material = new org.megrez.Material(source)
      val pipeline = new org.megrez.Pipeline("pipeline", Set(material), List[org.megrez.Pipeline.Stage]())

      

      val pipelineTrigger = new PipelineTrigger(pipeline, self)

    }
  }
}