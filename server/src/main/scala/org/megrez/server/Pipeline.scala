package org.megrez.server

import collection.mutable.HashSet

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

class Pipeline(val config: PipelineConfig) {
  case class Stage(val stage: PipelineConfig.Stage, val pipeline: Pipeline) {
    private val completedJobs = HashSet[Job]()
    private val failedJobs = HashSet[Job]()

    def jobs: Option[Set[Job]] = if (failedJobs.isEmpty) Some(stage.jobs) else None

    def complete(job: Job) {
      completedJobs.add(job)
      if (completedJobs == stage.jobs)
        pipeline.complete(this)
    }

    def fail(job: Job) {
      failedJobs.add(job)
      pipeline.fail(this)
    }
  }

  object Finish extends Stage(null, null) {
    override def jobs: Option[Set[Job]] = None

    override def complete(job: Job) {}

    override def fail(job: Job) {}
  }

  private val stageIterator = config.stages.iterator
  private var currentStage = nextStage

  def next(): Option[Set[Job]] = {
    currentStage.jobs
  }

  def complete(any: Any) = any match {
    case job: Job => currentStage.complete(job)
    case stage: Stage => currentStage = nextStage
    case _ =>
  }

  def fail(any: Any) = any match {
    case job: Job => currentStage.fail(job)
    case _ =>
  }

  private def nextStage = if (stageIterator.hasNext) Stage(stageIterator.next, this) else Finish
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