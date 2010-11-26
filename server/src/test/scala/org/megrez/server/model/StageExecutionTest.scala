package org.megrez.server.model

import data.Graph
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{BeforeAndAfterAll, Spec}
import tasks.{Ant, CommandLine}
import vcs.Subversion

class StageExecutionTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Stage execution") {
    it("should return job when stage execution scheduled") {
      val job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job)))
      val execution = StageExecution(stage)
      execution.jobs should have size(1)
      execution.jobs.head.job should equal(job) 
    }
  }

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion)
  }

  override def afterAll() {
    Neo4J.shutdown
  }
}