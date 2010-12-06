package org.megrez.server.core

import actors.Actor
import collection.mutable.{HashMap, HashSet}
import org.megrez.server.AgentToDispatcher
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
        case AgentConnect(agent) =>
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
          buildScheduler ! JobFinished(jobInProgress.get(jobExecution).get, () => operation)
          jobInProgress.remove(jobExecution)
          idleAgents.add(agent)
          dispatchJobs
          info("dispatchJobs")
        case "STOP" => exit
      }
    }
  }

  private def dispatchJobs() {
    if (jobAssignments.size == 0 || idleAgents.size == 0) return
    val (jobExecution, build) = jobAssignments.head
    val idleAgent = idleAgents.head
    idleAgent !? JobAssignmentFuture(build.id.toInt, build.pipeline.name, Map(), List()) match {
      case AgentToDispatcher.Confirm =>
        jobAssignments.remove(jobExecution)
        idleAgents.remove(idleAgent)
        jobInProgress.put(jobExecution, build)
        assignedJobs.put(build.id.toInt, jobExecution)
    }
  }

  start
}
case class AgentConnect(agent: Actor)