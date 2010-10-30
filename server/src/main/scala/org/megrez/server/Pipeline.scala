package org.megrez.server

import collection.mutable.HashSet

class Pipeline(val name: String, val material: Material, val stages: List[Pipeline.Stage]) {
  def workingDir() = {
    System.getProperty("user.dir") + "/pipelines" + name
  }
}

object Pipeline {
  case class Stage(val name: String, val jobs: Set[Job])

}

class Build(val pipeline: Pipeline) {
  import Build._
  private val stageIterator = pipeline.stages.iterator
  private var currentStage = nextStage

  def current = currentStage

  private def nextStage = if (stageIterator.hasNext) new JobStage(stageIterator.next, next, fail) else Completed

  private def next() {
    currentStage = nextStage
  }

  private def fail() {
    currentStage = Build.Failed
  }
}

object Build {
  trait Stage {
    def jobs: Option[Set[Job]] = None

    def complete(job: Job) = false

    def fail(job: Job) {}
  }

  object Completed extends Stage
  object Failed extends Stage

  class JobStage(val stage: Pipeline.Stage, val complete: () => Unit, val fail: () => Unit) extends Stage {
    private val completedJobs = HashSet[Job]()
    private val failedJobs = HashSet[Job]()

    override def jobs: Option[Set[Job]] = if (failedJobs.isEmpty) Some(stage.jobs) else None

    override def complete(job: Job) = {
      completedJobs.add(job)
      val completed = completedJobs == stage.jobs
      if (completed) complete()
      completed
    }

    override def fail(job: Job) {
      failedJobs.add(job)
      fail()
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