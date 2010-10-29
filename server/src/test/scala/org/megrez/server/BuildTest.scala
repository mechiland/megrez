package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.server.PipelineConfig.Stage
import java.lang.String
import collection.immutable.Set

class BuildTest extends Spec with ShouldMatchers {
  describe("Build") {
    it("should return job from stage") {
      val pipeline = new PipelineConfig("pipeline", null, List(createStage("stage1")))

      new Build(pipeline).current.jobs match {
        case Some(jobs: Set[Job]) =>
          jobs should have size (1)
          jobs.head.name should equal("stage1 job")
        case None => fail
      }
    }

    it("should return same job if previous job not complet") {
      val pipeline = new PipelineConfig("pipeline", null, List(createStage("stage1"), createStage("stage2")))
      val build = new Build(pipeline)
      build.current.jobs.get should have size (1)
      build.current.jobs.get should be === build.current.jobs.get
    }

    it("should return job from next stage when first stage successful") {
      val job1 = createJob("job1")
      val job2 = createJob("job2")
      val pipeline = new PipelineConfig("pipeline", null, List(createStage("stage1", job1),
        createStage("stage1", job2)))
      val build = new Build(pipeline)
      build.current.complete(job1) should be === true
      val jobs = build.current.jobs.get
      jobs should have size (1)
      jobs.head.name should equal("job2")
    }

    it("should return Completed if all stage finished") {
      val job = createJob("job1")
      val pipeline = new PipelineConfig("pipeline", null, List(createStage("stage1", job)))
      val build = new Build(pipeline)
      build.current.complete(job)
      build.current should be === Build.Completed
    }

    it("should return Failed if any job failed") {
      val job1 = createJob("job1")
      val job2 = createJob("job2")
      val pipeline = new PipelineConfig("pipeline", null, List(createStage("stage1", job1), createStage("stage1", job2)))
      val build = new Build(pipeline)
      build.current.fail(job1)
      build.current should be === Build.Failed
    }
  }

  def createJob(name: String): Job = new Job(name, Set[String](), List[Task]())

  def createStage(name: String): Stage =  Stage(name, Set(createJob(name + " job")))  

  def createStage(name: String, job : Job): Stage = Stage(name, Set(job))  
}