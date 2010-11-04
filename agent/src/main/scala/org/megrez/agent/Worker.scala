package org.megrez.agent

import actors.Actor
import java.io.File
import org.megrez.vcs.VersionControl

class Worker(val workspace: Workspace) extends Actor {
  def act() {
    loop {
      react {
        case assignment: JobAssignment => handleJob(assignment)
        case assignment: org.megrez.JobAssignment => handleAssignment(assignment)
      }
    }
  }

  private def handleAssignment(assignment: org.megrez.JobAssignment) {
    val (material, workset) = assignment.materials.head
    material match {
      case versionControl: VersionControl =>
        val pipelineDir = workspace.getPipelineFolder(assignment.pipeline) match {
          case null =>
            val dir = workspace.createPipelineFolder(assignment.pipeline)
            versionControl.checkout(dir, workset)
            dir
          case dir: File if (versionControl.isRepository(dir)) =>
            versionControl.update(dir, workset)
            dir
          case _ : File =>
            workspace.removePipelineFolder(assignment.pipeline)
            val dir = workspace.createPipelineFolder(assignment.pipeline)
            versionControl.checkout(dir, workset)
            dir
        }
        assignment.job.tasks.foreach(_ execute pipelineDir)
        reply(new org.megrez.JobCompleted())
      case _ =>
    }
  }

  private def handleJob(assignment: JobAssignment) {
    val pipelineDir = workspace.getPipelineFolder(assignment.pipelineId) match {
      case null =>
        val dir = workspace.createPipelineFolder(assignment.pipelineId)
        assignment.versionControl.checkout(dir, assignment.workSet)
        dir
      case dir: File if (assignment.versionControl.isRepository(dir)) =>
        assignment.versionControl.update(dir, assignment.workSet)
        dir
      case _: File =>
        workspace.removePipelineFolder(assignment.pipelineId)
        val dir = workspace.createPipelineFolder(assignment.pipelineId)
        assignment.versionControl.checkout(dir, assignment.workSet)
        dir
    }
    assignment.job.tasks.foreach(_ run pipelineDir)
    reply(JobCompleted(assignment.pipelineId, assignment.workSet))
  }
}

trait Workspace {
  def getPipelineFolder(pipelineId: String): File

  def createPipelineFolder(pipelineId: String): File

  def removePipelineFolder(pipelineId: String)
}

class FileWorkspace(val root: File) extends Workspace {
  override def getPipelineFolder(pipelineId: String): File = {
    val pipeline = new File(root, pipelineId)
    if (pipeline.exists) pipeline else null
  }

  override def createPipelineFolder(pipelineId: String): File = {
    val pipeline = new File(root, pipelineId)
    pipeline.mkdirs
    pipeline
  }


  def removePipelineFolder(pipelineId: String) {
    delete(getPipelineFolder(pipelineId))
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}
