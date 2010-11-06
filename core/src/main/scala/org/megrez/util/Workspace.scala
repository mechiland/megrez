package org.megrez.util

import java.io.File

trait Workspace {
  def getFolder(folder: String): File

  def createFolder(folder: String): File

  def removeFolder(folder: String)
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

  private def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}
