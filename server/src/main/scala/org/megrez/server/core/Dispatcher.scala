package org.megrez.server.core

import actors.Actor
import collection.mutable.{HashMap, HashSet}
import org.megrez.server.model.{Build, JobExecution}
import org.megrez.util.Logging
import org.megrez.JobAssignmentFuture

class Dispatcher(val buildScheduler: Actor) extends Actor with Logging {
  private val idleAgents = new HashSet[Actor]()
  private val jobAssignments = new HashMap[JobExecution, Build]()
  private val jobInProgress = new HashMap[JobExecution, Build]()
  private val assignedJobs = new HashMap[Int, JobExecution]()

  def act() {
    loop {
      react {
        case AgentManagerToDispatcher.AgentConnect(agent) =>
          idleAgents.add(agent)
          info("Agent idle for job, total " + idleAgents.size + " idle agents")
          dispatchJobs
        case jobs: List[Pair[Build, JobExecution]] =>
          info("Job scheduled for build " + jobs.head._1 + " " + jobs.size + " jobs")
          jobs.foreach((item: Pair[Build, JobExecution]) => jobAssignments.put(item._2, item._1))
          dispatchJobs
        case AgentToDispatcher.JobFinished(agent, jobAssignmentFuture, isFailed) =>
          info("Job finished")
          val jobExecution = assignedJobs.get(jobAssignmentFuture.buildId).get
          val operation = {
            if (!isFailed) jobExecution.completed
            else
              jobExecution.failed
          }
          buildScheduler ! DispatcherToScheduler.JobFinished(jobInProgress.get(jobExecution).get, () => operation)
          jobInProgress.remove(jobExecution)
          idleAgents.add(agent)
          dispatchJobs
        case "STOP" => exit
      }
    }
  }

  private def dispatchJobs() {
    jobAssignments.map(dispatchJob).foreach {
      _ match {
        case Some(Triple(agent, jobExecution, build)) =>
          jobAssignments.remove(jobExecution)
          idleAgents.remove(agent)
          jobInProgress.put(jobExecution, build)
          assignedJobs.put(build.id.toInt, jobExecution)
        case None =>
      }
    }
  }

  def dispatchJob(jobAssignment: (JobExecution, Build)): Option[(Actor, JobExecution, Build)] = {
    val agents = idleAgents.iterator
    val (jobExecution, build) = jobAssignment

    def assignJob(): Option[Actor] = {
      if (agents.hasNext) {
        val agent = agents.next
        agent !? JobAssignmentFuture(build.id.toInt, build.pipeline.name, Map(), List()) match {
          case AgentToDispatcher.Confirm => Some(agent)
          case AgentToDispatcher.Reject => assignJob
        }
      } else None
    }

    assignJob match {
      case Some(agent) =>
        Some(Triple(agent, jobExecution, build))
      case None =>
        None
    }
  }

  start
}