package org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.server.util.CommandUtil

class GitTestRepoTest extends Spec with ShouldMatchers with GitTestRepo {
  describe("Git test repo") {
    it("should create test repo on setup") {
      setupGitRepo
      val output: String = CommandUtil.run("git log", repoDir).mkString
      output.contains("commit") should be === true
      teardownGitRepo
    }

    it("should delete test repo on teardown") {
      setupGitRepo
      teardownGitRepo
      repoDir.exists should be === false
    }

    it("should add revision after commit") {
      setupGitRepo
      val initialRevision = currentRevision()
      makeNewCommit
      currentRevision should not equal initialRevision
      teardownGitRepo
    }
  }

}