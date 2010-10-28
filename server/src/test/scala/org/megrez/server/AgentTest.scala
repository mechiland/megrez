package org.megrez.server

import org.scalatest._
import org.scalatest.matchers._
import actors.Actor._

class AgentTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Agent receives job") {
    it("should confirm job if idle") {
      agent !? JobRequest("pipeline1", "stage1", new Job("unit test", Set(), List())) match {
        case message: JobConfirm => message.agent.status should be === AgentStatus.Busy
        case _ => fail
      }
    }

    it("should reject job if not idle") {
      busyAgent !? JobRequest("pipeline1", "stage1", new Job("unit test", Set(), List())) match {
        case message: JobReject => message.agent.status should be === AgentStatus.Busy
        case _ => fail
      }
    }

    it("should confirm job if resource qualified") {
      agent !? SetResources(Set("windows")) match {
        case _: Success =>
        case _ => fail
      }

      agent !? JobRequest("pipeline1", "stage1", new Job("unit test", Set("windows"), List())) match {
        case message: JobConfirm => message.agent.status should be === AgentStatus.Busy
        case _ => fail
      }
    }

    it("should reject job if resources not qualified") {
      agent !? SetResources(Set("windows")) match {
        case _: Success =>
        case _ => fail
      }
      agent !? JobRequest("pipeline1", "stage1", new Job("unit test", Set("unix"), List())) match {
        case message: JobReject => message.agent.status should be === AgentStatus.Idle
        case _ => fail
      }
    }

    def busyAgent() = {
      agent !? JobRequest("pipeline1", "stage1", new Job("unit test", Set(), List())) match {case _ =>}
      agent
    }
  }

  describe("Agent metadata management") {
    it("should assign resources to agent") {
      agent !? SetResources(Set("windows")) match {
        case _: Success =>
        case _ => fail
      }
      agent.resources should have size (1)
      agent.resources should contain("windows")
    }
  }

  describe("Agent reporting") {
    it("should be idle after job finished") {
      agent !? JobFinished(agent, "pipeline1", "stage1", "#1") match {
        case _: Success =>
        case _ => fail
      }
      agent.status should be === AgentStatus.Idle
    }
  }

  describe("Agent creation") {
    it("should create agent and link agent with agent handler") {
      
    }
  }

  var agent: Agent = _

  override def beforeEach() {
    agent = new Agent(null, self)
    agent start
  }

  override def afterEach() {
    agent ! Exit()
  }
}