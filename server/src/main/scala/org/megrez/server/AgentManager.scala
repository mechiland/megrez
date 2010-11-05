package org.megrez.server

import actors.Actor

class AgentManager(megrez : { val dispatcher : Actor}) extends Actor {  
  def act() {
    loop {
      react {
        case RemoteAgentConnected(handler : AgentHandler) =>
          val agent = new Agent(handler, megrez.dispatcher)
          handler.assignAgent(agent)
          megrez.dispatcher ! AgentConnect(agent)
        case _ =>
      }
    }
  }

  start
}