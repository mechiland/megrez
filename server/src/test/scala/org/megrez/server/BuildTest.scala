package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.Pipeline.Stage
import java.lang.String
import collection.immutable.Set
import org.megrez.{Pipeline, Job, Task}

class BuildTest extends Spec with ShouldMatchers {
  describe("Build") {
    it("should return job from stage") {
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1")))

      val build = new Build(pipeline)

      val jobs = build.current.jobs
      jobs should have size (1)
      jobs.head.name should equal("stage1 job")
    }

    it("should return same job if previous job not complet") {
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1"), createStage("stage2")))
      val build = new Build(pipeline)
      build.current.jobs should have size (1)
      build.current.jobs should be === build.current.jobs
    }

    it("should return job of next stage when first stage successful") {
      val job1 = createJob("job1")
      val job2 = createJob("job2")
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1", job1), createStage("stage1", job2)))
      val build = new Build(pipeline)
      build.complete(job1) match {
        case Some(stage : Build.Stage) =>
          build.current should be === stage
          val jobs = stage.jobs
          jobs should have size (1)
          jobs.head.name should equal("job2")        
        case _ => fail
      }
    }

    it("should return Completed if all stage finished") {
      val job = createJob("job1")
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1", job)))
      val build = new Build(pipeline)
      build.complete(job) match {
        case Some(Build.Completed) =>
        case _ => fail
      }
    }

    it("should return Failed if any job failed") {
      val job1 = createJob("job1")
      val job2 = createJob("job2")
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1", job1), createStage("stage1", job2)))
      val build = new Build(pipeline)
      build.fail(job1) match {
        case Some(Build.Failed) =>
        case _ => fail
      }
    }

    it("should wait all job finish if any of the job failed") {
      val job1 = createJob("job1")
      val job2 = createJob("job2")
      val job3 = createJob("job3")
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1", job1, job2), createStage("stage1", job3)))

      val build = new Build(pipeline)
      build.fail(job1) match {
        case None =>
        case _ => fail
      }
      build.complete(job2) match {
        case Some(Build.Failed) =>
        case _ => fail
      }
    }

    it("should cancel current stage when receive any cancel message") {
      val job1 = createJob("job1")
      val job2 = createJob("job2")
      val pipeline = new Pipeline("pipeline", null, List(createStage("stage1", job1), createStage("stage2", job2)))

      val build = new Build(pipeline)
      build.cancel(Set(job1)) match{
        case Some(Build.Canceled) =>
        case _ => fail
      }
    }
  }

  def createJob(name: String): Job = new Job(name, Set[String](), List[Task]())

  def createStage(name: String): Stage =new  Stage(name, Set(createJob(name + " job")))

  def createStage(name: String, job: Job*): Stage = new Stage(name, job.toSet)
}