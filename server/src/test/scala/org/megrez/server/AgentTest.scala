package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._
import actors.{TIMEOUT, Actor}
import java.util.UUID

class AgentTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  describe("Agent") {
    it("should confirm job when job has no resources") {
      agent ! JobRequest(UUID.randomUUID, new Job("unit test", Set(), List()))
      expectAgentConfirmedJob
      agentHandler.sent should be === true
    }

    it("should accept job if succeed to match resource") {
      agent ! SetResources(Set("LINUX", "FIREFOX", "JAVA"))
      agent ! JobRequest(UUID.randomUUID, new Job("unit test", Set("LINUX", "FIREFOX"), List()))
      expectAgentConfirmedJob
    }

    it("should reject job if failed to match resource") {
      agent ! SetResources(Set("LINUX"))
      agent ! JobRequest(UUID.randomUUID, new Job("unit test", Set("LINUX", "FIREFOX"), List()))
      expectAgentRejectedJob
    }

    it("should reject job if the agent is busy") {
      agent ! JobRequest(UUID.randomUUID, new Job("unit test", Set(), List()))
      expectAgentConfirmedJob
      agent ! JobRequest(UUID.randomUUID, new Job("unit test", Set(), List()))
      expectAgentRejectedJob
    }

    it("should send job finish message to dispatcher") {
      agent ! JobFinished(UUID.randomUUID, null, null)
      expectDispatcherReceivedJobFinished
    }


  }

  def expectAgentConfirmedJob {
    receiveWithin(2000) {
      case _: JobConfirm =>
      case TIMEOUT => fail
      case msg: Any => println(msg); fail
    }
  }

  def expectAgentRejectedJob {
    receiveWithin(2000) {
      case _: JobReject =>
      case TIMEOUT => fail
      case msg: Any => println(msg); fail
    }
  }

  def expectDispatcherReceivedJobFinished {
    receiveWithin(2000) {
      case _: JobFinished =>
      case TIMEOUT => fail
      case msg: Any => println(msg); fail
    }
  }

  var agent: Agent = _
  var agentHandler: AgentHandlerStub = _

  override def beforeEach() {
    agentHandler = new AgentHandlerStub()
    agent = new Agent(agentHandler, self)
    agent start
  }

  override def afterEach() {
    agent ! Exit()
  }
}

class AgentHandlerStub() extends AgentHandler {
  var sent = false

  def send(message: String) {
    sent = true
  }

  def assignAgent(agent: Actor) {}
}

trait AgentTestSuite extends Spec {
  def expectAgentGotJob {
    receiveWithin(2000) {
      case "agentGotJob" =>
      case TIMEOUT => fail
      case msg: Any => println(msg); fail
    }
  }

  def expectAgentFinishedJob: Unit = {
    receive {
      case _ =>
    }
  }
}