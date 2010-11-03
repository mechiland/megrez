package org.megrez.server.http

import actors._
import org.megrez.server.{PipelineManager, AddPipeline}

class PipelineController(val pipelineManager: PipelineManager) extends Actor {
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
      case Method.POST =>  pipelineManager ! AddPipeline(request.resource)
    }
  }
  
}