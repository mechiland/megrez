package org.megrez.server.core

import actors.Actor
import org.megrez.server.model.{JobExecution, Build}
import org.megrez.{JobAssignment, Stop}
import org.megrez.util.JSON

class Agent(val model: org.megrez.server.model.Agent, handler: AgentHandler, dispatcher: Actor) extends Actor {
  def act {
    loop {
      react {
        case Pair(build: Build, execution: JobExecution) =>
          val assignment = JobAssignment(build.id.toInt, build.pipeline.name, build.changes.map(change => (change.material.changeSource.toChangeSource, change.material.destination) -> change.toChange).toMap, execution.job.tasks.map(_.toTask).toList)
          handler.send(JSON.write(assignment))
          reply(AgentToDispatcher.Confirm)
        case Stop => exit
        case _ =>
      }
    }
  }
  start
}


trait AgentHandler {
  def assignAgent(agent: Actor)

  def send(message: String)
}
