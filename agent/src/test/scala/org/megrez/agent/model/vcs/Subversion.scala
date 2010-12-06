package org.megrez.agent.model.vcs

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

class SubversionTest extends Spec with ShouldMatchers {
  describe("Subversion") {
    it("should be equal if url matches") {
      new Subversion("svn_url") should equal(new Subversion("svn_url"))
    }
  }
}