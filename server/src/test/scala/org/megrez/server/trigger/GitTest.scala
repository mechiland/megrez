package org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import java.util.Calendar
import java.lang.String
import org.megrez.server.{GitMaterial, Pipeline}
import org.megrez.server.trigger.{GitTestRepo, Git}
import org.scalatest.{BeforeAndAfterEach, Spec}
import java.io.File
import org.megrez.server.util.{Utils, EnvUtil}

class GitTest extends Spec with ShouldMatchers with BeforeAndAfterEach with GitTestRepo {
  describe("Git") {
    it("should detect change for the first time") {
      git.checkChange should be === true
    }
    it("should not detect change if no new check in") {
      git.checkChange
      git.checkChange should be === false
    }
    it("should detect change given new check in") {
      git.checkChange
      makeNewCommit
      git.checkChange should be === true
    }
  }

  var git: Git = _

  override def beforeEach() {
    setupGitRepo
    val pipeline: Pipeline = new Pipeline("pipeline1", new GitMaterial(gitUrl), List()) {
      override def workingDir() = {
        new File(EnvUtil.tempDir(), Utils.aRandomName)
      }
    }
    git = new Git(pipeline)
  }

  override def afterEach() {
    teardownGitRepo
  }
}