package org.megrez.server.http

import actors._
import org.megrez.server.ToPipelineManager
import org.megrez.Pipeline
import util.parsing.json.JSON

class PipelineController(val pipelineManager: Actor) extends Actor {
  import org.megrez.io.JSON._
  
  def act {
    loop {
      react {
        case request: Request => handleRequest(request)
        case _ =>
      }
    }
  }

  def handleRequest(request: Request){
    request.method match{
      case Method.POST =>  pipelineManager ! ToPipelineManager.AddPipeline(read[Pipeline](JSON.parseFull(request.content).get))
    }
  }

  start  
}