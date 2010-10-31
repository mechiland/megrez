package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._
import java.util.UUID
import actors.TIMEOUT

class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  describe("Dispatcher") {
    it("should assign job to idle agent") {
      agentConnect
      scheduleJob(UUID.randomUUID)
      expectAgentGotJob
    }

    it("should notify scheduler about job completed") {
      agentConnect
      val buildId = UUID.randomUUID
      scheduleJob(buildId)
      expectAgentGotJob

      jobFinishedOnAgent(buildId)

      expectJobCompleted
    }
  }

  def newJob: Job = {
    new Job("unit test", Set(), List())
  }

  def agentConnect: Unit = {
    dispatcher !? AgentConnect(agent) match {
      case _: Success =>
      case msg: Any => println(msg); fail
    }
  }

  def scheduleJob(id: UUID): Unit = {
    dispatcher !? JobScheduled(id, Set(newJob)) match {
      case _: Success =>
      case msg: Any => println(msg); fail
    }
  }

  def jobFinishedOnAgent(id: UUID): Unit = {
    agent !? JobFinished(id, newJob, agent) match {
      case _: Success =>
      case msg: Any => println(msg); fail
    }
  }

  def expectJobCompleted: Unit = {
    receiveWithin(2000) {
      case _: JobCompleted =>
      case TIMEOUT => fail
      case msg: Any => println(msg); fail
    }
  }

  var agent: Agent = _
  var dispatcher: Dispatcher = _

  override def beforeEach() {
    dispatcher = new Dispatcher();
    dispatcher.buildScheduler = self
    agent = new Agent(new ActorBasedAgentHandler(self), dispatcher)
    agent start;
    dispatcher start;
  }

  override def afterEach() {
    agent ! Exit();
    dispatcher ! Exit();
  }
}
