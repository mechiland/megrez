package org.megrez.server.core

import actors.Actor
import org.megrez.util.JSON
import org.megrez.server.model.{Job, JobExecution, Build}
import org.megrez.{JobFailed, JobCompleted, JobAssignment, Stop}

class Agent(val model: org.megrez.server.model.Agent, handler: AgentHandler, dispatcher: Actor) extends Actor {
  var current: Option[JobExecution] = None

  def act {
    loop {
      react {
        case Pair(build: Build, jobExecution: JobExecution) =>
          current match {
            case None =>
              if (checkResource(jobExecution.job)) {
                val assignment = JobAssignment(build.id.toInt, build.pipeline.name, build.changes.map(change => (change.material.changeSource.toChangeSource, change.material.destination) -> change.toChange).toMap, jobExecution.job.tasks.map(_.toTask).toList)
                handler.send(JSON.write(assignment))
                current = Some(jobExecution)
                reply(AgentToDispatcher.Confirm)
              }
              else reply(AgentToDispatcher.Reject)
            case Some(_) =>
              reply(AgentToDispatcher.Reject)
          }
        case _: JobCompleted =>
          dispatcher ! AgentToDispatcher.JobFinished(this, current.get, false)
          current = None
        case _: JobFailed =>
          dispatcher ! AgentToDispatcher.JobFinished(this, current.get, true)
          current = None
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
