package org.megrez.server

import trigger.AutoTrigger
import actors.Actor

object Initializer {
  val agentManager : Actor = new AgentManager(this)
  val buildScheduler: Actor = new BuildScheduler(this)
  val buildManager : Actor = new PersistanceManager()
  val dispatcher: Actor = new Dispatcher(this)
  val pipelineManager = new PipelineManager(this)

  val triggerFactory : Pipeline => Trigger = pipeline => new AutoTrigger(pipeline, buildScheduler)  

  def stopApp() {
    dispatcher ! Exit()
    buildScheduler ! Exit()
    pipelineManager ! Exit()
  }  
}
class PersistanceManager extends Actor {
  def act() {}
}
