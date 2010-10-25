package org.megrez.agent

import actors.Actor
import java.io.File

class Worker(val workingDirectory: WorkingDirectory) extends Actor {
  def act() {
    loop {
      react {
        case assignment: JobAssignment => handleJob(assignment)
      }
    }
  }

  private def handleJob(assignment: JobAssignment) {
    val pipelineDir = workingDirectory.getDir(assignment.pipelineId) match {
      case null =>
        val dir = workingDirectory.makeDir(assignment.pipelineId)
        assignment.versionControl.initRepository(dir, assignment.workSet)
        dir
      case dir : File =>
        assignment.versionControl.checkout(dir, assignment.workSet)
        dir
    }
    reply(JobCompleted(assignment.pipelineId, assignment.workSet))
  }
}

trait VersionControl {
  def initRepository(workingDir: File, workSet : Any)
  def checkout(workingDir: File, workSet : Any)
}

trait WorkingDirectory {
  def getDir(pipelineId: String): File
  def makeDir(pipelineId: String): File
}