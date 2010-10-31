package scala.org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import java.lang.String
import org.megrez.server.util.CommandUtil
import org.megrez.server.trigger.SvnTestRepo

class SvnTestRepoTest extends Spec with ShouldMatchers with SvnTestRepo {
  describe("Svn test repo") {
    it("should create test repo on setup") {
      setupSvnRepo
      val output: String = CommandUtil.run("svn info " + svnUrl).mkString
      output.contains("Revision: 0") should be === true
      teardownSvnRepo
    }

    it("should delete test repo on teardown") {
      setupSvnRepo
      teardownSvnRepo
      repoDir.exists should be === false
    }

    it("should add revision after commit") {
      setupSvnRepo
      makeNewCommit
      val output: String = CommandUtil.run("svn info " + svnUrl).mkString
      output.contains("Revision: 1") should be === true
      teardownSvnRepo
    }
  }
}
