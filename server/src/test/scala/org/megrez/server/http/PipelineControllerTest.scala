package org.megrez.server.http

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import actors.Actor._
import actors.TIMEOUT
import org.megrez.Pipeline
import org.megrez.server.ToPipelineManager
import org.megrez.util.JSON

class PipelineControllerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("receive request") {
    it("handle POST request") {
      val pipelineController = new PipelineController(self)
      val json = """{"name":"pipeline","materials":[{"type":"svn","url":"svn_url","dest":"dest"}],"stages":[{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}]}]}"""
      pipelineController ! Request(Method.POST, "/pipelines", json)

      receiveWithin(1000) {
        case ToPipelineManager.AddPipeline(pipeline : Pipeline) =>
          JSON.write(pipeline) should equal(json)
        case TIMEOUT => fail
        case _ => fail
      }
    }
  }  
}