package org.megrez.server

import org.scalatest.mock.MockitoSugar
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import actors.Actor._
import actors.TIMEOUT
import org.megrez.server.Pipeline.Stage
import java.util.UUID

class BuildSchedulerTest extends Spec with ShouldMatchers with MockitoSugar {
  def createStage(name: String, job: Job) = new Stage(name, Set[Job](job))

  describe("Build scheduler") {
    it("should send the first job in pipeline to dispatcher") {
      val job = new Job("job1", Set[String](), List[Task]())
      val config = new Pipeline("pipeline", null, List(createStage("unit test", job)))

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

    it("should send jobs from next stage if first stage completed") {
      val job1 = new Job("job1", Set[String](), List[Task]())
      val job2 = new Job("job2", Set[String](), List[Task]())
      val config = new Pipeline("pipeline", null, List(createStage("unit test", job1), createStage("unit test", job2)))

      val scheduler = new BuildScheduler(self)
      scheduler ! TriggerBuild(config)

      receiveWithin(1000) {
        case JobScheduled(build: UUID, jobs: Set[Job]) =>
          scheduler  ! JobCompleted(build, job1)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case JobScheduled(build: UUID, jobs: Set[Job]) =>
          jobs should have size (1)
          jobs should contain(job2)          
        case TIMEOUT => fail
        case _ => fail
      }

    }
  }
}