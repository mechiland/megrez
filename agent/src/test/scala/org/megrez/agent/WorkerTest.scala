package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import java.io.File
import actors.TIMEOUT
import actors.Actor._
import vcs.VersionControl

class WorkerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Agent worker job execution") {
    it("should init pipeline folder under working dir") {
      val pipelineId = "pipeline"
      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      val versionControl = mock[VersionControl]
      val workSet : Any = 5
      val job = new Job(List(new EmptyTask))

      when(workingDirectory.getPipelineFolder(pipelineId)).thenReturn(null)
      when(workingDirectory.createPipelineFolder(pipelineId)).thenReturn(pipelineDir)
      
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

      verify(workingDirectory).createPipelineFolder(pipelineId)
      verify(versionControl).checkout(pipelineDir, workSet)
    }

    it("should checkout work set if pipeline already exist") {
      val pipelineId = "pipeline"
      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      val versionControl = mock[VersionControl]
      val workSet : Any = 5
      val job = new Job(List(new EmptyTask))

      when(workingDirectory.getPipelineFolder(pipelineId)).thenReturn(pipelineDir)
      when(versionControl.checkWorkingDir(pipelineDir)).thenReturn(true)

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

      verify(workingDirectory, never).createPipelineFolder(pipelineId)
      verify(versionControl).checkout(pipelineDir, workSet)
    }

    it("should re-create dir if pipeline dir is not a valid dir") {
      val pipelineId = "pipeline"
      val pipelineDir = new File("pipeline")
      val worksapce = mock[Workspace]
      val versionControl = mock[VersionControl]
      val workSet : Any = 5
      val job = new Job(List(new EmptyTask))

      when(worksapce.getPipelineFolder(pipelineId)).thenReturn(pipelineDir)
      when(worksapce.createPipelineFolder(pipelineId)).thenReturn(pipelineDir)
      when(versionControl.checkWorkingDir(pipelineDir)).thenReturn(false)      

      val worker = new Worker(worksapce)
      worker.start

      worker ! JobAssignment("pipeline", versionControl, workSet, job)

      receiveWithin(1000) {
        case complete : JobCompleted =>
          complete.pipelineId should equal(pipelineId)
          complete.workSet should equal(workSet)
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }

      verify(worksapce).removePipelineFolder(pipelineId)
      verify(worksapce).createPipelineFolder(pipelineId)
      verify(versionControl).checkout(pipelineDir, workSet)
    }

    it("should run tasks") {
      val pipelineId = "pipeline"
      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      val versionControl = mock[VersionControl]
      val workSet : Any = 5
      val task1 = mock[Task]
      val task2 = mock[Task]
      
      val job = new Job(List(task1, task2))

      when(workingDirectory.getPipelineFolder(pipelineId)).thenReturn(pipelineDir)
      when(versionControl.checkWorkingDir(pipelineDir)).thenReturn(true)

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

      verify(task1).run(same(pipelineDir))
      verify(task2).run(same(pipelineDir))
    }
  }
}

class EmptyTask extends Task {
  def run(file: File) {}
}
