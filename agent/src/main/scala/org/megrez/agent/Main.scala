package org.megrez.agent

import java.net._

import java.io.File

object Main {
  def main(args: Array[String]) {
    val worker = new Worker(new FileWorkspace(new File(args.last)))
    worker.start
    val serverConnection = new Server(new URI(args.head), 5000, worker)
    serverConnection.connect
  }
}



