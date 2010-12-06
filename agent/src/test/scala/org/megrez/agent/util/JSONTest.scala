package org.megrez.agent.util

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.task.CommandLineTask
import org.megrez.agent.model.vcs.{Git, Subversion}
import org.megrez.{AgentMessage, JobAssignmentFuture}

class JSONTest extends Spec with ShouldMatchers {
  import JSON._
  describe("Domain object serialization") {
    it("should deserialize svn material") {
      val json = """{"type" : "svn", "url" : "svn_url"}"""
      val subversion = JSON.read[Subversion](json)
      subversion.isInstanceOf[Subversion] should equal(true)
      subversion.asInstanceOf[Subversion].url should equal("svn_url")
    }
  }

  describe("Agent message serialization") {
    it("should deserialize job assignment") {
      val json = """{"materials":[{"material":{"type":"svn","url":"svn_url","dest":"dest"},"workset":{"revision":5}}],"pipeline":"pipeline","tasks":[{"type":"cmd","command":"ls"}],"type":"assignment","buildId":1}"""
      val assignment = JobAssignmentFuture(1, "pipeline", Map((new Subversion("svn_url"), "dest") -> Some(5)), List(new CommandLineTask("ls")))
      JSON.read[AgentMessage](json) should equal(assignment)
    }

    it("should deserialize job assignment for git") {
      val json = """{"materials":[{"material":{"type":"git","url":"git_url","dest":"dest"},"workset":{"commit":"abc"}}],"pipeline":"pipeline","tasks":[{"type":"cmd","command":"ls"}],"type":"assignment","buildId":1}"""
      val assignment = JobAssignmentFuture(1, "pipeline", Map((new Git("git_url"), "dest") -> Some("abc")), List(new CommandLineTask("ls")))
      JSON.read[JobAssignmentFuture](json) should equal(assignment)
    }
  }
}