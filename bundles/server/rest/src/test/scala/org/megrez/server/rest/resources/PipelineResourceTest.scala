package org.megrez.server.rest.resources

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.sun.jersey.api.core.PackagesResourceConfig
import javax.ws.rs.ext.RuntimeDelegate

@RunWith(classOf[JUnitRunner])
class PipelineResourceTest extends Spec with ShouldMatchers {
  describe("Pipeline") {
    it("should return the name of pipeline(dummy test for osgify)") {
//      val config = new PackagesResourceConfig("org.megrez.server.rest.resources", "org.megrez.server.rest.providers")
//      val resource = new PipelineResource
//      resource.show("name") should equal("name")
    }
  }
}