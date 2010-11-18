package org.megrez.server.http

import Method._
import java.net.URLDecoder
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, DefaultHttpResponse}
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.buffer.{ChannelBuffers}
import org.jboss.netty.util.CharsetUtil

case class Request(val method: Method, val uri: String, val content: String)
object Request {
  def parse(content: String) = content.split("&").map(parameter => {
    val Array(key, value) = parameter.split("=")
    key -> URLDecoder.decode(value, "UTF-8")
  }).toMap

}
case class WebSocket()

object HttpResponse {
  val OK = new HttpResponse(HttpResponseStatus.OK)
  val ERROR = new HttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR)
}

class HttpResponse(val status: HttpResponseStatus) extends DefaultHttpResponse(HTTP_1_1, status) {
  def this(content: String) = {
    this(HttpResponseStatus.OK)
    setContent(ChannelBuffers.copiedBuffer(content, CharsetUtil.UTF_8))
    this
  }
}