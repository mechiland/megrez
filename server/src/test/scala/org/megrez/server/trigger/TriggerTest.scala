package scala.org.megrez.server.trigger

import scala.actors._
import Actor._
import main.scala.org.megrez.server.trigger.VersionControl
import org.scalatest.Spec
import org.megrez.server.trigger.Trigger
import org.scalatest._
import org.scalatest.matchers._
import org.megrez.server._
import org.megrez.server.Exit

class TriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  
  describe("shoud call versionControl methods") {
    it("when build revision is setting") {
      trigger ! "click"
      receive {
        case msg: TriggerMessage => println("success")
        case _ => fail
      }
    }
  }

  var trigger: Actor = _

  override def beforeEach() {
    val vc: VersionControlMocker = new VersionControlMocker(new PipelineConfig("pipeline1", new SvnMaterial("url"), List()))
    trigger = new Trigger(vc, self)
    trigger start
  }

  override def afterEach() {
    trigger ! Exit()
  }
}

class VersionControlMocker(val pipeline: PipelineConfig) extends VersionControl {
  def checkChange() = {needTriggerScheduler = true}

  def getChange(): TriggerMessage = {
    if (needTriggerScheduler)
      return new TriggerMessage(pipeline.name, "1")
    return null
  }
}