package org.megrez.server

import collection.mutable.HashSet
import java.io.File
import vcs.VersionControl

class Pipeline(val name: String, val material: Material, val stages: List[Pipeline.Stage]) {
  private def workingDir() = {
    new File(System.getProperty("user.dir") + "/pipelines/" + name)
  }
  def checkChange() = material.versionControl(workingDir()).checkChange
}

object Pipeline {
  case class Stage(val name: String, val jobs: Set[Job])

}

class Build(val pipeline: Pipeline) {
  import Build._
  private val stageIterator = pipeline.stages.iterator
  private var currentStage = nextStage

  def current = currentStage

  private def nextStage = if (stageIterator.hasNext) new JobStage(stageIterator.next) else Completed

  def complete(job: Job): Option[Stage] = {
    currentStage.complete(job)
    currentStage.status match {
      case Build.Stage.Status.Completed =>
        currentStage = nextStage
        Some(currentStage)
      case Build.Stage.Status.Failed =>
        Some(Build.Failed)
      case Build.Stage.Status.Ongoing =>
        None
    }
  }

  def fail(job: Job): Option[Stage] = {
    currentStage.fail(job)
    currentStage.status match {
      case Build.Stage.Status.Failed =>
        Some(Build.Failed)
      case Build.Stage.Status.Ongoing =>
        None
    }    
  }
}

object Build {
  trait Stage {
    import Stage.Status._
    def jobs: Set[Job] = Set[Job]()
    def status: Status
    def complete(job: Job) {}    
    def fail(job: Job) {}
  }

  object Stage {
    object Status extends Enumeration {
      type Status = Value
      val Completed, Failed, Ongoing = Value
    }
  }

  object Completed extends Stage {
    override def status = Stage.Status.Completed
  }
  object Failed extends Stage {
    override def status = Stage.Status.Failed
  }

  class JobStage(val stage: Pipeline.Stage) extends Stage {
    private val completedJobs = HashSet[Job]()
    private val failedJobs = HashSet[Job]()
    private var stageStatus = Stage.Status.Ongoing

    override def jobs: Set[Job] = stage.jobs    

    override def status = stageStatus

    override def complete(job : Job) {
      completedJobs.add(job)
      if (completedJobs == stage.jobs)
        stageStatus = Stage.Status.Completed
      else if (failedJobs.size + completedJobs.size == stage.jobs.size)
        stageStatus = Stage.Status.Failed
    }

    override def fail(job : Job) {
      failedJobs.add(job)      
      if (failedJobs.size + completedJobs.size == stage.jobs.size)
        stageStatus = Stage.Status.Failed
    }
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