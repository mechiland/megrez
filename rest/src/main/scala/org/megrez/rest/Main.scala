package org.megrez.rest


import org.restlet.data.Protocol
import org.restlet.Server

object Main {
  def main(args: Array[String]) {
    new Server(Protocol.HTTP, 8080, classOf[PipelineResource]).start  
  }
}