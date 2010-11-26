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

    it("should be scheduled if all jobs are scheduled") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.status should equal(StageExecution.Status.Scheduled)
    }

    it("should be running if one job scheduled one job running") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.start
      execution.status should equal(StageExecution.Status.Running)
    }

    it("should be running if one job completed one job running") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.start
      execution.jobs.last.completed
      execution.status should equal(StageExecution.Status.Running)
    }

    it("should be running if two job running") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.start
      execution.jobs.last.start
      execution.status should equal(StageExecution.Status.Running)
    }

    it("should be completed if two job completed") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.completed
      execution.jobs.last.completed
      execution.status should equal(StageExecution.Status.Completed)
    }

    it("should be failling if one job failed and one job scheduled") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.failed
      execution.status should equal(StageExecution.Status.Failing)      
    }

    it("should be failling if one job failed and one job running") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.failed
      execution.jobs.last.start
      execution.status should equal(StageExecution.Status.Failing)
    }

    it("should be failed if one job failed and one job completed") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.failed
      execution.jobs.last.completed
      execution.status should equal(StageExecution.Status.Failed)
    }

    it("should be failed if two jobs failed") {
      val job1 = Job(Map("name" -> "server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val stage = Stage(Map("name" -> "test", "jobs" -> List(job1, job2)))
      val execution = StageExecution(stage)
      execution.jobs.head.failed
      execution.jobs.last.failed
      execution.status should equal(StageExecution.Status.Failed)
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