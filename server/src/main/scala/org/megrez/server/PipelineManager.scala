package org.megrez.server

import actors.Actor
import collection.mutable.HashMap

class PipelineManager(val triggerFactory: PipelineConfig => Trigger) extends Actor {
  private val pipelines = HashMap[String, Pair[PipelineConfig, Trigger]]()

  def act {
    loop {
      react {
        case message: AddPipeline =>
          addPipeline(message.config)
        case message: PipelineChanged =>
          removePipeline(message.config.name)
          addPipeline(message.config)
        case _ =>
      }
    }
  }

  private def addPipeline(config: PipelineConfig): Option[(PipelineConfig, Trigger)] = {
    pipelines.put(config.name, Pair(config, launchTrigger(config)))
  }


  private def launchTrigger(config: PipelineConfig): Trigger = {
    val trigger = triggerFactory(config)
    trigger.start
    trigger
  }

  private def removePipeline(name: String) {
    pipelines.remove(name) match {
      case Some(Pair(_, trigger: Trigger)) => trigger.stop
      case None =>
    }
  }

  start
}
