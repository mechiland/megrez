package org.megrez.server

import actors._
import scala.actors.Actor._
import org.scalatest._
import org.scalatest.matchers._

class AgentTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Agent receives job") {
    it("should confirm job if idle") {
      agent !? Job() match {
        case message: JobConfirm => message.agent.status should be === AgentStatus.Busy
        case _ => fail
      }
    }

    it("should reject job if not idle") {
      busyAgent !? Job() match {
        case message: JobReject => message.agent.status should be === AgentStatus.Busy
        case _ => fail
      }
    }

    def busyAgent() = {
      agent !? new Job() match {case _ =>}
      agent
    }
  }

  describe("Agent metadata management") {
    it("should assign tags to agent") {
      agent !? SetTags(Set("windows")) match {
        case _: Success =>
        case _ => fail
      }
      agent.tags should have size (1)
      agent.tags should contain("windows") 
    }
  }

  var agent: Agent = _

  override def beforeEach() {
    agent = new Agent()
    agent start
  }

  override def afterEach() {
    agent ! Exit()
  }
}