package org.megrez.server

import trigger.AutoTrigger
import actors.Actor

object Initializer {
  var dispatcher: Dispatcher = _
  var scheduler: BuildScheduler = _
  var pipelineManager: PipelineManager = _

  def initializeApp() {
    dispatcher = new Dispatcher()
    scheduler = new BuildScheduler(dispatcher, new PersistanceManager())
    dispatcher.buildScheduler = scheduler
    pipelineManager = new PipelineManager(triggerFactory)
  }

  def stopApp() {
    dispatcher ! Exit()
    scheduler ! Exit()
    pipelineManager ! Exit()
  }

  private def triggerFactory(pipeline: Pipeline): Trigger = {
    new AutoTrigger(pipeline, scheduler)
  }

}
class PersistanceManager extends Actor {
  def act() {}
}
