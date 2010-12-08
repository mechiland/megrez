package org.megrez.server.core

import actors.Actor
import actors.Actor._
import org.megrez.server.model.{JobExecution, Build}
import org.megrez.{JobAssignmentFuture, Stop}
import org.megrez.server.AgentToDispatcher
import org.megrez.util.JSON

class Agent(val model: org.megrez.server.model.Agent, handler: AgentHandler, dispatcher: Actor) extends Actor {
  def act {
    loop {
      react {
        case Pair(build: Build, execution: JobExecution) =>          
          val assignment = JobAssignmentFuture(build.id.toInt, build.pipeline.name, build.changes.map(change => (change.material.changeSource.asInstanceOf[org.megrez.ChangeSource], change.material.destination) -> Some(change : Any)).toMap, execution.job.tasks.map(_.asInstanceOf[org.megrez.Task]).toList)
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
