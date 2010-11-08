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
case class JobRequest(val buildId: UUID, val job : Job) extends JobMessage {
  def receivedMessage = {
    "received a job request: %s for build %s".format(buildId.toString, job.name)
  }
}
case class JobConfirm(val agent : Agent, val jobRequest: JobRequest) extends JobMessage
case class JobReject(val agent : Agent) extends JobMessage
case class JobFinished(val buildId: UUID, val job: Job, val agent : Actor) extends JobMessage

case class AddPipeline(pipeline : Pipeline)
case class PipelineChanged(pipeline : Pipeline)
case class RemovePipeline(pipeline : Pipeline)

case class JobScheduled(buildId : UUID, assignments : Set[JobAssignment])

object AgentToDispatcher {
  object Confirm
  object Reject
}

case class JobCompleted(buildId : UUID, job : Job)
case class JobFailed(buildId : UUID, job : Job)

case class BuildFailed(buildId : Build)
case class BuildCompleted(buildId : Build)
