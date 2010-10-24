package scala.org.megrez.server.trigger

import org.scalatest.Spec
import org.megrez.server.trigger.Trigger;
import org.scalatest._
import org.scalatest.matchers._

class TriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
    describe("shoud get latest revision number"){
        it("when repository is SVN"){
          val trigger: Trigger = new Trigger("http://svn.red-bean.com/repos/test/")
          trigger.getLatestVersionForSvn()
          trigger.revision should be === 3
          trigger.needTriggerSVN should be === true
        }
//      it("when repository is Git"){
//          val trigger: Trigger = new Trigger("https://ynjia@github.com/vincentx/megrez.git")
//          trigger.getLatestVersionForGit()
//          trigger.needTriggerGit should be === true
//        }
    }
}