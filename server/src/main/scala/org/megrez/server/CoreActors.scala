package org.megrez.server

import actors.Actor
import java.util.UUID
import collection.mutable.{HashSet, HashMap}

class PipelineManager(megrez : {val triggerFactory : Pipeline => Trigger}) extends Actor {
  private val pipelines = HashMap[String, Pair[Pipeline, Trigger]]()

  def act {
    loop {
      react {
        case message: AddPipeline =>
          addPipeline(message.config)
        case message: PipelineChanged =>
          removePipeline(message.config.name)
          addPipeline(message.config)
        case message: RemovePipeline => removePipeline(message.config.name)
        case _: Exit => exit
        case _ =>
      }
    }
  }

  private def addPipeline(config: Pipeline): Option[(Pipeline, Trigger)] = {
    pipelines.put(config.name, Pair(config, launchTrigger(config)))
  }


  private def launchTrigger(config: Pipeline): Trigger = {
    val trigger = megrez.triggerFactory(config)
    trigger.start
    trigger
  }

  private def removePipeline(name: String) {
    pipelines.remove(name) match {
      case Some(Pair(_, trigger: Trigger)) => trigger.stop
      case None =>
    }
  }

  start
}

class BuildScheduler(megrez : {val dispatcher: Actor; val buildManager: Actor}) extends Actor {
  private val builds = HashMap[UUID, Build]()

  def act {
    loop {
      react {
        case TriggerBuild(config: Pipeline) =>
          val id = UUID.randomUUID
          val build = new Build(config)
          builds.put(id, build)
          triggerJobs(id, build)
        case JobCompleted(id: UUID, job: Job) =>
          builds.get(id) match {
            case Some(build: Build) =>
              build.complete(job) match {
                case Some(Build.Completed) =>
                  megrez.buildManager ! BuildCompleted(build)
                  builds.remove(id)
                case Some(Build.Failed) =>
                  megrez.buildManager ! BuildFailed(build)
                  builds.remove(id)
                case Some(stage: Build.Stage) =>
                  triggerJobs(id, build)
                case None =>
              }
            case None =>
          }
        case JobFailed(id: UUID, job: Job) =>
          builds.get(id) match {
            case Some(build: Build) =>
              build.fail(job) match {
                case Some(Build.Failed) =>
                  megrez.buildManager ! BuildFailed(build)
                  builds.remove(id)
                case None =>
              }
            case None =>
          }
        case _: Exit => exit
        case _ =>
      }
    }
  }

  private def triggerJobs(id: UUID, build: Build) {
    megrez.dispatcher ! JobScheduled(id, build.current.jobs)
  }

  start
}

class Dispatcher(val megrez : {val buildScheduler : Actor}) extends Actor {
  private val jobQueue = new HashSet[Job]()
  private val idleAgents = new HashSet[Actor]()

  def act() {
    loop {
      react {
        case message: AgentConnect => {
          idleAgents.add(message.agent)
        }

        case message: JobScheduled => {
          message.jobs.foreach(jobQueue.add _)
          jobQueue.foreach(assignJob(message.build, _))
        }

        case message: JobConfirm => {
          jobQueue.remove(message.job)
          idleAgents.remove(message.agent)
          reply(Success())
        }

        case message: JobFinished => {
          idleAgents.add(message.agent)
          megrez.buildScheduler ! new JobCompleted(message.buildId, message.job)
          reply(Success())
        }

        case _: Exit => exit
      }
    }
  }

  private def assignJob(buildId: UUID, job: Job) {
    idleAgents.foreach(_ ! new JobRequest(buildId, job))
  }

  def jobs = jobQueue.toSet

  def agents = idleAgents.toSet

  start
}
