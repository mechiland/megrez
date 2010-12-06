package org.megrez.agent.model.vcs

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

class GitTest extends Spec with ShouldMatchers {
  describe("Git") {
    it("should be equal if url matches") {
      new Git("git_url") should equal(new Git("git_url"))
    }
  }
}