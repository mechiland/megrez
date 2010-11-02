package org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, Spec}
import scala.actors._
import Actor._
import org.megrez.server.{TriggerBuild, Pipeline}
import org.scalatest.mock.MockitoSugar

import org.mockito.Mockito._

class AutoTriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Auto trigger") {
    it("should send trigger message after started") {
      receiveWithin(2000) {
        case msg: TriggerBuild =>
        case _ => fail
      }
    }
  }

  var trigger: AutoTrigger = _

  override def beforeEach() {
    val pipeline: Pipeline = mock[Pipeline]
    when(pipeline.checkChange).thenReturn(true)
    trigger = new AutoTrigger(pipeline, self)
  }

  override def afterEach() {
    trigger = null
  }
}