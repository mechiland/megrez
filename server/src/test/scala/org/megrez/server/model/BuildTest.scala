package org.megrez.server.model

import data.Graph
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{IoSupport, Neo4JSupport}
import org.scalatest.{BeforeAndAfterAll, Spec}
import tasks.{CommandLine, Ant}
import vcs.Subversion

class BuildTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Build") {
    it("should return jobs from current stage") {
      val job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job)))))
      val build = Build(pipeline)
      val jobs = build.next
      jobs should have size (1)
      jobs.head.job should equal(job)
    }

    it("should return jobs of next stage when first stage successful") {
      val job1 = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "publish", "tasks" -> List(Map("type" -> "cmd", "command" -> "publish"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job1)), Map("name" -> "publish", "jobs" -> List(job2)))))
      val build = Build(pipeline)
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