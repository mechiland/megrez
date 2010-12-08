package org.megrez.server.core

import actors.Actor
import org.megrez.{JobAssignment, Stop}
import org.megrez.util.JSON
import collection.mutable.HashSet
import org.megrez.server.model.{Job, JobExecution, Build}

class Agent(val model: org.megrez.server.model.Agent, handler: AgentHandler, dispatcher: Actor) extends Actor {
  def act {
    loop {
      react {
        case Pair(build: Build, jobExecution: JobExecution) =>
          if (checkResource(jobExecution.job)) {
            val assignment = JobAssignment(build.id.toInt, build.pipeline.name, build.changes.map(change => (change.material.changeSource.toChangeSource, change.material.destination) -> change.toChange).toMap, jobExecution.job.tasks.map(_.toTask).toList)
            handler.send(JSON.write(assignment))
            reply(AgentToDispatcher.Confirm)
          }
          else {
            reply(AgentToDispatcher.Reject)
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
