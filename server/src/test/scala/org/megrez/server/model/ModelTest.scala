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
  }

  override def beforeEach() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, CommandLine, Job)
  }

  override def afterEach() {
    Neo4J.shutdown
  }
}