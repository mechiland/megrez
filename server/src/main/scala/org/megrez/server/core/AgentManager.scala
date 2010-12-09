package org.megrez.server.core

import actors.Actor
import org.megrez.Stop

class AgentManager(val dispatcher: Actor) extends Actor {
  def act() {
    loop {
      react {
        case ToAgentManager.RemoteAgentConnected(handler) =>
          val agent = new Agent(org.megrez.server.model.Agent(Map("resources" -> List())), handler, dispatcher)
          handler.assignAgent(agent)
          dispatcher ! AgentManagerToDispatcher.AgentConnect(agent)
        case Stop => exit
        case _ =>
      }
    }
  }
  start
}