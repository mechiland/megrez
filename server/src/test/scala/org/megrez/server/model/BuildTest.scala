package org.megrez.server.model

import data.Graph
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{BeforeAndAfterAll, Spec}
import tasks.{CommandLine, Ant}
import vcs.Subversion

import actors.Actor._
import actors.TIMEOUT

class BuildTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Build") {
    it("should return jobs from current stage") {
      val job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job)))))
      val build = Build(pipeline, Set())
      val jobs = build.next
      jobs should have size (1)
      jobs.head.job should equal(job)
    }

    it("should not return anything if current stage not finished") {
      val job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job)))))
      val build = Build(pipeline, Set())
      build.next
      val jobs = build.next
      jobs should have size (0)
    }

    it("should return jobs of next stage when first stage successful") {
      val job1 = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "publish", "tasks" -> List(Map("type" -> "cmd", "command" -> "publish"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job1)), Map("name" -> "publish", "jobs" -> List(job2)))))
      val build = Build(pipeline, Set())
      build.next.head.completed
      val jobs = build.next
      jobs should have size (1)
      jobs.head.job should equal(job2)
    }

    it("should mark as completed if all stages completed") {
      val job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job)))))
      val build = Build(pipeline, Set())
      build.next.head.completed
      build.next should have size (0)
      build.status() should equal(Build.Status.Completed)
    }

    it("should mark as failed if any stages failed") {
      val job1 = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "publish", "tasks" -> List(Map("type" -> "cmd", "command" -> "publish"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job1)), Map("name" -> "publish", "jobs" -> List(job2)))))
      val build = Build(pipeline, Set())
      build.next.head.failed
      build.next should have size (0)
      build.status() should equal(Build.Status.Failed)
    }

    it("should mark as failing if any stages failing") {
      val job1 = Job(Map("name" -> "ut-server", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "ut-agent", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job3 = Job(Map("name" -> "publish", "tasks" -> List(Map("type" -> "cmd", "command" -> "publish"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job1, job2)), Map("name" -> "publish", "jobs" -> List(job3)))))
      val build = Build(pipeline, Set())
      build.next.head.failed
      build.next should have size (0)
      build.status() should equal(Build.Status.Failing)
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