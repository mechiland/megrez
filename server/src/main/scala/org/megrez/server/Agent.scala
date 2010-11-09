package org.megrez.server

import actors._
import scala.collection.mutable._
import org.megrez.{JobAssignment, Job}

class Agent(handler: AgentHandler, dispatcher: Actor) extends Actor {
  import AgentStatus._

  private val resources = new HashSet[String]()
  private var _status = Idle

  def act() {
    loop {
      react {
        case assignment : JobAssignment =>          
          handleAssignment(assignment)
        case ToAgent.SetResources(tags) =>
          resources.clear
          tags.foreach(resources add _)
        case message: JobFinished => handleFinished(message)
        case _: Exit => exit
        case a : Any => println(a)
      }
    }
  }

  private def handleAssignment(assignment: JobAssignment) {
    _status match {
      case Idle =>
        if (checkResource(assignment.job)) {
          _status = Busy
          if (handler != null)
            handler.send("HAHA")
          reply(AgentToDispatcher.Confirm)
        } else {
          reply(AgentToDispatcher.Reject)
        }
      case Busy =>
        reply(AgentToDispatcher.Reject)
    }
  }

  private def handleFinished(message: JobFinished) {
    _status = Idle
    dispatcher ! message
  }

  private def checkResource(job: Job) = job.resources.forall(resources contains _)

  start

}

object AgentStatus extends Enumeration {
  type AgentStatus = Value
  val Idle, Busy = Value
}

trait AgentHandler {
  def assignAgent(agent: Actor)

  def send(message: String)
}
