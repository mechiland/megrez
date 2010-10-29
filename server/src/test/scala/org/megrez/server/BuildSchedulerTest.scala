package org.megrez.server

import org.scalatest.mock.MockitoSugar
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import actors.Actor._
import actors.TIMEOUT
import org.megrez.server.PipelineConfig.Stage
import java.util.UUID

class BuildSchedulerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("Build scheduler") {
    it("should send the first job in pipeline to dispatcher") {
      val job = new Job("job1", Set[String](), List[Task]())
      val config = new PipelineConfig("pipeline", null, List[Stage](new Stage("unit test", Set[Job](job))))

      val scheduler = new BuildScheduler(self)
      scheduler ! TriggerBuild(config)

      receiveWithin(1000) {
        case JobScheduled(build: UUID, jobs: Set[Job]) =>          
          jobs should have size (1)
          jobs should contain(job)
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }
}