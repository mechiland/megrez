package org.megrez.server

import collection.mutable.HashSet
import org.megrez.{Material, Pipeline, Job}

class Build(val pipeline: Pipeline, val changes: Map[Material, Option[Any]]) {
  def this(pipeline: Pipeline) = this (pipeline, Map[Material, Option[Any]]())

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

  def cancel(jobs: Set[Job]): Option[Stage] = {
    currentStage.cancel(jobs)
    currentStage.status match {
      case Build.Stage.Status.Canceled =>
        Some(Build.Canceled)
      case _ =>
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

    def cancel(jobs: Set[Job]) {}
  }

  object Stage {
    object Status extends Enumeration {
      type Status = Value
      val Completed, Failed, Ongoing, Canceled = Value
    }
  }

  object Completed extends Stage {
    override def status = Stage.Status.Completed
  }
  object Failed extends Stage {
    override def status = Stage.Status.Failed
  }

  object Canceled extends Stage {
    override def status = Stage.Status.Canceled
  }

  class JobStage(val stage: Pipeline.Stage) extends Stage {
    private val completedJobs = HashSet[Job]()
    private val failedJobs = HashSet[Job]()
    private val canceledJobs = HashSet[Job]()
    private var stageStatus = Stage.Status.Ongoing

    override def jobs: Set[Job] = stage.jobs

    override def status = stageStatus

    override def complete(job: Job) {
      completedJobs.add(job)
      if (completedJobs == stage.jobs)
        stageStatus = Stage.Status.Completed
      else if (failedJobs.size + completedJobs.size == stage.jobs.size)
        stageStatus = Stage.Status.Failed
    }

    override def fail(job: Job) {
      failedJobs.add(job)
      if (failedJobs.size + completedJobs.size == stage.jobs.size)
        stageStatus = Stage.Status.Failed
    }

    override def cancel(jobs: Set[Job]) {
      jobs.foreach(canceledJobs.add(_))
      stageStatus = Stage.Status.Canceled
    }

    def jobStatus(job: Job) = {
      if (completedJobs.contains(job)) Stage.Status.Completed
      else if (failedJobs.contains(job)) Stage.Status.Failed
      else if (canceledJobs.contains(job)) Stage.Status.Canceled
      else Stage.Status.Ongoing
    }

  }
}