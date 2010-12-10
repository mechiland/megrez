package org.megrez.server.model

import data.Graph
import vcs.Subversion
import tasks.{Ant, CommandLine}
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{Spec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

class JobExecutionTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Job execution") {
    it("should be scheduled when job exection first created") {
      val execution = JobExecution(job)
      execution.status() should equal(JobExecution.Status.Scheduled)
    }
              
    it("should mark execution running") {
      val execution = JobExecution(job)
      execution.start
      execution.status() should equal(JobExecution.Status.Running)
    }

    it("should mark execution completed") {
      val execution = JobExecution(job)
      execution.completed
      execution.status() should equal(JobExecution.Status.Completed)
    }

    it("should mark execution failed") {
      val execution = JobExecution(job)
      execution.failed
      execution.status() should equal(JobExecution.Status.Failed)
    }

    it("should set and get consoleoutput"){
       val execution = JobExecution(job)
      val executionTarget = execution
      executionTarget.appendConsoleOutput("console")

      execution.consoleOutput should equal("console")
    }
  }

  var job: Job = null

  override def beforeAll() {    
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, StageExecution, JobExecution)
    Graph.consistOf(CommandLine, Ant, Subversion)
    job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
  }

  override def afterAll() {
    Neo4J.shutdown
  }
}