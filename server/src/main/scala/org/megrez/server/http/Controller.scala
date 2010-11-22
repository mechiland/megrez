package org.megrez.server.http

import actors._
import org.megrez.Pipeline
import org.megrez.util.JSON
import org.megrez.server._
class PipelineController(val pipelineManager: Actor) extends Actor {
  def act {
    loop {
      react {
        case Request(Method.POST, _, content) =>
          try {
            pipelineManager ! ToPipelineManager.AddPipeline(JSON.read[Pipeline](Request.parse(content)("pipeline")))
            reply(HttpResponse.OK)
          } catch {
            case e: Exception =>
              e.printStackTrace
              reply(HttpResponse.ERROR)
          }
        case _ =>
      }
    }
  }

  start
}

class BuildsController(val megrez: Megrez) extends Actor {
  def act {
    loop {
      react {
        case Request(Method.GET, uri, content) =>
          try {
            if(uri.endsWith(".js")) {
              reply(new HttpResponse("onStatusChange(" + megrez.pipelinesJson + ")"))
            }
            else
              reply(new HttpResponse(megrez.pipelinesJson))
          } catch {
            case _: Exception => reply(HttpResponse.ERROR)
          }
        case _ =>
      }
    }
  }

  start
}

class Controllers(val megrez: Megrez) {
  val pipeline = new PipelineController(megrez.pipelineManager)
  val builds = new BuildsController(megrez)
}
