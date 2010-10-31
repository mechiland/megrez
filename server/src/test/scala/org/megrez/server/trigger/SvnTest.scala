package org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{SvnMaterial, Pipeline}
import org.scalatest.{BeforeAndAfterEach, Spec}

class SvnTest extends Spec with ShouldMatchers with BeforeAndAfterEach with SvnTestRepo {
  describe("Svn") {
    it("should detect change for the first time") {
      svn.checkChange should be === true
      svn.currentRevision should be === "0"
    }
    it("should not detect change if no new check in") {
      svn.checkChange
      svn.checkChange should be === false
    }
    it("should detect change given new check in") {
      svn.checkChange
      makeNewCommit
      svn.checkChange should be === true
      svn.currentRevision should be === "1"
    }
  }

  var svn: Svn = _

  override def beforeEach() {
    setupSvnRepo
    val pipeline: Pipeline = new Pipeline("pipeline1", new SvnMaterial(svnUrl), List())
    svn = new Svn(pipeline)
  }

  override def afterEach() {
    teardownSvnRepo
  }
}
