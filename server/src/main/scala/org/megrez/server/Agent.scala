package org.megrez.server

import actors._
import scala.actors.Actor._

import AgentStatus._

class Agent extends Actor {
  private var _status = Idle

  def act() {
    loop {
      react {
        case job: Job => handleJob(job)
        case _: Exit => exit
      }
    }
  }

  def status() = _status

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




