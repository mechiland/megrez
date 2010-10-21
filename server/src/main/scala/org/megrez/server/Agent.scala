package org.megrez.server

import actors._
import scala.actors.Actor._

import AgentStatus._

class Agent extends Actor {

  private var _status = Idle
  
  def act() {
    react {
      case job : Job => handleJob(job)
    }
  }

  def status() = _status
 
  private def handleJob(job : Job) {
    _status = Busy
    reply(JobConfirm(this))
  }
}




