package org.megrez.server

import actors._
import scala.collection.mutable._
import org.megrez._

class Agent(handler: AgentHandler, dispatcher: Actor) extends Actor {
  private val resources = new HashSet[String]()
  private var current: Option[JobAssignment] = None

  def act() {
    loop {
      react {
        case assignment: JobAssignment =>
          handleAssignment(assignment)
        case ToAgent.SetResources(tags) =>
          resources.clear
          tags.foreach(resources add _)
        case _: JobCompleted =>
          dispatcher ! AgentToDispatcher.JobCompleted(this, current.get)
          current = None
        case _: JobFailed =>
          dispatcher ! AgentToDispatcher.JobFailed(this, current.get)
          current = None
        case Stop => exit
      }
    }
  }

  private def handleAssignment(assignment: JobAssignment) {
    current match {
      case None =>
        if (checkResource(assignment.job)) {
          if (handler != null)
            handler.send("HAHA")
          current = Some(assignment)
          reply(AgentToDispatcher.Confirm)
        } else {
          reply(AgentToDispatcher.Reject)
        }
      case Some(_) =>
        reply(AgentToDispatcher.Reject)
    }
  }

  private def checkResource(job: Job) = job.resources.forall(resources contains _)

  start
}

trait AgentHandler {
  def assignAgent(agent: Actor)

  def send(message: String)
}
