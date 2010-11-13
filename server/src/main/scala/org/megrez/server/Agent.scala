package org.megrez.server

import actors._
import scala.collection.mutable._
import org.megrez._
import util.{JSON, Logging}

class Agent(handler: AgentHandler, dispatcher: Actor) extends Actor with Logging {
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
          val assignment = current.get
          info("Job completed " + assignment.pipeline + " " + assignment.job.name)
          dispatcher ! AgentToDispatcher.JobCompleted(this, assignment)
          current = None
        case _: JobFailed =>
          val assignment = current.get
          info("Job failed " + assignment.pipeline + " " + assignment.job.name)          
          dispatcher ! AgentToDispatcher.JobFailed(this, assignment)
          current = None
        case Stop => exit
      }
    }
  }

  private def handleAssignment(assignment: JobAssignment) {
    current match {
      case None =>
        if (checkResource(assignment.job)) {
          handler.send(JSON.write(assignment))
          current = Some(assignment)
          info("Confirm job " + assignment.pipeline + " " + assignment.job.name)
          reply(AgentToDispatcher.Confirm)
        } else {
          debug("Reject job " + assignment.pipeline + " " + assignment.job.name)
          reply(AgentToDispatcher.Reject)
        }
      case Some(_) =>
        debug("Reject job due to agent busy")
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
