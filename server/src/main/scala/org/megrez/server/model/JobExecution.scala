package org.megrez.server.model

import data.{Entity, Meta}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class JobExecution private(val node: Node) extends Entity {
  val job = read(JobExecution.job)
}

object JobExecution extends Meta[JobExecution] {
  val job = reference("job", Job, DynamicRelationshipType.withName("FOR_JOB"))

  def apply(node: Node) = new JobExecution(node)

  def apply(job: Job) : JobExecution = JobExecution(Map("job" -> job))

}