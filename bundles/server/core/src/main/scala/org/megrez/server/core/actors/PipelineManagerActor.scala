package org.megrez.server.core.actors

import scala.actors.Actor
import org.megrez.server.core.PipelineManager

class PipelineManagerActor extends Actor with PipelineManager {
  def act {
    loop{
      react{
        case "ADD" =>
          reply("ok")
        case "STOP" => exit
        case _ =>
      }
    }
  }

  start

  def addPipeline(name: String) = this !? "ADD"
}