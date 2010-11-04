package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import java.io.File
import actors.TIMEOUT
import actors.Actor._
import org.megrez.{Job, JobCompleted, JobAssignment, Material}

class WorkerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  
  describe("Agent worker job execution") {
    it("should init pipeline folder under working dir") {
      val pipeline = "pipeline"
      val version = mock[org.megrez.vcs.VersionControl]
      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(version, "dest") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getPipelineFolder(pipeline)).thenReturn(null)
      when(workingDirectory.createPipelineFolder(pipeline)).thenReturn(pipelineDir)

      val worker = new Worker(workingDirectory)

      worker.start

      worker ! assignment

      receiveWithin(1000) {
        case completed : JobCompleted =>
          verify(workingDirectory).createPipelineFolder(pipeline)
          verify(version).checkout(pipelineDir, Some(5))
          verify(task).execute(pipelineDir)
        case TIMEOUT => fail("Timeout")
        case a: Any =>
          fail
      }
    }

    it("should update work set if pipeline already exist") {
      val pipeline = "pipeline"
      val version = mock[org.megrez.vcs.VersionControl]
      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(version, "dest") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getPipelineFolder(pipeline)).thenReturn(pipelineDir)
      when(version.isRepository(pipelineDir)).thenReturn(true)

      val worker = new Worker(workingDirectory)
      worker.start

      worker ! assignment

      receiveWithin(1000) {
        case completed : JobCompleted =>
          verify(workingDirectory, never).createPipelineFolder(pipeline)
          verify(version, never).checkout(pipelineDir, Some(5))
          verify(version).update(pipelineDir, Some(5))
        case TIMEOUT => fail("Timeout")
        case a: Any =>
          fail
      }
    }

    it("should re-create dir if pipeline dir is not a valid dir") {
      val pipeline = "pipeline"
      val version = mock[org.megrez.vcs.VersionControl]
      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(version, "dest") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getPipelineFolder(pipeline)).thenReturn(pipelineDir)
      when(workingDirectory.createPipelineFolder(pipeline)).thenReturn(pipelineDir)
      when(version.isRepository(pipelineDir)).thenReturn(false)

      val worker = new Worker(workingDirectory)
      worker.start

      worker ! assignment

      receiveWithin(1000) {
        case complete : JobCompleted =>
          verify(workingDirectory).removePipelineFolder(pipeline)
          verify(workingDirectory).createPipelineFolder(pipeline)
          verify(version).checkout(pipelineDir, Some(5))
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }
    }
  }
}