package org.megrez.server

import data.Graph
import http.Route._
import actors.Actor
import http.{Controllers, AgentWebSocketHandler, Server}
import org.jboss.netty.channel.Channel
import org.megrez.util.Logging
import org.neo4j.kernel.EmbeddedGraphDatabase

object Main extends Logging {
  private var server: Server = null
  private val megrez = new Megrez
  private val controllers = new Controllers(megrez)

  private var neo : EmbeddedGraphDatabase = _

  def start(port: Int) {
    def agent(channel: Channel, actor: Actor) = new AgentWebSocketHandler(channel, actor)

    if (server == null) {
      server = new Server(
        websocket("/agent", agent) -> megrez.agentManager
        , post("/pipelines") -> controllers.pipeline
        , get("/builds") -> controllers.builds)
      server.start(port)
    }
    neo = new EmbeddedGraphDatabase("database/megrez")
    Graph.of(neo)
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        neo.shutdown
      }
    })
  }


  def stop {
    server.shutdown
    neo.shutdown
  }

  def main(args: Array[String]) {
    start(Integer.parseInt(args.head))
    info("Start listening " + args.head + "...")
  }
}
