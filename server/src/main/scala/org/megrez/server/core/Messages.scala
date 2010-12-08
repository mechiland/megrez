package org.megrez.server.core

import actors._
import java.util.UUID
import org.megrez.server.model.{Build, Pipeline, Change}
import org.megrez.{JobAssignmentFuture}

object ToAgentManager {
  case class RemoteAgentConnected(val handler: AgentHandler)
}

object AgentToDispatcher {
  object Confirm
  object Reject
  case class JobFinished(val agent: Actor, val assignment: JobAssignmentFuture, val isFailed: Boolean = false)
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
