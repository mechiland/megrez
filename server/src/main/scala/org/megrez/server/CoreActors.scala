package org.megrez.server

import actors.Actor
import java.util.UUID
import collection.mutable.{HashSet, HashMap}
import trigger.{Materials, OnChanges, Trigger}
import org.megrez.util.{FileWorkspace, Logging}
import java.io.File
import org.megrez.{JobAssignment, Material, Pipeline, Job}

class PipelineManager(megrez: {val triggerFactory: Pipeline => Trigger}) extends Actor {
  private val pipelines = HashMap[String, Pair[Pipeline, Trigger]]()

  def act {
    loop {
      react {
        case ToPipelineManager.AddPipeline(pipeline) =>
          addPipeline(pipeline)
        case ToPipelineManager.PipelineChanged(pipeline) =>
          removePipeline(pipeline.name)
          addPipeline(pipeline)
        case ToPipelineManager.RemovePipeline(pipeline) =>
          removePipeline(pipeline.name)
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

class BuildScheduler(megrez: {val dispatcher: Actor; val buildManager: Actor}) extends Actor {
  private val builds = HashMap[UUID, Build]()

  def act {
    loop {
      react {
        case TriggerToScheduler.TrigBuild(pipeline, materials) =>
          val id = UUID.randomUUID
          val build = new Build(pipeline, materials)
          builds.put(id, build)
          scheduleJobs(id, build)
        case DispatcherToScheduler.JobCompleted(id, job) =>
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
                  scheduleJobs(id, build)
                case None =>
              }
            case None =>
          }
        case DispatcherToScheduler.JobFailed(id, job) =>
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

  private def scheduleJobs(id: UUID, build: Build) {
    megrez.dispatcher ! SchedulerToDispatcher.JobScheduled(id, build.current.jobs.map(JobAssignment(build.pipeline.name, build.changes, _)).toSet)
  }

  start
}

class Dispatcher(megrez: {val buildScheduler: Actor}) extends Actor {
  private val jobAssignments = new HashMap[JobAssignment, UUID]()
  private val jobInProgress = new HashMap[JobAssignment, UUID]()
  private val idleAgents = new HashSet[Actor]()

  def act() {
    loop {
      react {
        case AgentManagerToDispatcher.AgentConnect(agent: Actor) =>
          idleAgents.add(agent)
          dispatchJobs
        case SchedulerToDispatcher.JobScheduled(build, assignments) =>
          assignments.foreach(jobAssignments.put(_, build))
          dispatchJobs
        case AgentToDispatcher.JobCompleted(agent, assignment) =>
          megrez.buildScheduler ! DispatcherToScheduler.JobCompleted(jobInProgress.get(assignment).get, assignment.job)
          jobInProgress.remove(assignment)
          idleAgents.add(agent)
          dispatchJobs
        case AgentToDispatcher.JobFailed(agent, assignment) =>
          megrez.buildScheduler ! DispatcherToScheduler.JobFailed(jobInProgress.get(assignment).get, assignment.job)
          jobInProgress.remove(assignment)
          idleAgents.add(agent)
          dispatchJobs
        case _: Exit => exit
      }
    }
  }

  private def dispatchJobs {
    jobAssignments.keys.map(dispatchJob).foreach(_ match {
      case Some((agent: Actor, job: JobAssignment)) =>
        idleAgents.remove(agent)
        jobInProgress.put(job, jobAssignments.get(job).get)
        jobAssignments.remove(job)
      case None =>
    })
  }

  private def dispatchJob(job: JobAssignment) = {
    val agents = idleAgents.iterator
    def assignJob: Option[Actor] = {
      if (agents.hasNext) {
        val agent = agents.next
        agent !? job match {
          case AgentToDispatcher.Confirm =>
            Some(agent)
          case AgentToDispatcher.Reject => assignJob
        }
      } else None
    }

    assignJob match {
      case Some(agent: Actor) =>
        Some(agent -> job)
      case None => None
    }
  }
  start
}

class AgentManager(megrez: {val dispatcher: Actor}) extends Actor with Logging {
  def act() {
    loop {
      react {
        case ToAgentManager.RemoteAgentConnected(handler) =>
          info("Remote agent connected")
          val agent = new Agent(handler, megrez.dispatcher)
          handler.assignAgent(agent)
          megrez.dispatcher ! AgentManagerToDispatcher.AgentConnect(agent)
        case _ =>
      }
    }
  }

  start
}

class BuildManager extends Actor {
  def act() {
    loop {
      react {
        case _: Exit =>
          exit
        case _ =>
      }
    }
  }

  start
}


object Megrez {
  val agentManager: Actor = new AgentManager(this)
  val buildScheduler: Actor = new BuildScheduler(this)
  val buildManager: Actor = new BuildManager()
  val dispatcher: Actor = new Dispatcher(this)
  val pipelineManager = new PipelineManager(this)
  val workspace = new FileWorkspace(new File(System.getProperty("user.dir")))

  val triggerFactory: Pipeline => Trigger = pipeline => new OnChanges(new Materials(pipeline, workspace), buildScheduler, 5 * 60 * 1000)

  def stop() {
    dispatcher ! Exit()
    buildScheduler ! Exit()
    pipelineManager ! Exit()
  }
}

