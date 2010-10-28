package scala.org.megrez.server.trigger

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.server.trigger.Svn
import java.lang.String
import org.megrez.server.{SvnMaterial, PipelineConfig}
import org.megrez.server.TestHelper

class SvnTest extends Spec with ShouldMatchers with TestHelper {
  describe("shoud get latest revision number") {
    it("when repository is SVN") {
      val svnDir: String = megrezParentFolder() + "/megrez/server/src/test/resources/repository/svn"
      val pipeline: PipelineConfig = new PipelineConfig("pipeline1", new SvnMaterial("file://" + svnDir), List())
      val svn: Svn = new Svn(pipeline)
      svn.checkChange()
      svn.getChange.pipelineName should be === "pipeline1"
    }
  }
}