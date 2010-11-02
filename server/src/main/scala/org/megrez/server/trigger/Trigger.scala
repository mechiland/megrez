package org.megrez.server.trigger

import actors.Actor
import org.megrez.server.{TriggerBuild, Pipeline}

trait Trigger {
  val pipeline: Pipeline
  val target: Actor

  def checkAndTrigger = {
    if (pipeline.checkChange()) {
      target ! new TriggerBuild(pipeline)
    }
  }
}




