package scala.org.megrez.server.trigger

import scala.actors._
import Actor._
import main.scala.org.megrez.server.trigger.VersionControl
import org.scalatest.Spec
import org.megrez.server.trigger.Trigger
import org.scalatest._
import org.scalatest.matchers._
import org.megrez.server._

class TriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("shoud call versionControl methods") {
    it("when build revision is setting") {
      val versionControl: VersionControlMocker = new VersionControlMocker(new PipelineConfig("pipeline1", new SvnMaterial("url"), List()))
      val scheduler = self
      val trigger: Trigger = new Trigger(versionControl,scheduler)
      trigger.start
      trigger ! "haha"
      receive{
        case msg:TriggerMessage =>println("success")
        case _ => fail
      }
    }
  }
}

class VersionControlMocker(val pipeline: PipelineConfig) extends VersionControl {
  def checkChange() = { needTriggerScheduler = true}

  def getChange(): TriggerMessage = {
    if (needTriggerScheduler)
      return new TriggerMessage(pipeline.name, "1")
    return null
  }
}