package org.megrez.agent

import java.net._

import java.io.File
import org.megrez.util.FileWorkspace

object Main {
  private var worker : Worker = null
  private var server : Server = null
  
  def start(url : String, workingDir : File) {
    worker = new Worker(new FileWorkspace(workingDir))
    worker.start
    server = new Server(new URI(url), 5000, worker)
    server.connect
  }

  def stop() {
    server.shutdown
  }

  def main(args: Array[String]) {
    start(args.head, new File(args.last))
  }
}



