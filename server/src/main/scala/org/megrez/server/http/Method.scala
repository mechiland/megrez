package org.megrez.server.http

object Method extends Enumeration {
  type Method = Value
  val GET = Value("GET")
  val PUT = Value("PUT")
  val DELETE = Value("DELETE")
  val POST = Value("POST")
}
