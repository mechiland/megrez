package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

class JobAssignmentTest extends Spec with ShouldMatchers {
  describe("parse from json") {
    it("parse job assignment for pipeline using svn") {
      val json = """{"pipeline" : {"id" : "pipeline", "vcs" : {"type" : "svn", "url" : "svn_url"}},
                     "workSet"  : {"revision" : "100"},
                     "job"      : {"tasks" : [] } }"""
      val assignment = JobAssignment.parse(json)
      assignment.pipelineId should equal("pipeline")
      assignment.versionControl.toString should equal("svn : svn_url")      
      assignment.job.tasks.size should equal(0)
      assignment.workSet should equal(100)
    }
  }
}