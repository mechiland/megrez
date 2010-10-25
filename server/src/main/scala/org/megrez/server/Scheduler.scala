package org.megrez.server

import actors._
import scala.collection.mutable._
import scala.collection.immutable.Set

class Scheduler extends Actor {

  private var _jobs = new HashSet[Job]()
  private var _agents = new HashSet[Actor]()

  def act() {
    loop {
      react {
        case trigger: TriggerMessage => handleTrigger(trigger)
        case connection: AgentConnect => registerAgent(connection.agent)
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
    val job = new Job(trigger.pipeline, Set(), List())
    _jobs.add(job)
    _agents.foreach(_ ! new JobRequest(job))
  	reply(Success())
}

}
