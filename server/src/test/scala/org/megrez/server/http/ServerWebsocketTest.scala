package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Spec}
import scala.actors.Actor._
import java.net.URI

import Route._
import actors.{Actor, TIMEOUT}
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http.websocket.{DefaultWebSocketFrame, WebSocketFrame}

class ServerWebsocketTest extends Spec with ShouldMatchers with BeforeAndAfterEach {
  describe("Server websocket support") {
    it("should send message when agent connect") {
      server = new Server(
        websocket("/agent") -> self
        )

      server.start(8080)

      client = new WebSocketClient(new URI("ws://localhost:8080/agent"), self)

      receiveWithin(1000) {
        case _: Channel =>
        case TIMEOUT => fail
        case _ => fail
      }

      receiveWithin(1000) {
        case "megrez-agent:1.0" =>
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should create handler and append handler to pipeline") {
      def agent(channel: Channel, actor: Actor) = new SimpleChannelUpstreamHandler() {
        override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
          event.getMessage match {
            case frame: WebSocketFrame =>
              if (!frame.isBinary) actor ! frame.getTextData
            case _ =>
          }
        }
      }
      server = new Server(
        websocket("/agent", agent) -> self
        )

      server.start(8080)

      client = new WebSocketClient(new URI("ws://localhost:8080/agent"), self)

      receiveWithin(1000) {
        case "megrez-agent:1.0" =>
        case TIMEOUT => fail
        case _ => fail
      }
    }

    it("should communicte with multi client via websocket") {      
      def agent(channel: Channel, actor: Actor) = new SimpleChannelUpstreamHandler() {
        actor ! channel
        override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
          event.getMessage match {
            case frame: WebSocketFrame =>
              if (frame.getTextData == "megrez-agent:1.0")
                channel.write(new DefaultWebSocketFrame("megrez-server:1.0"))              
            case _ =>
          }
        }
      }

      server = new Server(
        websocket("/agent", agent) -> self
      )

      server.start(8080)

      client = new WebSocketClient(new URI("ws://localhost:8080/agent"), self)
      val channelOfClient = receiveWithin(1000) {
        case channel : Channel => channel
        case TIMEOUT => fail
        case _ => fail
      }
      
      client2 = new WebSocketClient(new URI("ws://localhost:8080/agent"), self)
      val channelOfClient2 = receiveWithin(1000) {
        case channel : Channel => channel
        case TIMEOUT => fail
        case _ => fail
      }

      channelOfClient.write(new DefaultWebSocketFrame("client 1"))
      receiveWithin(1000) {
        case (source : WebSocketClient, "client 1") => source should be ===(client)
        case TIMEOUT => fail
        case _ => fail
      }

      channelOfClient2.write(new DefaultWebSocketFrame("client 2"))

      receiveWithin(1000) {
        case (source : WebSocketClient, "client 2") => source should be ===(client2)
        case TIMEOUT => fail
        case _ => fail
      }

    }
  }

  var server: Server = _
  var client: WebSocketClient = _
  var client2 : WebSocketClient = _ 

  override protected def afterEach() {
    server.shutdown
    client.shutdown
    if (client2 != null) client2.shutdown
  }
}