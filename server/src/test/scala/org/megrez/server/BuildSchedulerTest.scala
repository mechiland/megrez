package org.megrez.server

import org.scalatest.mock.MockitoSugar
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import actors.Actor._
import actors.TIMEOUT
import org.megrez.Pipeline.Stage
import java.util.UUID
import org.megrez.{Material, Pipeline, Job, Task}

class BuildSchedulerTest extends Spec with ShouldMatchers with MockitoSugar {
  def createStage(name: String, job: Job*) = new Stage(name, job.toSet)

  describe("Build scheduler") {
    it("should send the first job in pipeline to dispatcher") {
      val job = new Job("job1", Set[String](), List[Task]())
      val pipeline = new Pipeline("pipeline", null, List(createStage("unit test", job)))

      object Context {
        val dispatcher = self
        val buildManager = self
      }

      val scheduler = new BuildScheduler(Context)
      val changes = Map[Material, Option[Any]]()
      
      scheduler ! TrigBuild(pipeline, changes)

      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          jobs should have size (1)
          jobs should contain(job)
          materials should be === changes
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should send jobs from next stage if first stage completed") {
      val job1 = new Job("job1", Set[String](), List[Task]())
      val job2 = new Job("job2", Set[String](), List[Task]())
      val pipeline = new Pipeline("pipeline", null, List(createStage("unit test", job1), createStage("unit test", job2)))

      object Context {
        val dispatcher = self
        val buildManager = self
      }

      val scheduler = new BuildScheduler(Context)
      val changes = Map[Material, Option[Any]]()

      scheduler ! TrigBuild(pipeline, changes)

      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          scheduler ! JobCompleted(build, job1)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          jobs should have size (1)
          jobs should contain(job2)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should fail the build if stage failed") {
      val job1 = new Job("job1", Set[String](), List[Task]())
      val job2 = new Job("job2", Set[String](), List[Task]())
      val pipeline = new Pipeline("pipeline", null, List(createStage("unit test", job1), createStage("unit test", job2)))

      object Context {
        val dispatcher = self
        val buildManager = self
      }

      val scheduler = new BuildScheduler(Context)
      val changes = Map[Material, Option[Any]]()

      scheduler ! TrigBuild(pipeline, changes)

      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          scheduler ! JobFailed(build, job1)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case BuildFailed(build: Build) =>
          build.pipeline should be === pipeline
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should fail the build if all stage jobs failed") {
      val job1 = new Job("job1", Set[String](), List[Task]())
      val job2 = new Job("job2", Set[String](), List[Task]())
      val job3 = new Job("job2", Set[String](), List[Task]())
      val pipeline = new Pipeline("pipeline", null, List(createStage("unit test", job1, job2), createStage("unit test", job3)))

      object Context {
        val dispatcher = self
        val buildManager = self
      }

      val scheduler = new BuildScheduler(Context)
      val changes = Map[Material, Option[Any]]()

      scheduler ! TrigBuild(pipeline, changes)

      var id = UUID.randomUUID
      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          id = build
          scheduler ! JobFailed(build, job1)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(300) {
        case BuildFailed(build: Build) => fail
        case TIMEOUT => scheduler ! JobCompleted(id, job1)
        case _ => fail
      }

      receiveWithin(1000) {
        case BuildFailed(build: Build) =>
          build.pipeline should be === pipeline
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should complete build if all jobs completed") {
      val job1 = new Job("job1", Set[String](), List[Task]())
      val job2 = new Job("job2", Set[String](), List[Task]())
      val pipeline = new Pipeline("pipeline", null, List(createStage("unit test", job1), createStage("unit test", job2)))

      object Context {
        val dispatcher = self
        val buildManager = self
      }

      val scheduler = new BuildScheduler(Context)
      val changes = Map[Material, Option[Any]]()

      scheduler ! TrigBuild(pipeline, changes)

      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          scheduler ! JobCompleted(build, job1)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case JobScheduled(build: UUID, materials : Map[Material, Option[Any]], jobs: Set[Job]) =>
          scheduler ! JobCompleted(build, job2)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case BuildCompleted(build: Build) =>
          build.pipeline should be === pipeline
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }
}