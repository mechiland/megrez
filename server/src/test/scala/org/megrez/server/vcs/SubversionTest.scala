package org.megrez.server.vcs

import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.trigger.SvnTestRepo
import org.scalatest.{BeforeAndAfterEach, Spec}

class SubversionTest extends Spec with ShouldMatchers with BeforeAndAfterEach with SvnTestRepo {
  describe("Subversion") {
    it("should detect change") {
      val subversion = new Subversion(svnUrl)
      subversion.changes match {
        case Some(revision: Int) =>
          revision should equal(0)
        case None => fail("none")
      }
    }

    it("should not detect change if no changes") {
      val subversion = new Subversion(svnUrl)
      subversion.changes
      subversion.changes match {
        case None =>
        case _ => fail
      }
    }
  }

  override protected def beforeEach() {
    setupSvnRepo()
  }

  override def afterEach() {
    teardownSvnRepo
  }
}