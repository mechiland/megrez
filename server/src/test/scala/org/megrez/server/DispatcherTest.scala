package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._
import java.util.UUID
import actors.{Actor, TIMEOUT}
import org.megrez.{JobAssignment, Material, Job}

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  val job = new Job("unit test", Set(), List())

  describe("Dispatcher") {
    it("should assign job to idle agent") {
      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)

      val dispatcher = new Dispatcher(Context)
      dispatcher ! AgentManagerToDispatcher.AgentConnect(self)

      dispatcher ! SchedulerToDispatcher.JobScheduled(UUID.randomUUID, Set(assignment))
      receiveWithin(1000) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
          reply(AgentToDispatcher.Confirm)
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should try next agent if the first one reject the job") {
      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)
      val main = self
      val agent1 = actor {
        react {
          case jobAssignment: JobAssignment =>
            reply(AgentToDispatcher.Reject)
          case TIMEOUT => fail
          case _ => fail
        }
      }

      val agent2 = actor {
        react {
          case jobAssignment: JobAssignment =>
            main ! jobAssignment
            reply(AgentToDispatcher.Confirm)
          case TIMEOUT => fail
          case _ => fail
        }
      }

      val dispatcher = new Dispatcher(Context)
      dispatcher ! AgentManagerToDispatcher.AgentConnect(agent1)
      dispatcher ! AgentManagerToDispatcher.AgentConnect(agent2)

      dispatcher ! SchedulerToDispatcher.JobScheduled(UUID.randomUUID, Set(assignment))

      receiveWithin(1000) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should assign job when idle agent connected") {
      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)

      val dispatcher = new Dispatcher(Context)
      dispatcher ! SchedulerToDispatcher.JobScheduled(UUID.randomUUID, Set(assignment))
      receiveWithin(500) {
        case TIMEOUT =>
        case _: JobAssignment => fail
        case _ => fail
      }

      dispatcher ! AgentManagerToDispatcher.AgentConnect(self)
      receiveWithin(500) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should notify build scheduler job completed") {
      val build = UUID.randomUUID
      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)

      val dispatcher = new Dispatcher(Context)
      dispatcher ! AgentManagerToDispatcher.AgentConnect(self)

      dispatcher ! SchedulerToDispatcher.JobScheduled(build, Set(assignment))
      receiveWithin(1000) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
          reply(AgentToDispatcher.Confirm)
          dispatcher ! AgentToDispatcher.JobCompleted(self, jobAssignment)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case DispatcherToScheduler.JobCompleted(id: UUID, completed: Job) =>
          id should be === build
          completed should be === job
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should notify build scheduler job failed") {
      val build = UUID.randomUUID
      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)

      val dispatcher = new Dispatcher(Context)
      dispatcher ! AgentManagerToDispatcher.AgentConnect(self)

      dispatcher ! SchedulerToDispatcher.JobScheduled(build, Set(assignment))
      receiveWithin(1000) {
        case jobAssignment: JobAssignment =>
          jobAssignment should be === assignment
          reply(AgentToDispatcher.Confirm)
          dispatcher ! AgentToDispatcher.JobFailed(self, jobAssignment)
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case DispatcherToScheduler.JobFailed(id: UUID, completed: Job) =>
          id should be === build
          completed should be === job
        case TIMEOUT => fail
        case _ => fail
      }
    }

    describe("Cancel Build") {
      it("should notify build scheduler canceled jobs") {
        val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), job)
        val assignment2 = JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("function test", Set(), List()))

        val dispatcher = new Dispatcher(Context)
        dispatcher ! AgentManagerToDispatcher.AgentConnect(self)

        val build = UUID.randomUUID
        dispatcher ! SchedulerToDispatcher.JobScheduled(build, Set(assignment))

        receiveWithin(1000) {
          case jobAssignment: JobAssignment =>
            jobAssignment should be === assignment
            reply(AgentToDispatcher.Confirm)
          case TIMEOUT => fail
          case _ => fail
        }

        dispatcher ! SchedulerToDispatcher.JobScheduled(build, Set(assignment2))

        dispatcher ! SchedulerToDispatcher.CancelBuild(build)

        receiveWithin(1000) {
          case DispatcherToScheduler.BuildCanceled(id: UUID, jobAssignments: Set[JobAssignment]) =>
            id should be === build
            jobAssignments.contains(assignment) should be === true
            jobAssignments.contains(assignment2) should be === true
          case TIMEOUT => fail
          case _ => fail
        }

      }
    }
  }

  object Context {
    val buildScheduler = self
  }
}