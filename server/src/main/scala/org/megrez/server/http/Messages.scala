package org.megrez.server.http

import Method._

case class Request(val method : Method, val uri: String, val content: String)
case class WebSocket()