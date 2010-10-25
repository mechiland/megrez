package org.megrez.agent

import actors.Actor
import java.io.File

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
        val dir = workspace.createPipelineFolder(assignment.pipelineId)
        assignment.versionControl.checkout(dir, assignment.workSet)
        dir
      case dir : File =>
        assignment.versionControl.checkout(dir, assignment.workSet)
        dir
    }
    assignment.job.tasks.foreach(_ run)
    reply(JobCompleted(assignment.pipelineId, assignment.workSet))
  }
}

trait Workspace {
  def getPipelineFolder(pipelineId: String): File
  def createPipelineFolder(pipelineId: String): File
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
}
