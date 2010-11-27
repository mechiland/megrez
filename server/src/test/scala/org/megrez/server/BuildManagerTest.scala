package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.Pipeline

class BuildManagerTest extends Spec with ShouldMatchers {
  val build = new Build(new Pipeline("pipeline", null, List()))
  val buildManager = new BuildManager()

  describe("Completed Pipelines") {
    it("should add canceled build to completed pipelines") {
      buildManager ! SchedulerToBuildManager.BuildCanceled(build)
      assertResult
    }

    it("should add completed build to completed pipelines") {
      buildManager ! SchedulerToBuildManager.BuildCompleted(build)
      assertResult
    }

    it("should add failed build to completed pipelines") {
      buildManager ! SchedulerToBuildManager.BuildFailed(build)
      assertResult
    }
  }

  private def assertResult() {
    buildManager !? ToBuildManager.CompletedBuilds match {
      case builds: Iterable[Build] =>
        builds.size should be === 1
        builds.head should be === build
      case _ => fail
    }
  }

}