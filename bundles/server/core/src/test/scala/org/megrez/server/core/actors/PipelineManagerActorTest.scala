package org.megrez.server.core.actors

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PipelineManagerActorTest extends Spec with ShouldMatchers {
  describe("Pipeline manager") {
    it("should ") {
      val actor = new PipelineManagerActor
      actor.addPipeline("name") should equal("ok")
    }
  }
}