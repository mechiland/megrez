package org.megrez.server.http

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import actors.Actor._
import actors.TIMEOUT
import org.megrez.Pipeline
import org.megrez.server.AddPipeline

class PipelineControllerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("receive request") {
    it("handle POST request") {
      val pipelineController = new PipelineController(self)
      pipelineController ! Request(Method.POST, "/pipelines",
        """{"name" : "pipeline", "materials" : [{"type" : "svn", "url" : "svn_url", "dest" : "dest"}] }""")

      receiveWithin(1000) {
        case AddPipeline(pipeline : Pipeline) =>
          pipeline.name should equal("pipeline")
          pipeline.materials should have size(1)          
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }  
}