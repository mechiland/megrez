package org.megrez.server

import actors._
import scala.actors.Actor._

class Agent extends Actor {
  private var state = "IDLE"
  
  def act() {
    react {
      case job : Job => handleJob(job)
    }
  }
 
  def isBusy() = state == "BUSY"

  private def handleJob(job : Job) {
    state = "BUSY"
    job.scheduler ! new AgentStateChange(self, state)       
  }
}