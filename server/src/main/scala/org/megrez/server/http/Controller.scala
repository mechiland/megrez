package org.megrez.server.http

import actors._
import org.megrez.server.ToPipelineManager
import org.megrez.Pipeline
import org.megrez.util.JSON

class PipelineController(val pipelineManager: Actor) extends Actor {
  
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
      case Method.POST =>  pipelineManager ! ToPipelineManager.AddPipeline(JSON.read[Pipeline](request.content))
    }
  }

  start  
}