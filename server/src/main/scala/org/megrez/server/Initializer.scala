package org.megrez.server

import trigger.AutoTrigger
import actors.Actor

object Initializer {
  val dispatcher: Actor = new Dispatcher(this)
  val buildScheduler: Actor = new BuildScheduler(this)
  val buildManager : Actor = new PersistanceManager()
  val pipelineManager = new PipelineManager(this)


  def initializeApp() {
  }

  def stopApp() {
    dispatcher ! Exit()
    buildScheduler ! Exit()
    pipelineManager ! Exit()
  }   

  val triggerFactory : Pipeline => Trigger = pipeline => new AutoTrigger(pipeline, buildScheduler)  

}
class PersistanceManager extends Actor {
  def act() {}
}
