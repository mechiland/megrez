package org.megrez.server.trigger

import org.scalatest.Spec
import org.megrez.{Pipeline, Material, ChangeSource}
import java.io.File
import org.megrez.util.Workspace
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import actors.Actor._
import actors.TIMEOUT
import org.megrez.server.TrigBuild
import org.scalatest.matchers.ShouldMatchers

class OnChangesTest extends Spec with ShouldMatchers with MockitoSugar {
  describe("OnChanges Trigger") {
    it("should notify build scheduler if materials changed") {
      val source = mock[ChangeSource]
      val material = new Material(source)
      val pipeline = new Pipeline("pipeline", Set(material), List[org.megrez.Pipeline.Stage]())

      val workspace = mock[Workspace]
      val workingDir = new File("workingdir")

      when(workspace.createFolder("pipeline")).thenReturn(workingDir)
      when(source.changes(workingDir, None)).thenReturn(Some(5))
      when(source.changes(workingDir, Some(5))).thenReturn(Some(6))

      val materials = new Materials(pipeline, workspace)
      
      val trigger = new OnChanges(materials, self, 500)

      trigger.start

      receiveWithin(100) {
        case TrigBuild(pipeline : Pipeline, changes : Map[Material, Option[Any]]) =>
          pipeline should be === pipeline
          changes should have size(1)
          val (source, workset) = changes.head
          source should be === material
          workset should equal(Some(5))
        case TIMEOUT =>
          trigger.stop
          fail
        case _ =>
          trigger.stop
          fail
      }

      receiveWithin(500) {
        case TrigBuild(pipeline : Pipeline, changes : Map[Material, Option[Any]]) =>
          pipeline should be === pipeline
          changes should have size(1)
          val (source, workset) = changes.head
          source should be === material
          workset should equal(Some(6))
          trigger.stop
        case TIMEOUT =>
          trigger.stop
          fail
        case _ =>
          trigger.stop
          fail
      }
    }
  }
}