package org.megrez.server

import java.io.File
class Pipeline(val name: String, val material: Material, val stages: List[Pipeline.Stage]) {
  private def workingDir() = {
    new File(System.getProperty("user.dir") + "/pipelines/" + name)
  }
  def checkChange() = material.versionControl(workingDir()).checkChange
}

object Pipeline {
  case class Stage(val name: String, val jobs: Set[Job])

}

class Job(val name: String, val resources: Set[String], val tasks: List[Task])

class Task

object Configuration {
  def hasNextStage(pipeline: String, stage: String) = {
    false
  }

  def firstStage(pipeline: String) = {
    "stage1"
  }

  def nextStage(pipeline: String, stage: String) = {
    "stage2"
  }
}