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
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))))))
      val build = Build(Map("pipeline" -> pipeline))
      val jobs = build.current().jobs
      jobs should have size (1)
      jobs.head.name should equal("ut")
    }

    it("should return jobs of next stage when first stage successful") {
      val job1 = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      val job2 = Job(Map("name" -> "publish", "tasks" -> List(Map("type" -> "cmd", "command" -> "publish"))))
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(job1)), Map("name" -> "publish", "jobs" -> List(job2)))))
      val build = Build(Map("pipeline" -> pipeline))
//      build.complete(job)
    }
  }
  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build)
    Graph.consistOf(CommandLine, Ant, Subversion)


  }

  override def afterAll() {
    Neo4J.shutdown
  }


}