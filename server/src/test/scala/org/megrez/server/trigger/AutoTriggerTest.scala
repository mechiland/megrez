package org.megrez.server.trigger

import main.scala.org.megrez.server.trigger.VersionControl
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.megrez.server.{SvnMaterial, Pipeline, TriggerMessage}
import scala.actors._
import Actor._

class AutoTriggerTest  extends Spec with ShouldMatchers with BeforeAndAfterEach{
  describe("should trigger schedule automatically"){
    it("when default delay time"){
      receiveWithin(2000) {
        case msg: TriggerMessage => println("success")
        case _ => fail
      }
    }
  }

  var trigger:AutoTrigger =_
   override def beforeEach() {
    val vnc: VersionControlMocker = new VersionControlMocker(new Pipeline("pipeline1", new SvnMaterial("url"), List()))
    trigger = new AutoTrigger(vnc, self)
  }

  override def afterEach() {
    trigger = null
  }
}

class VersionControlMocker(val pipeline: Pipeline) extends VersionControl {
  def checkChange() = {needTriggerScheduler = true}

  def getChange(): TriggerMessage = {
    if (needTriggerScheduler)
      return new TriggerMessage(pipeline.name, "1")
    return null
  }
}