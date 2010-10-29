package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.server.PipelineConfig.Stage
import java.lang.String
import collection.immutable.Set

class PipelineTest extends Spec with ShouldMatchers {
  describe("Build") {
    it("should return job from stage") {
      val config = new PipelineConfig("pipeline", null, List[PipelineConfig.Stage](createStage("stage1")))

      new Build(config).next match {
        case Some(jobs: Set[Job]) =>
          jobs should have size (1)
          jobs.head.name should equal("stage1 job")
        case None => fail
      }
    }

    it("should return same job if previous job not complet") {
      val config = new PipelineConfig("pipeline", null, List[PipelineConfig.Stage](createStage("stage1"), createStage("stage2")))
      val pipeline = new Build(config)
      pipeline.next.get should have size (1)
      pipeline.next.get should be === pipeline.next.get
    }

    it("should return job from next stage when first stage successful") {
      val config = new PipelineConfig("pipeline", null, List[PipelineConfig.Stage](createStage("stage1"), createStage("stage2")))
      val pipeline = new Build(config)
      pipeline.next.get.foreach(pipeline complete _)
      val jobs = pipeline.next.get
      jobs should have size (1)
      jobs.head.name should equal("stage2 job")
    }

    it("should return None if all stage finished") {
      val config = new PipelineConfig("pipeline", null, List[PipelineConfig.Stage](createStage("stage1")))
      val pipeline = new Build(config)
      pipeline.next.get.foreach(pipeline complete _)
      pipeline.next should be === None
    }

    it("should return None if any job failed") {
      val config = new PipelineConfig("pipeline", null, List[PipelineConfig.Stage](createStage("stage1"), createStage("stage2")))
      val pipeline = new Build(config)
      pipeline.next.get.foreach(pipeline fail _)
      pipeline.next should be === None
    }
  }

  def createStage(name: String): Stage = {
    Stage(name, Set(new Job(name + " job", Set[String](), List[Task]())))
  }  
}