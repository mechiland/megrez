package org.megrez.server

import actors._
import scala.collection.mutable._
import scala.collection.immutable.Set

class Scheduler extends Actor {
  private val _jobs = new HashSet[Job]()
  private val _agents = new HashSet[Actor]()

  def act() {
    loop {
      react {
        case trigger: TriggerMessage => handleTrigger(trigger)
        case connection: AgentConnect => registerAgent(connection.agent)
        case message: JobConfirm => handleJobConfirm(message)
        case message: JobFinished => handleJobFinished(message)
        case _: Exit => exit
      }
    }
  }

  def jobs = _jobs.toSet

  def agents = _agents.toSet

  private def registerAgent(agent: Actor) {
    _agents add agent
    reply(Success())
  }

  private def handleTrigger(trigger: TriggerMessage) {
    triggerStage(trigger.pipeline, "stage1")
  }

  private def handleJobConfirm(message: JobConfirm) {
    _jobs.remove(message.job)
    _agents.remove(message.agent)
  }

  private def handleJobFinished(message: JobFinished) {
    _agents.add(message.agent)
    if (Configuration.hasNextStage(message.pipeline, message.stage)) {
      triggerStage(message.pipeline, Configuration.nextStage(message.pipeline, message.stage))
    }
  }

  private def triggerStage(pipeline: String, stage: String) {
    val job = new Job(pipeline, Set(), List())
    _jobs.add(job)
    _agents.foreach(_ ! new JobRequest(pipeline, stage, job))
    reply(Success())
  }

}
