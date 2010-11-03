package org.megrez.server

import actors._
import scala.collection.mutable._

class Agent(handler: AgentHandler, dispatcher: Actor) extends Actor {
  import AgentStatus._

  private var _resources = new HashSet[String]()
  private var _status = Idle

  def act() {
    loop {
      react {
        case job: JobRequest => handleJob(job)
        case tags: SetResources => setResources(tags)
        case message: JobFinished => handleFinished(message)
        case _: Exit => exit
      }
    }
  }

  def status = _status

  def resources = _resources.toSet

  private def setResources(message: SetResources) {
    _resources.clear()
    message.resources.foreach(_resources add _)
  }

  private def handleJob(request: JobRequest) {
    _status match {
      case Idle =>
        if (checkResource(request.job)) {
          _status = Busy
          if(handler != null)
            handler.send("received a job")
          reply(JobConfirm(this, request.job))
        } else {
          reply(JobReject(this))
        }
      case Busy =>
        reply(JobReject(this))
    }
  }

  private def handleFinished(message: JobFinished) {
    _status = Idle
    dispatcher ! message
  }

  private def checkResource(job : Job) = job.resources.forall( _resources contains _ )
}

object AgentStatus extends Enumeration {
  type AgentStatus = Value
  val Idle, Busy = Value
}

trait AgentHandler {
  def assignAgent(agent : Actor)
  def send(message : String)
}

object Agent extends Actor {
  def act() {
    loop {
      react {
        case message : RemoteAgentConnected =>
          
        case _ => 
      }
    }
  }

  start
}
