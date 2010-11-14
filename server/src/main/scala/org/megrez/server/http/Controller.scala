package org.megrez.server.http

import actors._
import org.megrez.Pipeline
import org.megrez.util.JSON
import org.megrez.server.{Megrez, ToPipelineManager}

class PipelineController(val pipelineManager: Actor) extends Actor {
  def act {
    loop {
      react {
        case Request(Method.POST, _, content) =>
          try {
            pipelineManager ! ToPipelineManager.AddPipeline(JSON.read[Pipeline](Request.parse(content)("pipeline")))
            reply(HttpResponse.OK)
          } catch {
            case _ : Exception => reply(HttpResponse.ERROR) 
          }
        case _ =>
      }
    }
  }

  start
}

class Controllers(val megrez: Megrez) {
  val pipeline = new PipelineController(megrez.pipelineManager)
}