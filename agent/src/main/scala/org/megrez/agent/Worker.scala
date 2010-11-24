package org.megrez.agent

import actors.Actor
import org.megrez.util.Workspace
import org.megrez.task.CommandLineTask
import org.megrez._
import java.io.{FileInputStream, File}
import java.util.{ArrayList, UUID}
import org.megrez.util.Logging
import vcs.VersionControl

class Worker(val workspace: Workspace) extends Actor with Logging {
  def act() {
    loop {
      react {
        case (actor: Actor, assignment: JobAssignment) =>
          try {
            val pipelineDir = workspace.createFolder(assignment.pipeline)
            updateMaterials(assignment)
            for (task <- assignment.job.tasks) {
              task match {
                case t: CommandLineTask =>
                  t.onConsoleOutputChanges(actor ! ConsoleOutput(_))
                  t.execute(pipelineDir)
                case _ => task.execute(pipelineDir)
              }
            }
            sendArtifactBack(assignment, pipelineDir, actor)
            actor ! new JobCompleted("done")
          } catch {
            case e: Exception => actor ! new JobFailed(e.getMessage)
            info(e.getMessage, e)
          }
        case _ =>
      }
    }
  }

  private def sendArtifactBack(assignment: JobAssignment, pipelineDir: File, actor: Actor) = {
    for (artifact <- assignment.job.artifacts) {
      val path: String = artifact.path
      if (path != "") {
        val tags: String = artifact.tags.mkString(":")
        val artifactFiles: ArrayList[File] = workspace.findFiles(pipelineDir, path)
        var zipFileName: String = UUID.randomUUID.toString
        zipFileName = ZipFileGenerator.getZipFile(zipFileName, artifactFiles, tags)
        if (zipFileName != "")
          actor ! new ArtifactStream(new FileInputStream(zipFileName))
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

