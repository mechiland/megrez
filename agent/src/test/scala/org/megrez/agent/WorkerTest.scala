package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import java.io.File
import actors.TIMEOUT
import actors.Actor._

class WorkerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Agent worker job execution") {
    it("should init pipeline folder under working dir") {
      val pipelineId = "pipeline"
      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[WorkingDirectory]
      val versionControl = mock[VersionControl]
      val workSet : Any = 5
      val job = new Job(List(new EmptyTask))

      when(workingDirectory.getDir(pipelineId)).thenReturn(null)
      when(workingDirectory.makeDir(pipelineId)).thenReturn(pipelineDir)      
      
      val worker = new Worker(workingDirectory)
      worker.start

      worker ! JobAssignment("pipeline", versionControl, workSet, job)
      
      receiveWithin(1000) {
        case complete : JobCompleted =>
          complete.pipelineId should equal(pipelineId)
          complete.workSet should equal(workSet)
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }

      verify(workingDirectory).makeDir(pipelineId)
      verify(versionControl).initRepository(pipelineDir, workSet)
    }

    it("should checkout work set if pipeline already exist") {
      val pipelineId = "pipeline"
      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[WorkingDirectory]
      val versionControl = mock[VersionControl]
      val workSet : Any = 5
      val job = new Job(List(new EmptyTask))

      when(workingDirectory.getDir(pipelineId)).thenReturn(pipelineDir)

      val worker = new Worker(workingDirectory)
      worker.start

      worker ! JobAssignment("pipeline", versionControl, workSet, job)

      receiveWithin(1000) {
        case complete : JobCompleted =>
          complete.pipelineId should equal(pipelineId)
          complete.workSet should equal(workSet)
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }

      verify(workingDirectory, never).makeDir(pipelineId)      
      verify(versionControl).checkout(pipelineDir, workSet)
      verify(versionControl, never).initRepository(pipelineDir, workSet)
    }
  }
}

class EmptyTask extends Task
