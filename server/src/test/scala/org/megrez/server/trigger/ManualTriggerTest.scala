package scala.org.megrez.server.trigger

import scala.actors._
import Actor._
import org.scalatest.Spec
import org.scalatest._
import org.scalatest.matchers._
import org.megrez.server._
import org.megrez.server.Exit
import main.scala.org.megrez.server.trigger.{ManualTrigger, VersionControl}

class ManualTriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
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
    val vc: VersionControlMocker = new VersionControlMocker(new Pipeline("pipeline1", new SvnMaterial("url"), List()))
    trigger = new ManualTrigger(vc, self)
    trigger start
  }

  override def afterEach() {
    trigger ! Exit()
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
