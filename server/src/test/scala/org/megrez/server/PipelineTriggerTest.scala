package org.megrez.server

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.scalatest.mock.MockitoSugar
import org.megrez.ChangeSource
import org.mockito.Mockito._
import org.megrez.util.Workspace
import java.io.File

class ChangeSetTest extends Spec with ShouldMatchers with MockitoSugar {
  type Material = org.megrez.Material
  describe("ChangeSet") {
    it("should get change set when any material found changes") {
      val source = mock[ChangeSource]
      val material = new org.megrez.Material(source)
      val pipeline = new org.megrez.Pipeline("pipeline", Set(material), List[org.megrez.Pipeline.Stage]())

      val workspace = mock[Workspace]
      val workingDir = new File("workingdir")

      when(workspace.createFolder("pipeline")).thenReturn(workingDir)
      when(source.changes(workingDir, None)).thenReturn(Some(5))

      val changeSet = new ChangeSet(pipeline, workspace)
      changeSet.get match {
        case Some(changeSet: Map[Material, Option[Any]]) =>
          changeSet should have size (1)
          val (sourceMaterial, workset) = changeSet.head
          sourceMaterial should be === material
          workset should equal(Some(5))
        case _ => fail
      }
    }

    it("should not detect any changes if no changes") {
      val source = mock[ChangeSource]
      val material = new org.megrez.Material(source)
      val pipeline = new org.megrez.Pipeline("pipeline", Set(material), List[org.megrez.Pipeline.Stage]())

      val workspace = mock[Workspace]
      val workingDir = new File("workingdir")

      when(workspace.createFolder("pipeline")).thenReturn(workingDir)
      when(source.changes(workingDir, None)).thenReturn(None)

      val changeSet = new ChangeSet(pipeline, workspace)
      changeSet.get match {
        case None => 
        case _ => fail
      }
    }
  }
}