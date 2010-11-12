package org.megrez.agent

import actors.Actor
import java.io.File
import org.megrez.vcs.VersionControl
import org.megrez.{JobFailed, JobCompleted, JobAssignment}
import org.megrez.util.Workspace

class Worker(val workspace: Workspace) extends Actor {
  def act() {
    loop {
      react {
        case assignment: JobAssignment =>
          try {
            val pipelineDir = workspace.createFolder(assignment.pipeline)
            updateMaterials(assignment)
            val result: String = assignment.job.tasks.map(_ execute pipelineDir).mkString
            reply(new JobCompleted(result))
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

