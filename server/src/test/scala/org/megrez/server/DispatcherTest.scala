package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor
import actors.Actor._
import java.util.UUID
class DispatcherTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
  def newJob: Job = {
    new Job("unit test", Set(), List())
  }

  describe("Dispatcher") {
    it("should assign job to idle agent") {
      dispatcher !? AgentConnect(agent) match {
        case _: Success =>
        case msg: Any => println(msg); fail
      }
      dispatcher !? JobScheduled(UUID.randomUUID, Set(newJob)) match {
        case _: Success =>
        case msg: Any => println(msg); fail
      }
      expectAgentGotJob
    }
  }

  //  describe("Dispatcher receives job finished") {
  //    it("should schedule next stage after job finished") {
  //      scheduler !? AgentConnect(agent) match {case _ =>}
  //      scheduler !? TriggerMessage("pipeline1", "#1") match {case _ =>}
  //
  //      agent !? JobFinished(agent, "pipeline1", "stage1", "#1") match {case _ =>}
  //
  //      expectAgentGotJob
  //    }
  //  }


  var agent: Agent = _
  var dispatcher: Dispatcher = _

  override def beforeEach() {
    dispatcher = new Dispatcher();
    agent = new Agent(new ActorBasedAgentHandler(self), dispatcher)
    agent start;
    dispatcher start;
  }

  override def afterEach() {
    agent ! Exit();
    dispatcher ! Exit();
  }
}
