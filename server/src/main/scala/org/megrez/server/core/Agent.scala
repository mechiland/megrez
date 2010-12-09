package org.megrez.server.core

import actors.Actor
import org.megrez.util.JSON
import org.megrez.{Stop, JobCompleted, JobFailed, JobAssignment}
import org.megrez.server.model.{Job, JobExecution, Build}

class Agent(val model: org.megrez.server.model.Agent, handler: AgentHandler, dispatcher: Actor) extends Actor {
  private var currentJob: Option[JobExecution] = None

  def act {
    loop {
      react {
        case Pair(build: Build, jobExecution: JobExecution) =>
          if (checkResource(jobExecution.job)) {
            val assignment = JobAssignment(build.id.toInt, build.pipeline.name, build.changes.map(change => (change.material.changeSource.toChangeSource, change.material.destination) -> change.toChange).toMap, jobExecution.job.tasks.map(_.toTask).toList)
            handler.send(JSON.write(assignment))
            currentJob = Some(jobExecution)
            reply(AgentToDispatcher.Confirm)
          }
          else {
            reply(AgentToDispatcher.Reject)
          }
        case JobCompleted(result) =>
          currentJob match {
            case None =>
            case _ =>
              dispatcher ! AgentToDispatcher.JobFinished(this, currentJob.get, false)
              currentJob = None
          }
        case JobFailed(reason) =>
          currentJob match {
            case None =>
            case _ =>
              dispatcher ! AgentToDispatcher.JobFinished(this, currentJob.get, true)
              currentJob = None
          }
        case Stop => exit
        case _ =>
      }
    }
  }

  private def checkResource(job: Job) = job.resources.forall(model.resources().contains(_))
  start
}


trait AgentHandler {
  def assignAgent(agent: Actor)

  def send(message: String)
}
