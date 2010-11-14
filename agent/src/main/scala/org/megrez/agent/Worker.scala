package org.megrez.agent

import actors.Actor
import java.io.File
import org.megrez.vcs.VersionControl
import org.megrez.util.{Logging, Workspace}
import org.megrez.task.CommandLineTask
import org.megrez._

class Worker(val workspace: Workspace) extends Actor with Logging{
  def act() {
    loop {
      react {
        case (actor: Actor, assignment: JobAssignment) =>
          try {
            val pipelineDir = workspace.createFolder(assignment.pipeline)
            updateMaterials(assignment)
            for (task <- assignment.job.tasks) {
              task match {
                case t:CommandLineTask =>
                  t.onConsoleOutputChanges(actor ! ConsoleOutput(_))
                  t.execute(pipelineDir)
                case _ => task.execute(pipelineDir)
              }
            }
            actor ! new JobCompleted()
          } catch {
            case e: Exception => actor ! new JobFailed(e.getMessage)
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

