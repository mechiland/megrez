package org.megrez.server.http

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import actors.Actor._
import actors.TIMEOUT
import org.megrez.server.ToPipelineManager
import org.megrez.util.JSON
import org.megrez.{Artifact, Pipeline}

class PipelineControllerTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("receive request") {
    it("handle POST request") {
      val pipelineController = new PipelineController(self)
      val content = """pipeline=%7B%22name%22%3A%22pipeline%22%2C%22materials%22%3A%5B%7B%22type%22%3A%22svn%22%2C%22url%22%3A%22svn_url%22%2C%22dest%22%3A%22dest%22%7D%5D%2C%22stages%22%3A%5B%7B%22name%22%3A%22stage%22%2C%22jobs%22%3A%5B%7B%22name%22%3A%22job%22%2C%22resources%22%3A%5B%22LINUX%22%5D%2C%22tasks%22%3A%5B%7B%22type%22%3A%22cmd%22%2C%22command%22%3A%22ls%22%7D%5D%2C%22artifacts%22%3A%5B%7B%22path%22%3A%22target%22%2C%22tags%22%3A%5B%22artifact%22%5D%7D%5D%7D%5D%7D%5D%7D"""
      val json = """{"name":"pipeline","materials":[{"type":"svn","url":"svn_url","dest":"dest"}],"stages":[{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"target","tags":["artifact"]}]}]}]}"""
      val future = pipelineController !! Request(Method.POST, "/pipelines", content)

      receiveWithin(1000) {
        case ToPipelineManager.AddPipeline(pipeline: Pipeline) =>
          val artifact: Artifact = pipeline.stages.head.jobs.head.artifacts.head
          JSON.write(pipeline) should equal(json)
        case TIMEOUT => fail
        case _ => fail
      }

      future() should equal(HttpResponse.OK)
    }
  }
}