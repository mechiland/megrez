package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import java.io.File

class FileWorkspaceTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  
  describe("File Workspace") {
    it("should create dir for pipeline") {
      val workspace = new FileWorkspace(root)
      workspace.createPipelineFolder("pipeline")
      
      new File(root, "pipeline") should be('exists)
    }

    it("should returen folder if exists") {
      new File(root, "pipeline").mkdir
      val workspace = new FileWorkspace(root)
      workspace.getPipelineFolder("pipeline") should not be(null)
    }

    it("should return null if folder doesn't exist") {
      val workspace = new FileWorkspace(root)
      workspace.getPipelineFolder("pipeline") should be(null)      
    }

    it("should remove all sub folder under pipeline dir") {
      val workspace = new FileWorkspace(root)
      val pipelineDir = workspace.createPipelineFolder("pipeline")
      new File(pipelineDir, "sub").mkdir

      workspace.removePipelineFolder("pipeline")
      pipelineDir should not be ('exists)
    }
  }

  val root = new File(System.getProperty("user.dir"), "root")

  override def beforeEach() {
    root.mkdirs
  }

  override def afterEach() {
    delete(root)
  }

  def delete(file : File) {
    file.listFiles.foreach {file =>
      if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}