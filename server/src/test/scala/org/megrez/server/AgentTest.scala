package org.megrez.server

import org.scalatest._
import mock.MockitoSugar
import org.scalatest.matchers._
import actors.Actor._
import actors.TIMEOUT
import org.megrez._

class AgentTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Agent") {
    it("should confirm job when job has no resources") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)
      agent !? (1000, JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set(), List()))) match {
        case Some(AgentToDispatcher.Confirm) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }
    }

    it("should confirm job if match material") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)

      agent ! ToAgent.SetResources(Set("LINUX"))      
      agent !? (1000, JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set("LINUX"), List()))) match {
        case Some(AgentToDispatcher.Confirm) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }
    }

    it("should reject job if failed to match material") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)

      agent ! ToAgent.SetResources(Set("LINUX"))
      agent !? (1000, JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set("FIREFOX"), List()))) match {
        case Some(AgentToDispatcher.Reject) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }
    }

    it("should notify dispatcher when job finished") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)

      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set(), List()))
      
      agent !? (1000, assignment) match {
        case Some(AgentToDispatcher.Confirm) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }
      
      agent ! JobCompleted()

      receiveWithin(1000) {
        case AgentToDispatcher.JobCompleted(agent, jobAssignment) =>
          agent should be === agent
          jobAssignment should be === assignment
        case TIMEOUT =>fail
        case _ => fail
      }
    }

    it("should notify dispatcher when job failed") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)

      val assignment = JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set(), List()))

      agent !? (1000, assignment) match {
        case Some(AgentToDispatcher.Confirm) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }

      agent ! JobFailed("reason")

      receiveWithin(1000) {
        case AgentToDispatcher.JobFailed(agent, jobAssignment) =>
          agent should be === agent
          jobAssignment should be === assignment
        case TIMEOUT =>fail
        case _ => fail
      }
    }
  }
}
