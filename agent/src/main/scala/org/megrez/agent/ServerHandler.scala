package org.megrez.agent

import java.net.URI

trait ServerHandler {
  def megrezServerConnected()
  def invalidMegrezServer(uri: URI)
}