package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor
import actors.Actor._
import scala.concurrent.TIMEOUT

class SchedulerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with AgentTestSuite {
//  describe("Dispatcher receives trigger") {
//
//    it("should assign job to agent if there is idle agent") {
//      scheduler !? AgentConnect(agent) match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      scheduler !? TriggerMessage("pipeline1", "#1") match {
//        case _: Success =>
//        case _ => fail
//      }
//
//      expectAgentGotJob
//    }
//  }
//
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

  var agent: Agent = _
  var scheduler: Dispatcher = _

  override def beforeEach() {
    scheduler = new Dispatcher();
    agent = new Agent(new ActorBasedAgentHandler(self), scheduler)
    agent start;
    scheduler start;
  }

  override def afterEach() {
    agent ! Exit();
    scheduler ! Exit();
  }
}