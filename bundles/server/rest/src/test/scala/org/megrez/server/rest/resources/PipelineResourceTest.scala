package org.megrez.server.rest.resources

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class PipelineResourceTest extends Spec with ShouldMatchers {
  describe("Pipeline") {
    it("should return the name of pipeline(dummy test for osgify)") {
      val resource = new PipelineResource
      resource.show("name") should equal("name")
    }
  }
}