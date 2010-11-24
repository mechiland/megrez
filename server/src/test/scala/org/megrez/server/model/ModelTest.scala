package org.megrez.server.model

import data.Graph
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{Neo4JSupport, IoSupport}
import org.scalatest.{BeforeAndAfterEach, Spec}
import tasks.CommandLine

class ModelTest extends Spec with ShouldMatchers with BeforeAndAfterEach with IoSupport with Neo4JSupport {
  describe("Model persistent") {
    it("should create Task for specified type") {
      val task = Task(Map("type" -> "cmd", "command" -> "ls"))
      task match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }
    }
    
    it("should create Job") {
      val job = Job(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      job.name should equal("ut")
      job.tasks should have size(1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }
    }

    it("should create stage") {
      val stage = Stage(Map("name" -> "test", "jobs"->List(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))))
      stage.name should equal("test")
      stage.jobs should have size(1)
      val job = stage.jobs.head
      job.name should equal("ut")
      job.tasks should have size(1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }      
    }

    it("should create pipeline") {
      val pipeline = Pipeline(Map("name" -> "pipeline", "stages"-> List(Map("name" -> "test", "jobs"->List(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))))))
      pipeline.name should equal("pipeline")
      pipeline.stages should have size(1)
      val stage = pipeline.stages.head
      stage.name should equal("test")
      stage.jobs should have size(1)
      val job = stage.jobs.head
      job.name should equal("ut")
      job.tasks should have size(1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }    
    }
  }

  override def beforeEach() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, CommandLine, Job, Stage, Pipeline)
  }

  override def afterEach() {
    Neo4J.shutdown
  }
}