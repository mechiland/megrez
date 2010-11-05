package org.megrez.server

import actors._
import java.util.UUID

abstract class CommonMessage
case class Success() extends CommonMessage
case class Exit() extends CommonMessage

abstract class AgentMessage
case class SetResources(val resources : Set[String]) extends AgentMessage
case class RemoteAgentConnected(val handler : AgentHandler) extends AgentMessage

case class AgentConnect(val agent : Actor)

abstract class JobMessage
case class JobRequest(val buildId: UUID, val job : Job) extends JobMessage
case class JobConfirm(val agent : Agent, val jobRequest: JobRequest) extends JobMessage
case class JobReject(val agent : Agent) extends JobMessage
case class JobFinished(val buildId: UUID, val job: Job, val agent : Actor) extends JobMessage

case class AddPipeline(config : Pipeline)
case class PipelineChanged(config : Pipeline)
case class RemovePipeline(config : Pipeline)

case class TriggerBuild(config : Pipeline)

case class JobScheduled(buildId : UUID, jobs : Set[Job]) {
  def jobRequests: Set[JobRequest] = {
    jobs.map(job => JobRequest(buildId, job))
  }
}
case class JobCompleted(buildId : UUID, job : Job)
case class JobFailed(buildId : UUID, job : Job)

case class BuildFailed(buildId : Build)
case class BuildCompleted(buildId : Build)
