package org.megrez.server

import collection.mutable.{HashMap, HashSet}

class Material(val url: String)
class SvnMaterial(override val url: String) extends Material(url)
class GitMaterial(override val url: String) extends Material(url)

trait MaterialSource {
  def parse(json: Map[String, Any]): Material
}

object SvnMaterial extends MaterialSource {
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new SvnMaterial(url)
    case _ => null
  }
}
object GitMaterial extends MaterialSource {
  def parse(json: Map[String, Any]) = json("url") match {
    case url: String => new GitMaterial(url)
    case _ => null
  }
}


object Material {
  def find(vcs: String) = vcs match {
    case "svn" => SvnMaterial
    case "git" => GitMaterial
    case _ => null
  }
}

class PipelineConfig(val name: String, val material: Material, val stages: List[PipelineConfig.Stage]) {
  def workingDir() = {
    System.getProperty("user.dir") + "/pipelines" + name
  }
}

object PipelineConfig {
  case class Stage(val name: String, val jobs: Set[Job])

}

class Build(val pipeline: PipelineConfig) {
  import Build._
  private val stageIterator = pipeline.stages.iterator
  private var currentStage = nextStage

  def current = currentStage

  def complete(stage: Build.Stage) {
    currentStage = nextStage
  }

  def fail(stage: Build.Stage) {
    currentStage = Build.Failed
  }

  private def nextStage = if (stageIterator.hasNext) Build.Stage(stageIterator.next, this) else Completed
}

object Build {
  
  case class Stage(val stage: PipelineConfig.Stage, val build: Build) {
    private val completedJobs = HashSet[Job]()
    private val failedJobs = HashSet[Job]()

    def jobs: Option[Set[Job]] = if (failedJobs.isEmpty) Some(stage.jobs) else None

    def complete(job: Job) = {
      completedJobs.add(job)
      if (completedJobs == stage.jobs)
        build.complete(this)
      (completedJobs.size + failedJobs.size) == stage.jobs.size
    }

    def fail(job: Job) {
      failedJobs.add(job)
      build.fail(this)
    }
  }
  object Completed extends Stage(null, null) {
    override def jobs: Option[Set[Job]] = None
    override def complete(job: Job) = false
    override def fail(job: Job) {}
  }

  object Failed extends Stage(null, null) {
    override def jobs: Option[Set[Job]] = None
    override def complete(job: Job) = false
    override def fail(job: Job) {}    
  }
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