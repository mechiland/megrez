package org.megrez.server.core

import org.megrez.server.{ToAgentManager, Agent, AgentManagerToDispatcher}
import actors.Actor
import org.megrez.util.Logging

class AgentManager(val dispatcher: Actor) extends Actor with Logging {
  def act() {
    loop {
      react {
        case ToAgentManager.RemoteAgentConnected(handler) =>
          info("Remote agent connected")
          val agent = new Agent(handler, dispatcher)
          handler.assignAgent(agent)
          dispatcher ! AgentManagerToDispatcher.AgentConnect(agent)
        case _ =>
      }
    }
  }

  start
}