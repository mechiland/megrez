package org.megrez.server.core

import actors.Actor
import org.megrez.util.JSON
import org.megrez.server.model.{Job, JobExecution, Build}
import org.megrez._

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
        case ConsoleOutput(output) => currentJob match {
          case None =>
          case Some(job) => job.appendConsoleOutput(output)
        }
        case JobCompleted(result) => currentJob match {
          case None =>
          case Some(job) =>
            dispatcher ! AgentToDispatcher.JobFinished(this, job, false)
            currentJob = None
        }
        case JobFailed(reason) => currentJob match {
          case None =>
          case Some(job) =>
            dispatcher ! AgentToDispatcher.JobFinished(this, job, true)
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
