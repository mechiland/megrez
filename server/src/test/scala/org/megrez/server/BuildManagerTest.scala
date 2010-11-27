package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.Pipeline

class BuildManagerTest extends Spec with ShouldMatchers {

  describe("Completed Pipelines") {
    it("should add canceled build to completed pipelines") {
      val pipeline = new Pipeline("pipeline", null, List())
      val build = new Build(pipeline)
      val buildManager = new BuildManager()

      buildManager ! SchedulerToBuildManager.BuildCanceled(build)

      buildManager !? ToBuildManager.CompletedPipelines match {
        case pipelines : Iterable[Build] =>
          pipelines.size should be === 1
        case _ => fail
      }

    }
  }

}