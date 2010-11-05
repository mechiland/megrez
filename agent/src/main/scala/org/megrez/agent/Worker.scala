package org.megrez.agent

import actors.Actor
import java.io.File
import org.megrez.vcs.VersionControl
import org.megrez.{JobFailed, JobCompleted, JobAssignment}

class Worker(val workspace: Workspace) extends Actor {
  def act() {
    loop {
      react {
        case assignment: JobAssignment =>
          try {
            val pipelineDir = workspace.createFolder(assignment.pipeline)
            updateMaterials(assignment)
            assignment.job.tasks.foreach(_ execute pipelineDir)
            reply(new JobCompleted())
          } catch {
            case e: Exception => reply(new JobFailed(e.getMessage))
          }
        case _ =>
      }
    }
  }

  private def updateMaterials(assignment: JobAssignment) {
    for ((material, workset) <- assignment.materials)
      material.source match {
        case versionControl: VersionControl =>
          val folder = if (material.destination != "$main") assignment.pipeline + "/" + material.destination else assignment.pipeline
          workspace.getFolder(folder) match {
            case null =>
              val dir = workspace.createFolder(folder)
              versionControl.checkout(dir, workset)
            case dir: File if (versionControl.isRepository(dir)) =>
              versionControl.update(dir, workset)
            case dir: File =>
              workspace.removeFolder(folder)
              val dir = workspace.createFolder(folder)
              versionControl.checkout(dir, workset)
          }
        case _ =>
      }
  }
}

trait Workspace {
  def getFolder(pipelineId: String): File

  def createFolder(pipelineId: String): File

  def removeFolder(pipelineId: String)
}

class FileWorkspace(val root: File) extends Workspace {
  override def getFolder(pipelineId: String): File = {
    val pipeline = new File(root, pipelineId)
    if (pipeline.exists) pipeline else null
  }

  override def createFolder(pipelineId: String): File = {
    val pipeline = new File(root, pipelineId)
    if (!pipeline.exists) pipeline.mkdirs
    pipeline
  }


  def removeFolder(pipelineId: String) {
    delete(getFolder(pipelineId))
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}
