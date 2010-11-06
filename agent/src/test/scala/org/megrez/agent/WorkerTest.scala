package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, BeforeAndAfterEach}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import java.io.File
import actors.TIMEOUT
import actors.Actor._
import org.megrez._
import util.Workspace

class WorkerTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
  describe("Agent worker job execution") {
    it("should init pipeline folder under working dir") {
      val pipeline = "pipeline"
      val version = mock[org.megrez.vcs.VersionControl]
      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(version, "$main") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getFolder(pipeline)).thenReturn(null)
      when(workingDirectory.createFolder(pipeline)).thenReturn(pipelineDir)

      val worker = new Worker(workingDirectory)

      worker.start

      worker ! assignment

      receiveWithin(1000) {
        case completed: JobCompleted =>          
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

      val assignment = JobAssignment(pipeline, Map(new Material(version, "$main") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getFolder(pipeline)).thenReturn(pipelineDir)
      when(version.isRepository(pipelineDir)).thenReturn(true)

      val worker = new Worker(workingDirectory)
      worker.start

      worker ! assignment

      receiveWithin(1000) {
        case completed: JobCompleted =>          
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

      val assignment = JobAssignment(pipeline, Map(new Material(version, "$main") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getFolder(pipeline)).thenReturn(pipelineDir)
      when(workingDirectory.createFolder(pipeline)).thenReturn(pipelineDir)      
      when(version.isRepository(pipelineDir)).thenReturn(false)

      val worker = new Worker(workingDirectory)
      worker.start

      worker ! assignment

      receiveWithin(1000) {
        case complete: JobCompleted =>
          verify(workingDirectory).removeFolder(pipeline)
          verify(workingDirectory, times(2)).createFolder(pipeline)
          verify(version).checkout(pipelineDir, Some(5))
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }
    }

    it("should update for mulit materials") {
      val pipeline = "pipeline"
      val materialA = mock[org.megrez.vcs.VersionControl]
      val materialB = mock[org.megrez.vcs.VersionControl]

      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(materialA, "destA") -> Some(5),
        new Material(materialB, "destB") -> Some(4)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val materialADir = new File(pipelineDir, "/destA")
      val materialBDir = new File(pipelineDir, "/destB")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.createFolder(pipeline)).thenReturn(pipelineDir)
      when(workingDirectory.createFolder(pipeline + "/destA")).thenReturn(materialADir)
      when(workingDirectory.createFolder(pipeline + "/destB")).thenReturn(materialBDir)

      val worker = new Worker(workingDirectory)
      worker.start
      worker ! assignment

      receiveWithin(1000) {
        case completed: JobCompleted =>
          verify(materialA).checkout(materialADir, Some(5))
          verify(materialB).checkout(materialBDir, Some(4))
          verify(task).execute(pipelineDir)
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }
    }

    it("should reply job failed if any material fail to update") {
      val pipeline = "pipeline"
      val version = mock[org.megrez.vcs.VersionControl]
      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(version, "$main") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getFolder(pipeline)).thenReturn(pipelineDir)
      when(workingDirectory.createFolder(pipeline)).thenReturn(pipelineDir)      
      when(version.isRepository(pipelineDir)).thenReturn(true)
      when(version.update(pipelineDir, Some(5))).thenThrow(new RuntimeException("error check out"))

      val worker = new Worker(workingDirectory)
      worker.start
      worker ! assignment

      receiveWithin(1000) {
        case failed: JobFailed =>
          failed.reason should equal("error check out")
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }      
    }

    it("should reply job failed if any task fail to update") {
      val pipeline = "pipeline"
      val version = mock[org.megrez.vcs.VersionControl]
      val task = mock[org.megrez.Task]

      val assignment = JobAssignment(pipeline, Map(new Material(version, "$main") -> Some(5)), new Job("test", List(task)))

      val pipelineDir = new File("pipeline")
      val workingDirectory = mock[Workspace]
      when(workingDirectory.getFolder(pipeline)).thenReturn(pipelineDir)
      when(workingDirectory.createFolder(pipeline)).thenReturn(pipelineDir)
      when(version.isRepository(pipelineDir)).thenReturn(true)
      when(task.execute(pipelineDir)).thenThrow(new RuntimeException("task error"))

      val worker = new Worker(workingDirectory)
      worker.start
      worker ! assignment

      receiveWithin(1000) {
        case failed: JobFailed =>
          failed.reason should equal("task error")
        case TIMEOUT => fail("Timeout")
        case _ => fail
      }
    }
  }
}
