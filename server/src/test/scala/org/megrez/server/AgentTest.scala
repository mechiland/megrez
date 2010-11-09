package org.megrez.server

import org.scalatest._
import mock.MockitoSugar
import org.scalatest.matchers._
import actors.Actor._
import actors.TIMEOUT
import org.megrez.{Material, JobAssignment, Job}

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

    it("should confirm job if match resource") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)

      agent ! ToAgent.SetResources(Set("LINUX"))      
      agent !? (1000, JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set("LINUX"), List()))) match {
        case Some(AgentToDispatcher.Confirm) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }
    }

    it("should reject job if failed to match resource") {
      val handler = mock[AgentHandler]
      val agent = new Agent(handler, self)

      agent ! ToAgent.SetResources(Set("LINUX"))
      agent !? (1000, JobAssignment("pipeline", Map[Material, Option[Any]](), new Job("ut", Set("FIREFOX"), List()))) match {
        case Some(AgentToDispatcher.Reject) =>
        case Some(TIMEOUT) => fail
        case _ => fail
      }
    }
  }
}
