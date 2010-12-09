package org.megrez.server.core

import actors.Actor

class AgentManager(val dispatcher: Actor) extends Actor {
  def act() {
    loop {
      react {
        case ToAgentManager.RemoteAgentConnected(handler) =>
          val agent = new Agent(org.megrez.server.model.Agent(Map("resources" -> List())), handler, dispatcher)
          handler.assignAgent(agent)
          dispatcher ! AgentManagerToDispatcher.AgentConnect(agent)
      }
    }
  }
  start
}