package scala.org.megrez.server.trigger

import org.scalatest.Spec
import main.scala.org.megrez.server.trigger.Trigger;
import org.scalatest._
import org.scalatest.matchers._

class TriggerTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
    describe("shoud get latest revision number"){
        it("when repository is SVN"){
        new Trigger("http://svn.red-bean.com/repos/test/").getLatestVersionForSvn() should be === 3
        }
    }
}