package org.megrez.agent

import actors.Actor
import java.io.File
import java.lang.String

class Worker(val workspace: Workspace) extends Actor {
  def act() {
    loop {
      react {
        case assignment: JobAssignment => handleJob(assignment)
      }
    }
  }

  private def handleJob(assignment: JobAssignment) {
    val pipelineDir = workspace.getPipelineFolder(assignment.pipelineId) match {
      case null =>
        workspace.createPipelineFolder(assignment.pipelineId)        
      case dir : File =>
        if (!assignment.versionControl.checkWorkingDir(dir)) {
          workspace.removePipelineFolder(assignment.pipelineId)
          workspace.createPipelineFolder(assignment.pipelineId)
        } else dir
    }
    assignment.versionControl.checkout(pipelineDir, assignment.workSet)
    assignment.job.tasks.foreach(_ run)
    reply(JobCompleted(assignment.pipelineId, assignment.workSet))
  }
}

trait Workspace {
  def getPipelineFolder(pipelineId: String): File
  def createPipelineFolder(pipelineId: String): File
  def removePipelineFolder(pipelineId: String)
}

class FileWorkspace(val root : File) extends Workspace {
  override def getPipelineFolder(pipelineId : String) : File = {
    val pipeline = new File(root, pipelineId)
    if (pipeline.exists) pipeline else null 
  }

  override def createPipelineFolder(pipelineId : String) : File = {
    val pipeline = new File(root, pipelineId)
    pipeline.mkdirs
    pipeline
  }


  def removePipelineFolder(pipelineId: String) {
    delete(getPipelineFolder(pipelineId))
  }

  private def delete(file : File) {
    file.listFiles.foreach {file =>
      if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}
