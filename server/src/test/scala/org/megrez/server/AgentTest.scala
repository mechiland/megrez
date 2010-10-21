package org.megrez.server

import actors._
import scala.actors.Actor._
import org.scalatest._
import org.scalatest.matchers._

class AgentTest extends Spec with ShouldMatchers {
  describe("receiving job") {
    it("should start job if idle") {
      val agent = new Agent()
      agent start

      agent !? new Job() match {
        case message: AgentStateChange => message.state should equal("BUSY")
      }
    }
  }
}