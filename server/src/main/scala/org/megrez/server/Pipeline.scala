package org.megrez.server

import java.util.Calendar

class Material(val url: String)
class SvnMaterial(override val url: String) extends Material(url)
class GitMaterial(override val url: String) extends Material(url)

class PipelineConfig(val name: String, val material: Material) {
  def workingDir() = {
    System.getProperty("user.dir") + "/pipelines" + name
  }
}

class Pipeline(val name: String, val repositoryUrl: String, val buildRevision: String, val buildDate: Calendar = null, val workDir: String = "")

class Job(val name: String, val resources: Set[String], val tasks: List[Task])

class Task

object Configuration {
  def hasNextStage(pipeline: String, stage: String) = {
    true
  }

  def firstStage(pipeline: String) = {
    "stage1"
  }

  def nextStage(pipeline: String, stage: String) = {
    "stage2"
  }
}