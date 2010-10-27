package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import Method._
class RequestTest extends Spec with ShouldMatchers {
  describe("parse from json") {
    it("parse pipeline") {
      val json = """{"pipeline" : {"name" : "pipeline1", "vcs" : {"type" : "svn", "url" : "svn_url"}}}"""
      val pipelineConfig = Request(GET, "/pipelines", json).resource;
	  pipelineConfig.name should be === "pipeline1"
	  pipelineConfig.material.url should be === "svn_url"
    }
  }
}