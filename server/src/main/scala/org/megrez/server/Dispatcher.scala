package org.megrez.server

import actors._
import scala.collection.mutable._
import scala.collection.immutable.Set

class Dispatcher extends Actor {
  private val jobQueue = new HashSet[Job]()
  private val idleAgents = new HashSet[Actor]()

  def act() {
    loop {
      react {
        case message: TriggerMessage => trigger(message.pipelineName, Configuration.firstStage(message.pipelineName))
        case connection: AgentConnect => registerAgent(connection.agent)
        case message: JobConfirm => handleJobConfirm(message)
        case message: JobFinished => handleJobFinished(message)
        case _: Exit => exit
      }
    }
  }

  def jobs = jobQueue.toSet

  def agents = idleAgents.toSet

  private def registerAgent(agent: Actor) {
    idleAgents add agent
    reply(Success())
  }

  private def handleJobConfirm(message: JobConfirm) {
    jobQueue.remove(message.job)
    idleAgents.remove(message.agent)
  }

  private def handleJobFinished(message: JobFinished) {
    idleAgents.add(message.agent)
    if (Configuration.hasNextStage(message.pipeline, message.stage)) {
      trigger(message.pipeline, Configuration.nextStage(message.pipeline, message.stage))
    }
  }

  private def trigger(pipeline: String, stage: String) {
    val job = new Job(pipeline, Set(), List())
    jobQueue.add(job)
    idleAgents.foreach(_ ! new JobRequest(pipeline, stage, job))
    reply(Success())
  }

}
