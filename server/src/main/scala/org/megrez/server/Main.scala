package org.megrez.server

import http.Route._
import actors.Actor
import actors.Actor._
import http.{AgentWebSocketHandler, Server}
import org.jboss.netty.channel.Channel

object Main {
  object Megrez {
    
  }
  
  private var server: Server = null

  def start(port: Int) {
    def agent(channel: Channel, actor: Actor) = new AgentWebSocketHandler(channel, actor)  

    if (server == null) {
      server = new Server(
        websocket("/agent", agent) -> actor {
          react {
            case connected: RemoteAgentConnected =>
              println(connected.handler)
            case _ =>
          }
        }
        )
      server.start(port)
    }
  }


  def stop {
    server.shutdown
  }

  def main(args: Array[String]) {
    start(8080)
  }
}
