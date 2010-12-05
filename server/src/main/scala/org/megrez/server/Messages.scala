package org.megrez.server

import actors._
import java.util.UUID
import org.megrez._

object ToAgentManager {
  case class RemoteAgentConnected(val handler: AgentHandler)
}

object ToAgent {
  case class SetResources(val resources: Set[String])
}
object ToPipelineManager {
  case class AddPipeline(val pipeline: Pipeline)
  case class PipelineChanged(val pipeline: Pipeline)
  case class RemovePipeline(val pipeline: Pipeline)
  case class TriggerPipeline(val pipeline: Pipeline)
}

object ToBuildManager {
  case class CompletedBuilds()
}

object AgentToDispatcher {
  object Confirm
  object Reject
  case class JobCompleted(val agent: Actor, val assignment: JobAssignment)
  case class JobFinished(val agent: Actor, val assignment: JobAssignmentFuture, val isFailed : Boolean =false)
  case class JobFailed(val agent: Actor, val assignment: JobAssignment)
}

object TriggerToScheduler {
  case class TrigBuild(val pipeline: Pipeline, val changes: Map[Material, Option[Any]])
}

object AgentManagerToScheduler {
  case class CancelBuild(val build: UUID)
}

object AgentManagerToDispatcher {
  case class AgentConnect(val agent: Actor)
}

object SchedulerToDispatcher {
  case class JobScheduled(val buildId: UUID, val assignments: Set[JobAssignment])
  case class CancelBuild(val buildId: UUID)
}

object DispatcherToScheduler {
  case class JobCompleted(val build: UUID, val job: Job)
  case class JobFailed(val build: UUID, val job: Job)
  case class BuildCanceled(val build: UUID, val assignments: Set[Job])
}

object SchedulerToBuildManager {
  case class BuildFailed(build: Build)
  case class BuildCompleted(build: Build)
  case class BuildCanceled(build: Build)
}
