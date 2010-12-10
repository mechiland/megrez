package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class JobExecution private(val node: Node) extends Entity {
  import JobExecution.Status._

  val job = read(JobExecution.job)
  val status = reader(JobExecution.status)

  def start = write(JobExecution.status, Running)

  def completed = write(JobExecution.status, Completed)

  def failed = write(JobExecution.status, Failed)
  
  def consoleOutput:String = read(JobExecution.consoleOutput) match{
    case null => ""
    case console:String => console
  }
  def appendConsoleOutput(output: String) = write(JobExecution.consoleOutput, consoleOutput+output)
}

object JobExecution extends Meta[JobExecution] {
  val job = reference("job", Job, DynamicRelationshipType.withName("FOR_JOB"))
  val consoleOutput = property[String]("consoleOutput")
  val status = enum("status", Status)

  def apply(node: Node) = new JobExecution(node)

  def apply(job: Job): JobExecution = JobExecution(Map("job" -> job, "status" -> Status.Scheduled))

  object Status extends Enumeration {
    type Status = Value
    val Scheduled, Running, Completed, Failed = Value
  }
}