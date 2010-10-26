package org.megrez.agent

import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.websocket._
import java.net._
import actors.Actor

class HandshakeHandler(val server: URI, val callback: ServerHandler) extends SimpleChannelUpstreamHandler {
  override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    event.getChannel.write(handshakeRequest)
    context.getPipeline.replace("encoder", "ws-encoder", new WebSocketFrameEncoder())
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case response: HttpResponse => handleWebSocketHandshake(context, response, event.getChannel)
      case frame: WebSocketFrame => handleMegrezHandshake(context, frame, event.getChannel)
    }
  }

  override def exceptionCaught(context: ChannelHandlerContext, event: ExceptionEvent) {
    event.getCause match {
      case exception: NotMegrezServerException => callback.invalidServer(exception.uri)
    }
  }

  private def handleWebSocketHandshake(context: ChannelHandlerContext, response: HttpResponse, channel: Channel) {
    val validStatus = response.getStatus.getCode == 101 && response.getStatus.getReasonPhrase == "Web Socket Protocol Handshake"
    val validHeaders = response.getHeader(Names.UPGRADE) == WEBSOCKET && response.getHeader(CONNECTION) == Values.UPGRADE
    if (!validStatus || !validHeaders) throw new NotMegrezServerException(server)
    context.getPipeline.replace("decoder", "ws-decoder", new WebSocketFrameDecoder())
    context.getChannel.write(new DefaultWebSocketFrame("megrez-agent:1.0"))
  }

  private def handleMegrezHandshake(context: ChannelHandlerContext, response: WebSocketFrame, channel: Channel) {
    if (response.getTextData != "megrez-server:1.0") throw new NotMegrezServerException(server)
    callback.connected
  }

  private def handshakeRequest = {
    val request = new DefaultHttpRequest(HTTP_1_1, GET, server.getPath)
    request.addHeader(Names.UPGRADE, WEBSOCKET)
    request.addHeader(CONNECTION, Values.UPGRADE)
    request.addHeader(HOST, if (server.getPort == 80) server.getHost else server.getHost + ":" + server.getPort)
    request.addHeader(ORIGIN, "http://" + server.getHost)
    request
  }
}

class AgentHandler(val callback: ServerHandler, val actor : Actor) extends SimpleChannelUpstreamHandler {
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val assignment = e.getMessage match {
      case frame : WebSocketFrame => actor ! JobAssignment.parse(frame.getTextData)
      case _ =>
    }
  }

  override def channelDisconnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    callback.disconnected
  }

  override def exceptionCaught(context: ChannelHandlerContext, event: ExceptionEvent) {
    super.exceptionCaught(context, event)
  }
}

class HandlerHolder(var handler: SimpleChannelUpstreamHandler) extends SimpleChannelUpstreamHandler {
  override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    handler.channelConnected(context, event)
  }

  override def channelDisconnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    handler.channelDisconnected(context, event)
  }

  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    handler.messageReceived(context, event)
  }

  override def exceptionCaught(context: ChannelHandlerContext, event: ExceptionEvent) {    
    handler.exceptionCaught(context, event)
  }
}
