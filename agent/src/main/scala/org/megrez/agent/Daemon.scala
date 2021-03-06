package org.megrez.agent

import java.io.File
import java.lang.String
import org.megrez.util.Logging

object Daemon extends Logging{

  def main(args: Array[String]) {
    val server = System.getProperty("megrez.server", "localhost")
    val port = System.getProperty("megrez.port", "8051")
    val dir = System.getProperty("megrez.agent.dir", "target")
    val url: String = String.format("ws://%s:%s/agent", server, port)
    Main.start(url, new File(dir))
    info(String.format("Started agent connected to server at %s", url))
    while(true) {}
  }

}