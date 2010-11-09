package org.megrez.server

import actors._
import scala.collection.mutable._
import org.megrez._

class Agent(handler: AgentHandler, dispatcher: Actor) extends Actor {
  import AgentStatus._

  private val resources = new HashSet[String]()
  private var status = Idle
  private var current : JobAssignment = null

  def act() {
    loop {
      react {
        case assignment : JobAssignment =>          
          handleAssignment(assignment)
        case ToAgent.SetResources(tags) =>
          resources.clear
          tags.foreach(resources add _)
        case _ : JobCompleted =>
          status = Idle
          dispatcher ! AgentToDispatcher.JobCompleted(this, current)
        case _ : JobFailed =>
          status = Idle
          dispatcher ! AgentToDispatcher.JobFailed(this, current)        
        case _: Exit => exit
      }
    }
  }

  private def handleAssignment(assignment: JobAssignment) {
    status match {
      case Idle =>
        if (checkResource(assignment.job)) {
          status = Busy
          if (handler != null)
            handler.send("HAHA")
          current = assignment
          reply(AgentToDispatcher.Confirm)
        } else {
          reply(AgentToDispatcher.Reject)
        }
      case Busy =>
        reply(AgentToDispatcher.Reject)
    }
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
