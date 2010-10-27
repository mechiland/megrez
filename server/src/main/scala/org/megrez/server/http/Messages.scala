package org.megrez.server.http

import Method._

case class Request(val method : Method)
case class WebSocket