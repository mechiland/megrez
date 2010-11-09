package org.megrez.server

import actors._
import java.util.UUID
import org.megrez.{JobAssignment, Material, Pipeline, Job}

case class TrigBuild(val pipeline : Pipeline, val changes : Map[Material, Option[Any]])

abstract class CommonMessage
case class Success() extends CommonMessage
case class Exit() extends CommonMessage

abstract class AgentMessage
case class SetResources(val resources : Set[String]) extends AgentMessage
case class RemoteAgentConnected(val handler : AgentHandler) extends AgentMessage

case class AgentConnect(val agent : Actor)

abstract class JobMessage
case class JobFinished(val buildId: UUID, val job: Job, val agent : Actor) extends JobMessage


object ToAgent {
  case class SetResources(val resources : Set[String])
}
object ToPipelineManager {
  case class AddPipeline(val pipeline : Pipeline)
  case class PipelineChanged(val pipeline : Pipeline)
  case class RemovePipeline(val pipeline : Pipeline)
}

object AgentToDispatcher {
  object Confirm
  object Reject
  case class JobCompleted(val agent : Actor, val assignment : JobAssignment)
  case class JobFailed(val agent : Actor, val assignment : JobAssignment)
}

object SchedulerToDispatcher {
  case class JobScheduled(val buildId : UUID, val assignments : Set[JobAssignment])
}

object DispatcherToScheduler {
  case class JobCompleted(val build : UUID, val job : Job)
  case class JobFailed(val build: UUID, val job : Job)
}

case class BuildFailed(buildId : Build)
case class BuildCompleted(buildId : Build)
