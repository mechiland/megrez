package scala.org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.server.Pipeline
import java.util.Calendar
import org.megrez.server.trigger.Git
import java.lang.String


class GitTest extends Spec with ShouldMatchers {
  describe("should get latest repository revision") {
    it("from Git") {
      val gitUrl: String = "git@github.com/vincentx/megrez.git"
      val userDir: String = System.getProperty("user.dir").split("/megrez")(0)
      val date: Calendar = Calendar.getInstance()
      date.set(2009, 1, 1)
      val pipeline: Pipeline = new Pipeline("1", gitUrl, date,userDir)
      val git: Git = new Git(pipeline)

      git.checkChange
      git.getChange.pipelineName should be === "1"
    }
  }
}