package org.megrez.agent

import actors.Actor
import org.megrez.util.Workspace
import org.megrez.task.CommandLineTask
import org.megrez._
import org.megrez.util.Logging
import vcs.VersionControl
import java.util.zip.{ZipOutputStream, ZipEntry}
import java.io.{FileOutputStream, IOException, FileInputStream, File}

class Worker(val workspace: Workspace) extends Actor with Logging {
  def act() {
    loop {
      react {
        case (actor: Actor, assignment: JobAssignment) =>
          try {
            val pipelineDir = workspace.createFolder(assignment.pipeline)
            updateMaterials(assignment)
            for (task <- assignment.tasks) {
              task match {
                case t: CommandLineTask =>
                  t.onConsoleOutputChanges(actor ! ConsoleOutput(_))
                  t.execute(pipelineDir)
                case _ => task.execute(pipelineDir)
              }
            }
//            sendArtifactBack(assignment, pipelineDir, actor)
            actor ! new JobCompleted("done")
          } catch {
            case e: Exception => actor ! new JobFailed(e.getMessage)
            info(e.getMessage, e)
          }
        case _ =>
      }
    }
  }

//  private def sendArtifactBack(assignment: JobAssignmentFuture, pipelineDir: File, actor: Actor) = {
//    for (artifact <- assignment.job.artifacts) {
//      val path: String = artifact.path
//      if (path != "") {
//        val tags: String = artifact.tags.mkString(":")
//        val artifactFiles: List[File] = workspace.findFiles(pipelineDir, path)
//        var zipFileName: String = UUID.randomUUID.toString
//        zipFileName = generateZipFile(zipFileName, artifactFiles, tags)
//        if (zipFileName != "")
//          actor ! new ArtifactStream(new FileInputStream(zipFileName))
//      }
//    }
//  }

  private def generateZipFile(path: String, artifactFiles: List[File], tags: String): String = {
    try {
      val temFile = File.createTempFile(path, ".zip")
      val zipFile: ZipOutputStream = new ZipOutputStream(new FileOutputStream(temFile))
      zipFile.setComment("tag info:" + tags)
      val buf: Array[Byte] = new Array[Byte](1024)
      artifactFiles.foreach {
        file =>
          zipFile.putNextEntry(new ZipEntry(file.getAbsolutePath))
          val fileStream: FileInputStream = new FileInputStream(file)
          var b = fileStream.read(buf)
          while (b > 0) {
            zipFile.write(buf, 0, b)
            b = fileStream.read(buf)
          }
          zipFile.closeEntry
          fileStream.close
      }
      zipFile.close
      return temFile.getAbsolutePath
    }
    catch {
      case e: IOException => {
        System.out.println(e.getMessage)
        return ""
      }
    }
  }

  private def updateMaterials(assignment: JobAssignment) {
    for (((source, destination), workset) <- assignment.sources)
      source match {
        case versionControl: VersionControl =>
          val folder = if (destination != "$main") assignment.pipeline + "/" + destination else assignment.pipeline
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

