package org.megrez.server.trigger

import org.scalatest.mock.MockitoSugar
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.megrez.{Pipeline, Material, ChangeSource}
import java.io.File
import org.megrez.util.Workspace
import org.mockito.Mockito._

class MaterialsTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("Materials") {
    it("should get change set when any material found changes") {
      val source = mock[ChangeSource]
      val material = new Material(source)
      val pipeline = new Pipeline("pipeline", Set(material), List[org.megrez.Pipeline.Stage]())

      val workspace = mock[Workspace]
      val workingDir = new File("workingdir")

      when(workspace.createFolder("pipeline")).thenReturn(workingDir)
      when(source.changes(workingDir, None)).thenReturn(Some(5))

      val materials = new Materials(pipeline, workspace)
      materials.hasChanges should equal(true)
      materials.changes should have size (1)
      val (sourceMaterial, workset) = materials.changes.head
      sourceMaterial should be === material
      workset should equal(Some(5))
    }

    it("should not detect any changes if no changes") {
      val source = mock[ChangeSource]
      val material = new Material(source)
      val pipeline = new Pipeline("pipeline", Set(material), List[org.megrez.Pipeline.Stage]())

      val workspace = mock[Workspace]
      val workingDir = new File("workingdir")

      when(workspace.createFolder("pipeline")).thenReturn(workingDir)
      when(source.changes(workingDir, None)).thenReturn(None)

      val materials = new Materials(pipeline, workspace)
      materials.hasChanges should equal(false)
    }
  }
}