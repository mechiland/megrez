package org.megrez.server

import actors._
import java.util.UUID
import org.megrez.{JobAssignment, Material, Pipeline, Job}

object ToAgentManager {
  case class RemoteAgentConnected(val handler : AgentHandler)
}

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

object TriggerToScheduler {
  case class TrigBuild(val pipeline : Pipeline, val changes : Map[Material, Option[Any]])
}

object AgentManagerToDispatcher {
  case class AgentConnect(val agent : Actor)
}

object SchedulerToDispatcher {
  case class JobScheduled(val buildId : UUID, val assignments : Set[JobAssignment])
}

object DispatcherToScheduler {
  case class JobCompleted(val build : UUID, val job : Job)
  case class JobFailed(val build: UUID, val job : Job)
}

object SchedulerToBuildManager {
  case class BuildFailed(buildId : Build)
  case class BuildCompleted(buildId : Build)
}
