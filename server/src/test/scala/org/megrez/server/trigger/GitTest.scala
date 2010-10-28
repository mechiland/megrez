package scala.org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import java.util.Calendar
import org.megrez.server.trigger.Git
import java.lang.String
import org.megrez.server.{GitMaterial, SvnMaterial, PipelineConfig}

class GitTest extends Spec with ShouldMatchers {
  describe("should get latest repository revision") {
    it("from Git") {
      val gitUrl: String = "git@github.com/vincentx/megrez.git"
      val date: Calendar = Calendar.getInstance()
      date.set(2009, 1, 1)
      val pipeline: PipelineConfig = new PipelineConfig("pipeline1", new GitMaterial(gitUrl), List()) {
        override def workingDir() = {
          System.getProperty("user.dir").split("/megrez")(0)
        }
      }
      val git: Git = new Git(pipeline)

      git.checkChange
      git.getChange.pipelineName should be === "pipeline1"
    }
  }
}