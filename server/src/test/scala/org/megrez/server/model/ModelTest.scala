package org.megrez.server.model

import data.Graph
import org.scalatest.matchers.ShouldMatchers
import org.megrez.server.{Neo4JSupport, IoSupport}
import tasks.{Ant, CommandLine}
import org.scalatest.{BeforeAndAfterAll, Spec}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType}
import vcs.{Git, Subversion}

class ModelTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport {
  describe("Model persistent") {
    it("should create Task for command task") {
      val task = Task(Map("type" -> "cmd", "command" -> "ls"))
      task match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }
    }

    it("should create Task for ant task") {
      val task = Task(Map("type" -> "ant", "target" -> "compile", "buildfile" -> "build.xml"))
      task match {
        case task: Ant => {
          task.target should equal("compile")
          task.buildFile should equal("build.xml")
        }
        case _ => fail
      }
    }

    it("should create Job") {
      val job = Job(Map("name" -> "ut",
        "resources" -> List("Windows"),
        "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))
      job.name should equal("ut")
      job.resources should equal(Set("Windows"))
      job.tasks should have size (1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }
    }

    it("should create stage") {
      val stage = Stage(Map("name" -> "test", "jobs" -> List(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))))
      stage.name should equal("test")
      stage.jobs should have size (1)
      val job = stage.jobs.head
      job.name should equal("ut")
      job.tasks should have size (1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }
    }

    it("should create svn change source") {
      val changeSource = ChangeSource(Map("type" -> "svn", "url" -> "svn_url"))
      changeSource match {
        case svn: Subversion =>
          svn.url should equal("svn_url")
        case _ => fail
      }
    }

    it("should create git change source") {
      val changeSource = ChangeSource(Map("type" -> "git", "url" -> "svn_url"))
      changeSource match {
        case git: Git =>
          git.url should equal("svn_url")
        case _ => fail
      }
    }

    it("should create material with source") {
      val material = Material(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url")))
      material.destination should equal("dest")
      material.changeSource match {
        case svn: Subversion =>
          svn.url should equal("svn_url")
        case _ => fail
      }
    }

    it("should create pipeline") {
      val pipeline = Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))))))
      pipeline.name should equal("pipeline")
      pipeline.materials should have size (1)
      val material = pipeline.materials.head
      material.destination should equal("dest")
      material.changeSource match {
        case svn: Subversion =>
          svn.url should equal("svn_url")
        case _ => fail
      }
      pipeline.stages should have size (1)
      val stage = pipeline.stages.head
      stage.name should equal("test")
      stage.jobs should have size (1)
      val job = stage.jobs.head
      job.name should equal("ut")
      job.tasks should have size (1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }
    }

    it("should create build") {
      val build = Build(Map("pipeline" -> Pipeline(Map("name" -> "pipeline",
        "materials" -> List(Map("destination" -> "dest", "source" -> Map("type" -> "svn", "url" -> "svn_url"))),
        "stages" -> List(Map("name" -> "test", "jobs" -> List(Map("name" -> "ut", "tasks" -> List(Map("type" -> "cmd", "command" -> "ls"))))))))))

      val pipeline = build.pipeline
      pipeline.name should equal("pipeline")
      pipeline.materials should have size (1)
      val material = pipeline.materials.head
      material.destination should equal("dest")
      material.changeSource match {
        case svn: Subversion =>
          svn.url should equal("svn_url")
        case _ => fail
      }
      pipeline.stages should have size (1)
      val stage = pipeline.stages.head
      stage.name should equal("test")
      stage.jobs should have size (1)
      val job = stage.jobs.head
      job.name should equal("ut")
      job.tasks should have size (1)
      job.tasks.head match {
        case task: CommandLine => task.command should equal("ls")
        case _ => fail
      }

      import scala.collection.JavaConversions._

      val relationships = build.pipeline.node.getRelationships(DynamicRelationshipType.withName("FOR_PIPELINE"), Direction.INCOMING)
      val list = relationships.toList
      list should have size (1)
      list.head.getStartNode should equal(build.node)
    }

    it("should create agent") {
      val agent = Agent(Map("resources" -> List("WINDOWS")))
      agent.resources() should equal(Set("WINDOWS"))
    }
  }

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, Agent)
    Graph.consistOf(CommandLine, Ant, Subversion, Git)
  }

  override def afterAll() {
    Neo4J.shutdown
  }
}