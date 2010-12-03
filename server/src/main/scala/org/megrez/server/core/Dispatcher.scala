package org.megrez.server.core

import actors.Actor
import collection.mutable.{HashMap, HashSet}
import org.megrez.server.AgentToDispatcher
import org.megrez.JobAssignmentFuture
import collection.immutable.Set
import org.megrez.server.model.{Pipeline, Material, Build, JobExecution}
import org.megrez.util.Logging

class Dispatcher extends Actor with Logging {
  private val idleAgents = new HashSet[Actor]()
  private val jobAssignments = new HashMap[JobExecution, Build]()
  private val jobInProgress = new HashMap[JobExecution, Build]()

  def act() {
    loop {
      react {
        case AgentConnect(agent) =>
          idleAgents.add(agent)
          info("Agent idle for job, total " + idleAgents.size + " idle agents")
          dispatchJobs
        case jobs: List[Pair[Build, JobExecution]] =>
          info("Job scheduled for build " + jobs.head._1 + " " + jobs.size + " jobs")
          jobs.foreach((item: Pair[Build, JobExecution]) => jobAssignments.put(item._2, item._1))
          dispatchJobs
      }
    }
  }

  private def dispatchJobs {
    jobAssignments.keys.map(dispatchJob).foreach(_ match {
      case Some((agent: Actor, job: JobExecution)) =>
        idleAgents.remove(agent)
        jobInProgress.put(job, jobAssignments.get(job).get)
        jobAssignments.remove(job)
      case None =>
    })
    info("Total " + idleAgents.size + " idle agents and " + jobAssignments.size + " job waiting")
  }

  private def dispatchJob(job: JobExecution) = {
    val agents = idleAgents.iterator
    def assignJob: Option[Actor] = {
      if (agents.hasNext) {
        val agent = agents.next
        val build: Build = jobAssignments.get(job).get
        val pipeline: Pipeline = build.pipeline
        val materials: Set[Material] = pipeline.materials
        val jobAssignmentFuture: JobAssignmentFuture = new JobAssignmentFuture(build.id.toInt, pipeline.name, null, null)
        agent !? jobAssignmentFuture match {
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
case class AgentConnect(agent: Actor)