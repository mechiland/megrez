package org.megrez.server.http

import Method._
import java.net.URLDecoder

case class Request(val method: Method, val uri: String, val content: String)
object Request {
  def parse(content: String) = content.split("&").map(parameter => {
    val Array(key, value) = parameter.split("=")
    key -> URLDecoder.decode(value, "UTF-8")
  }).toMap

}
case class WebSocket()

object HttpResponse {
  object OK
  object ERROR
}