package org.megrez.server

import actors._
import scala.collection.mutable._
import org.megrez._
import util.{JSON, Logging}
import java.io.{OutputStream, FileOutputStream, InputStream, File}

class Agent(handler: AgentHandler, dispatcher: Actor) extends Actor with Logging {
  private val resources = new HashSet[String]()
  private var current: Option[JobAssignment] = None
  private val output = new StringBuffer
  private val error = new StringBuffer

  def act() {
    loop {
      react {
        case assignment: JobAssignment =>
          handleAssignment(assignment)
        case ConsoleOutput(content) =>
          output.append(content)
        case ToAgent.SetResources(tags) =>
          resources.clear
          tags.foreach(resources add _)
        case artifact: ArtifactStream =>
          handleArtifact(artifact)
        case _: JobCompleted =>
          val assignment = current.get
          info("Job completed " + assignment.pipeline + " " + assignment.job.name)
          dispatcher ! AgentToDispatcher.JobCompleted(this, assignment)
          cleanup
        case _: JobFailed =>
          val assignment = current.get
          info("Job failed " + assignment.pipeline + " " + assignment.job.name)
          dispatcher ! AgentToDispatcher.JobFailed(this, assignment)
          cleanup
        case Stop => exit
      }
    }
  }

  private def cleanup {
    current = None
    output.delete(0, output.length)
    error.delete(0, error.length)
  }

  private def handleArtifact(artifact: ArtifactStream) {
    val zipFile: File = new File("/tmp/artifact.zip")
    if (zipFile.exists) zipFile.delete
    zipFile.createNewFile
    transfer(artifact.input, new FileOutputStream(zipFile))
  }

  private def transfer(input: InputStream, out: OutputStream)
  {
    val buffer = new Array[Byte](1024)
    var read = input.read(buffer)
    while (read > 0)
    {
      out.write(buffer, 0, read)
      read = input.read(buffer)
    }
  }

  private def handleAssignment(assignment: JobAssignment) {
    current match {
      case None =>
        if (checkResource(assignment.job)) {
          handler.send(JSON.write(assignment))
          current = Some(assignment)
          info("Confirm job " + assignment.pipeline + " " + assignment.job.name)
          reply(AgentToDispatcher.Confirm)
        } else {
          debug("Reject job " + assignment.pipeline + " " + assignment.job.name)
          reply(AgentToDispatcher.Reject)
        }
      case Some(_) =>
        debug("Reject job due to agent busy")
        reply(AgentToDispatcher.Reject)
    }
  }

  private def checkResource(job: Job) = job.resources.forall(resources contains _)

  start
}

trait AgentHandler {
  def assignAgent(agent: Actor)

  def send(message: String)
}
