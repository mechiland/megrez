package org.megrez.server

import actors.Actor

trait Trigger {
  val pipeline: Pipeline
  val target: Actor

  def start

  def stop

  def checkAndTrigger = {
    if (pipeline.checkChange()) {
      target ! new TriggerBuild(pipeline)
    }
  }
}

class PipelineTrigger(val pipeline : org.megrez.Pipeline, val buildScheduler : Actor) {
  
}