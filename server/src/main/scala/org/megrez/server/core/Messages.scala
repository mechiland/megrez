package org.megrez.server.core

import actors._
import java.util.UUID
import org.megrez.server.model.{Build, Pipeline, Change}
import org.megrez.{JobAssignmentFuture, JobAssignment}

object ToAgentManager {
  case class RemoteAgentConnected(val handler: AgentHandler)
}

object AgentToDispatcher {
  object Confirm
  object Reject
  case class JobCompleted(val agent: Actor, val assignment: JobAssignment)
  case class JobFinished(val agent: Actor, val assignment: JobAssignmentFuture, val isFailed: Boolean = false)
  case class JobFailed(val agent: Actor, val assignment: JobAssignment)
}

object TriggerToScheduler {
  case class TriggerBuild(pipeline: Pipeline, changes: Set[Change])
}

object AgentManagerToDispatcher {
  case class AgentConnect(agent: Actor)
}

object SchedulerToDispatcher {
  case class JobScheduled(val buildId: UUID, val assignments: Set[JobAssignment])
  case class CancelBuild(val buildId: UUID)
}

object DispatcherToScheduler {
  case class JobFinished(build: Build, operation: () => Unit)
}
