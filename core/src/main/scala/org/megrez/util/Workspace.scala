package org.megrez.util

import java.io.File
import io.Source
import java.lang.String

trait Workspace {
  def getFolder(folder: String): File

  def createFolder(folder: String): File

  def removeFolder(folder: String)

  def findFiles(folder: File, pattern: String): List[File]
}

class FileWorkspace(val root: File) extends Workspace {
  override def getFolder(pipelineId: String): File = {
    val pipeline = new File(root, pipelineId)
    if (pipeline.exists) pipeline else null
  }

  override def createFolder(folder: String): File = {
    val pipeline = new File(root, folder)
    if (!pipeline.exists) pipeline.mkdirs
    pipeline
  }


  def removeFolder(folder: String) {
    delete(getFolder(folder))
  }

  def findFiles(folder: File, pattern: String): List[File] = {
    var result: List[File] = List[File]()

    val process = Runtime.getRuntime().exec("find . -name " + pattern, null, folder)
    process.waitFor match {
      case 0 => Source.fromInputStream(process.getInputStream()).getLines.foreach {
        filePath: String => result = new File(folder.getPath + filePath.replaceFirst(".", "")):: result
      }
      case exitCode: Int => throw new ShellException("exit code " + exitCode + "\n" + Source.fromInputStream(process.getErrorStream).mkString)
    }
    return result
  }

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}
