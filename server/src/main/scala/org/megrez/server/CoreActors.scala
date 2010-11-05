package org.megrez.server

import actors.Actor
import java.util.UUID
import collection.mutable.{HashSet, HashMap}

class PipelineManager(val triggerFactory: Pipeline => Trigger) extends Actor {
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
    val trigger = triggerFactory(config)
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

class BuildScheduler(val dispatcher: Actor, val buildManager: Actor) extends Actor {
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
                  buildManager ! BuildCompleted(build)
                  builds.remove(id)
                case Some(Build.Failed) =>
                  buildManager ! BuildFailed(build)
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
                  buildManager ! BuildFailed(build)
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
    dispatcher ! JobScheduled(id, build.current.jobs)
  }

  start
}

class Dispatcher() extends Actor {
  private val jobRequestQueue = new HashSet[JobRequest]()
  private val idleAgents = new HashSet[Actor]()
  var buildScheduler: Actor = _

  def act() {
    loop {
      react {
        case message: AgentConnect => {
          idleAgents.add(message.agent)
          startAssigning
        }

        case message: JobScheduled => {
          message.jobRequests.foreach(jobRequest => jobRequestQueue.add(jobRequest))
          startAssigning
        }

        case message: JobConfirm => {
          jobRequestQueue.remove(message.jobRequest)
          idleAgents.remove(message.agent)
          reply(Success())
        }

        case message: JobFinished => {
          idleAgents.add(message.agent)
          buildScheduler ! new JobCompleted(message.buildId, message.job)
          reply(Success())
        }

        case _: Exit => exit
      }
    }
  }

  private def startAssigning {
    val assignedJobRequests = new HashSet[JobRequest]();
    jobRequestQueue.foreach {
      jobRequest => {
        if (!idleAgents.isEmpty) {
          val assigned: Boolean = idleAgents.remove(assignJobRequest(idleAgents.iterator, jobRequest).get)
          if(assigned)
            assignedJobRequests.add(jobRequest)
        }
      }
    }
    assignedJobRequests.foreach(jobRequestQueue.remove(_))
  }

  private def assignJobRequest(iterator: Iterator[Actor], jobRequest: JobRequest): Option[Actor] = {
    iterator.next !? jobRequest match {
      case message: JobConfirm => {
        Some(message.agent)
      }
      case message: JobReject => {
        if(iterator.hasNext) assignJobRequest(iterator, jobRequest) else None
      }
    }
  }

  def jobRequests = jobRequestQueue.toSet

  def agents = idleAgents.toSet

  start
}
