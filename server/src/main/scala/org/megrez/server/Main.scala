package org.megrez.server

import http.Route._
import actors.Actor
import http.{AgentWebSocketHandler, Server}
import org.jboss.netty.channel.Channel
import org.megrez.util.Logging

object Main extends Logging {  
  private var server: Server = null

  def start(port: Int) {
    def agent(channel: Channel, actor: Actor) = new AgentWebSocketHandler(channel, actor)  

    if (server == null) {
      server = new Server(
        websocket("/agent", agent) -> Megrez.agentManager
        )
      server.start(port)
    }
  }


  def stop {
    server.shutdown
  }

  def main(args: Array[String]) {
    start(8080)
    info("Start listening 8080...")
  }
}
