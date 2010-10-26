package org.megrez.server

import actors._
import scala.collection.mutable._

class Agent extends Actor {
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
    reply(Success())
  }

  private def handleJob(request: JobRequest) {
    _status match {
      case Idle =>
        if (checkResource(request.job)) {
          _status = Busy
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
    reply(Success())
  }

  private def checkResource(job : Job) = job.resources.forall( _resources contains _ )
}

object AgentStatus extends Enumeration {
  type AgentStatus = Value
  val Idle, Busy = Value
}
