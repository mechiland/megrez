package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor

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

      Thread.sleep(500)
      agent.status should be === AgentStatus.Busy
      agent.job.stage should be === "stage1"
    }
  }

  describe("Scheduler receives job finished") {
    it("should schedule next stage after job finished") {
      scheduler !? AgentConnect(agent) match {case _ =>}
      scheduler !? TriggerMessage("pipeline1", "#1") match {case _ =>}

      agent.finishJob
      scheduler !? JobFinished(agent, "pipeline1", "stage1", "#1") match {case _ =>}

      Thread.sleep(500)
      agent.status should be === AgentStatus.Busy
      agent.job.stage should be === "stage2"
    }
  }

  class AgentStub extends Actor {
    import AgentStatus._

    private var _status = Idle
    var job: JobRequest = null

    def act() {
      loop {
        react {
          case newjob: JobRequest => handleJob(newjob)
          case _: Exit => exit
        }
      }
    }

    private def handleJob(newjob: JobRequest){
      _status = Busy
      job = newjob
    }

    def status = _status
    def finishJob = _status = Idle
  }

  var agent: AgentStub = _
  var scheduler: Scheduler = _

  override def beforeEach() {
    agent = new AgentStub()
    scheduler = new Scheduler();
    agent start;
    scheduler start;
  }

  override def afterEach() {
    agent ! Exit();
    scheduler ! Exit();
  }
}