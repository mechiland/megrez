package org.megrez.server.http

import org.megrez.util.{Logging, JSON}
import org.jboss.netty.channel.{Channel, SimpleChannelUpstreamHandler, ChannelHandlerContext, MessageEvent}
import org.megrez.server.core.{AgentHandler, ToAgentManager}
import actors.Actor
import org.jboss.netty.handler.codec.http.websocket.{WebSocketFrame, DefaultWebSocketFrame}
import org.megrez.AgentMessage

class AgentWebSocketHandler(val channel: Channel, agentManager: Actor) extends SimpleChannelUpstreamHandler with AgentHandler with Logging {
  private val MegrezAgentHandshake = "megrez-agent:1.0"
  private var agent: Actor = _

  override def assignAgent(actor: Actor) {
    agent = actor
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case frame: WebSocketFrame =>
        frame.getTextData match {
          case MegrezAgentHandshake =>
            send("megrez-server:1.0")
            agentManager ! ToAgentManager.RemoteAgentConnected(this)
          case message: String =>
            agent ! JSON.read[AgentMessage](message)
        }
      case _ =>
    }
  }

  def send(message: String) {
    channel.write(new DefaultWebSocketFrame(message))
  }
}