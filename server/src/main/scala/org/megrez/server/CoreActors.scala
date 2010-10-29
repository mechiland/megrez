package org.megrez.server

import actors.Actor
import java.util.UUID
import collection.mutable.{HashSet, HashMap}

class PipelineManager(val triggerFactory: PipelineConfig => Trigger) extends Actor {
  private val pipelines = HashMap[String, Pair[PipelineConfig, Trigger]]()

  def act {
    loop {
      react {
        case message: AddPipeline =>
          addPipeline(message.config)
        case message: PipelineChanged =>
          removePipeline(message.config.name)
          addPipeline(message.config)
        case _ =>
      }
    }
  }

  private def addPipeline(config: PipelineConfig): Option[(PipelineConfig, Trigger)] = {
    pipelines.put(config.name, Pair(config, launchTrigger(config)))
  }


  private def launchTrigger(config: PipelineConfig): Trigger = {
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

class BuildScheduler(val dispatcher: Actor) extends Actor {
  private val builds = HashMap[UUID, Build]()

  def act {
    loop {
      react {
        case TriggerBuild(config: PipelineConfig) =>
          val id = UUID.randomUUID
          val build = new Build(config)
          builds.put(id, build)
          triggerJobs(id, build)
        case JobCompleted(id: UUID, job: Job) =>
          builds.get(id) match {
            case Some(build: Build) =>
              if (build.current.complete(job)) {
                build.current match {
                  case Build.Completed =>
                  case Build.Failed =>
                  case _ => triggerJobs(id, build)
                }                
              }
            case None =>
          }
        case _ =>
      }
    }
  }

  private def triggerJobs(id: UUID, build: Build): Unit = {
    build.current.jobs match {
      case Some(jobs: Set[Job]) =>
        dispatcher ! JobScheduled(id, jobs)
      case None =>
    }
  }

  start
}

class Dispatcher extends Actor {
  private val jobQueue = new HashSet[Job]()
  private val idleAgents = new HashSet[Actor]()

  def act() {
    loop {
      react {
        case message: TriggerMessage => trigger(message.pipelineName, Configuration.firstStage(message.pipelineName))
        case connection: AgentConnect => registerAgent(connection.agent)
        case message: JobConfirm => handleJobConfirm(message)
        case message: JobFinished => handleJobFinished(message)
        case _: Exit => exit
      }
    }
  }

  def jobs = jobQueue.toSet

  def agents = idleAgents.toSet

  private def registerAgent(agent: Actor) {
    idleAgents add agent
    reply(Success())
  }

  private def handleJobConfirm(message: JobConfirm) {
    jobQueue.remove(message.job)
    idleAgents.remove(message.agent)
  }

  private def handleJobFinished(message: JobFinished) {
    idleAgents.add(message.agent)
    if (Configuration.hasNextStage(message.pipeline, message.stage)) {
      trigger(message.pipeline, Configuration.nextStage(message.pipeline, message.stage))
    }
  }

  private def trigger(pipeline: String, stage: String) {
    val job = new Job(pipeline, Set(), List())
    jobQueue.add(job)
    idleAgents.foreach(_ ! new JobRequest(pipeline, stage, job))
    reply(Success())
  }

}
