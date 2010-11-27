package org.megrez.server.json

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.Pipeline.Stage
import org.megrez.server.Build._
import JSON._ //Don't delete this line
import org.megrez.{Material, Pipeline, Job}
import org.megrez.vcs.Git
import org.megrez.server.Build
class JSONTest extends Spec with ShouldMatchers {
  val job = new Job("job", Set(), List())
  val stage = new Stage("stage", Set(job))
  val material = new Material(new Git("git_url"))
  val pipeline = new Pipeline("pipeline", Set(material), List(stage))

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

      val canceledStage = new JobStage(stage)
      canceledStage.cancel(Set(job))
      JSON.write(canceledStage) should equal("""{"name":"stage","status":"canceled","jobs":[{"name":"job","status":"canceled"}]}""")
    }

    it("should serialize Build") {
      val buildWithOneStage = new Build(pipeline, Map(material -> Some("#1")))
      JSON.write(buildWithOneStage) should equal("""{"name":"pipeline","materials":[{"revision":"#1"}],"stages":[{"name":"stage","status":"ongoing","jobs":[{"name":"job","status":"ongoing"}]}]}""")

      val buildWithTwoStages = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      JSON.write(buildWithTwoStages) should equal("""{"name":"pipeline","materials":[],"stages":[{"name":"stage","status":"ongoing","jobs":[{"name":"job","status":"ongoing"}]},{"name":"stage2","status":"unknown","jobs":[{"name":"job","status":"unknown"}]}]}""")

      val completedBuild = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      completedBuild.complete(job)
      completedBuild.complete(job)
      JSON.write(completedBuild) should equal("""{"name":"pipeline","materials":[],"stages":[{"name":"stage","status":"completed","jobs":[{"name":"job","status":"completed"}]},{"name":"stage2","status":"completed","jobs":[{"name":"job","status":"completed"}]}]}""")

      val failedOnFirstStage = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      failedOnFirstStage.fail(job)
      JSON.write(failedOnFirstStage) should equal("""{"name":"pipeline","materials":[],"stages":[{"name":"stage","status":"failed","jobs":[{"name":"job","status":"failed"}]},{"name":"stage2","status":"unknown","jobs":[{"name":"job","status":"unknown"}]}]}""")

      val failedOnSecondStage = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      failedOnSecondStage.complete(job)
      failedOnSecondStage.fail(job)
      JSON.write(failedOnSecondStage) should equal("""{"name":"pipeline","materials":[],"stages":[{"name":"stage","status":"completed","jobs":[{"name":"job","status":"completed"}]},{"name":"stage2","status":"failed","jobs":[{"name":"job","status":"failed"}]}]}""")

      val canceledStage = new Build(new Pipeline("pipeline", Set(), List(new Stage("stage", Set(job)), new Stage("stage2", Set(job)))))
      canceledStage.cancel(Set(job))
      JSON.write(canceledStage) should equal("""{"name":"pipeline","materials":[],"stages":[{"name":"stage","status":"canceled","jobs":[{"name":"job","status":"canceled"}]},{"name":"stage2","status":"unknown","jobs":[{"name":"job","status":"unknown"}]}]}""")
    }
  }
}