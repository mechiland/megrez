package org.megrez.server.core

import actors._
import org.megrez.server.model.{JobExecution, Build, Pipeline, Change}

object ToAgentManager {
  case class RemoteAgentConnected(val handler: AgentHandler)
}

object AgentToDispatcher {
  object Confirm
  object Reject
  case class JobFinished(val agent: Actor, val assignment: JobExecution, val isFailed: Boolean = false)
}

object TriggerToScheduler {
  case class TriggerBuild(pipeline: Pipeline, changes: Set[Change])
}

object AgentManagerToDispatcher {
  case class AgentConnect(agent: Actor)
}

object SchedulerToDispatcher {
}

object DispatcherToScheduler {
  case class JobFinished(build: Build, operation: () => Unit)
}
