package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._

class SchedulerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {

  describe("Scheduler receives trigger") {

    it("should assign job to agent if there is idle agent") {
	  scheduler !? AgentConnect(agent) match {
        case _: Success =>
        case _ => fail
      }
	
      scheduler !? TriggerMessage("pipeline1", "#1") match {
        case _: Success =>
        case _ => fail
      }

	  agent !? JobRequest(new Job("unit test", Set(), List())) match {
        case message: JobReject => message.agent.status should be === AgentStatus.Busy
        case _ => fail
      }
    }
  }

  var agent: Agent = _
  var scheduler: Scheduler = _

  override def beforeEach() {	
	agent = new Agent()
    scheduler = new Scheduler();
	agent start;
    scheduler start;
  }

  override def afterEach() {
    agent ! Exit();
    scheduler ! Exit();
  }
}