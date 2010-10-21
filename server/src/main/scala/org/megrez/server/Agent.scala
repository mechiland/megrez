package org.megrez.server

import actors._
import scala.collection.mutable._

class Agent extends Actor {
  import AgentStatus._

  private var _tags : HashSet[String] = new HashSet[String]()
  private var _status = Idle

  def act() {
    loop {
      react {
        case job: Job => handleJob(job)
        case tags : SetTags => setTags(tags)
        case _: Exit => exit
      }
    }
  }

  def status() = _status
  def tags() = _tags.toSet

  private def setTags(tags : SetTags) {
    _tags.clear()
    tags.tags.foreach(_tags add _)
    reply(Success())
  }

  private def handleJob(job: Job) {
    _status match {
      case Idle =>
        _status = Busy
        reply(JobConfirm(this))
      case Busy =>
        reply(JobReject(this))
    }
  }
}

object AgentStatus extends Enumeration {
  type AgentStatus = Value
  val Idle, Busy = Value
}
