package org.megrez.server.json

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.Pipeline.Stage
import org.megrez.server.Build._
import JSON._ //Don't delete this line
import org.megrez.server.Build
import org.megrez.{Pipeline, Job}

class JSONTest extends Spec with ShouldMatchers {
  val job = new Job("job", Set(), List())
  val stage = new Stage("stage", Set(job))

  describe("Domain object serialization") {
    it("should serialize JobStage") {
      val ongoingStage = new JobStage(stage)
      JSON.write(ongoingStage) should equal("""{"name":"stage","status":"ongoing","jobs":[{"name":"job","status":"ongoing"}]}""")

      val completedStage = new JobStage(stage)
      completedStage.complete(job)
      JSON.write(completedStage) should equal("""{"name":"stage","status":"completed","jobs":[{"name":"job","status":"completed"}]}""")

      val failedStage = new JobStage(stage)
      failedStage.fail(job)
      JSON.write(failedStage) should equal("""{"name":"stage","status":"failed","jobs":[{"name":"job","status":"failed"}]}""")
    }

    it("should serialize Build") {
      val buildWithOneStage = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)))))
      JSON.write(buildWithOneStage) should equal("""{"name":"pipeline","stages":[{"name":"stage","status":"ongoing","jobs":[{"name":"job","status":"ongoing"}]}]}""")

      val buildWithTwoStages = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      JSON.write(buildWithTwoStages) should equal("""{"name":"pipeline","stages":[{"name":"stage","status":"ongoing","jobs":[{"name":"job","status":"ongoing"}]},{"name":"stage2","status":"unknown","jobs":[{"name":"job","status":"unknown"}]}]}""")

      val completedBuild = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      completedBuild.complete(job)
      completedBuild.complete(job)
      JSON.write(completedBuild) should equal("""{"name":"pipeline","stages":[{"name":"stage","status":"completed","jobs":[{"name":"job","status":"completed"}]},{"name":"stage2","status":"completed","jobs":[{"name":"job","status":"completed"}]}]}""")

      val failedOnFirstStage = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      failedOnFirstStage.fail(job)
      JSON.write(failedOnFirstStage) should equal("""{"name":"pipeline","stages":[{"name":"stage","status":"failed","jobs":[{"name":"job","status":"failed"}]},{"name":"stage2","status":"unknown","jobs":[{"name":"job","status":"unknown"}]}]}""")

      val failedOnSecondStage = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      failedOnSecondStage.complete(job)
      failedOnSecondStage.fail(job)
      JSON.write(failedOnSecondStage) should equal("""{"name":"pipeline","stages":[{"name":"stage","status":"completed","jobs":[{"name":"job","status":"completed"}]},{"name":"stage2","status":"failed","jobs":[{"name":"job","status":"failed"}]}]}""")
    }
  }
}