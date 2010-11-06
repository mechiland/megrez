package org.megrez.util

import java.io.File

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
