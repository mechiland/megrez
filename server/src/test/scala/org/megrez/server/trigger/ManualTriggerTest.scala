package org.megrez.server.trigger

import scala.actors._
import Actor._
import org.scalatest.Spec
import org.scalatest._
import mock.MockitoSugar
import org.scalatest.matchers._
import org.megrez.server._
import org.megrez.server.Exit
import main.scala.org.megrez.server.trigger.ManualTrigger
import org.mockito.Mockito._

class ManualTriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Manual trigger") {
    it("should send trigger message after clicked") {
      trigger !? "click" match {
        case _: Success =>
        case msg: Any => println(msg); fail
      }
      receiveWithin(2000) {
        case _: TriggerBuild =>
        case TIMEOUT => fail
        case msg: Any => println(msg); fail
      }
    }
  }

  var trigger: Actor = _

  override def beforeEach() {
    val pipeline: Pipeline = mock[Pipeline]
    when(pipeline.checkChange).thenReturn(true)
    trigger = new ManualTrigger(pipeline, self)
    trigger start
  }

  override def afterEach() {
    trigger ! Exit()
  }
}