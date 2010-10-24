package org.megrez.agent

import java.net.URI

trait ServerHandler {  
  def connected() {}
  def disconnected() {}
  def invalidServer(uri: URI) {}
}