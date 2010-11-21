package org.megrez.util

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez._
import org.megrez.Pipeline.Stage
import task.{AntTask, CommandLineTask}
import vcs.{Git, Subversion}

class JsonTest extends Spec with ShouldMatchers {
  describe("Domain object serialization") {
    it("should serialize svn material") {
      val material = new Material(new Subversion("svn_url"), "dest")
      JSON.write(material) should equal("""{"type":"svn","url":"svn_url","dest":"dest"}""")
    }

    it("should deserialize svn material") {
      val json = """{"type" : "svn", "url" : "svn_url", "dest" : "dest"}"""
      val material = JSON.read[Material](json)
      material.source.isInstanceOf[Subversion] should equal(true)
      material.source.asInstanceOf[Subversion].url should equal("svn_url")
      material.destination should equal("dest")
    }

    it("should serialize git material") {
      val material = new Material(new Git("git_url"), "dest")
      JSON.write(material) should equal("""{"type":"git","url":"git_url","dest":"dest"}""")
    }

    it("should deserialize git material") {
      val json = """{"type" : "git", "url" : "git_url", "dest" : "dest"}"""
      val material = JSON.read[Material](json)
      material.source.isInstanceOf[Git] should equal(true)
      material.source.asInstanceOf[Git].url should equal("git_url")
      material.destination should equal("dest")
    }

    it("should serialize job") {
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")), List(new Artifact("/target/**/*.jar", Set("artifact"))))
      JSON.write(job) should equal("""{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"/target/**/*.jar","tags":["artifact"]}]}""")
    }

    it("should deserialize job") {
      val json = """{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"/target/**/*.jar","tags":["artifact"]}]}"""
      val job = JSON.read[Job](json)
      job.name should equal("job")
      job.resources should equal(Set("LINUX"))
      job.tasks should have size (1)
      val task = job.tasks.head
      task.isInstanceOf[CommandLineTask] should equal(true)
      task.asInstanceOf[CommandLineTask].command should equal("ls")
      job.artifacts should have size (1)
      val artifact: Artifact = job.artifacts.head
      artifact.path should equal("/target/**/*.jar")
      artifact.tags should equal(Set("artifact"))
    }

    it("should serialize stage") {
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")))
      val stage = new Stage("stage", Set(job))
      JSON.write(stage) should equal("""{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}]}""")
    }

    it("should deserialize stage") {
      val json = """{"name":"stage", "jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"/target/**/*.jar","tags":["artifact"]}]}]}"""
      val stage = JSON.read[Stage](json)
      stage.name should equal("stage")
      stage.jobs should have size (1)
      val job = stage.jobs.head
      job.name should equal("job")
      job.resources should equal(Set("LINUX"))
      job.tasks should have size (1)
      val task = job.tasks.head
      task.isInstanceOf[CommandLineTask] should equal(true)
      task.asInstanceOf[CommandLineTask].command should equal("ls")
    }

    it("should serialize pipeline") {
      val material = new Material(new Subversion("svn_url"), "dest")
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")))
      val stage = new Stage("stage", Set(job))
      val pipeline = new Pipeline("pipeline", Set(material), List(stage))
      JSON.write(pipeline) should equal("""{"name":"pipeline","materials":[{"type":"svn","url":"svn_url","dest":"dest"}],"stages":[{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}]}]}""")
    }

    it("should deserialize pipeline") {
      val json = """{"name":"pipeline","materials":[{"type":"svn","url":"svn_url","dest":"dest"}],"stages":[{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"/target/**/*.jar","tags":["artifact"]}]}]}]}"""
      val pipeline = JSON.read[Pipeline](json)
      pipeline.name should equal("pipeline")
      pipeline.materials should have size (1)
      val material = pipeline.materials.head
      material.source.isInstanceOf[Subversion] should equal(true)
      material.source.asInstanceOf[Subversion].url should equal("svn_url")
      material.destination should equal("dest")
      pipeline.stages should have size (1)
      val stage = pipeline.stages.head
      stage.name should equal("stage")
      stage.jobs should have size (1)
      val job = stage.jobs.head
      job.name should equal("job")
      job.resources should equal(Set("LINUX"))
      job.tasks should have size (1)
      val task = job.tasks.head
      task.isInstanceOf[CommandLineTask] should equal(true)
      task.asInstanceOf[CommandLineTask].command should equal("ls")
    }

    it("should seraialize artifact") {
      val artifact = new Artifact("/target/**/*.jar", Set("artifact"))
      JSON.write(artifact) should equal("""{"path":"/target/**/*.jar","tags":["artifact"]}""")
    }

    it("should deseraialize artifact") {
      val artifact = JSON.read[Artifact]("""{"path":"/target/**/*.jar","tags":["artifact"]}""")
      artifact.path should equal("/target/**/*.jar")
      artifact.tags should equal(Set("artifact"))
    }
  }

  describe("Task serialization") {

    it("should serialize command line task") {
      val task = new CommandLineTask("ls")
      JSON.write(task) should equal("""{"type":"cmd","command":"ls"}""")
    }

    it("should deserialize command line task") {
      val json = """{"type":"cmd","command":"ls"}"""
      val task = JSON.read[Task](json)
      task.isInstanceOf[CommandLineTask] should equal(true)
      task.asInstanceOf[CommandLineTask].command should equal("ls")
    }

    it("should serialize bare ant task") {
      val task = new AntTask(null, null)
      JSON.write(task) should equal("""{"type":"ant"}""")
    }

    it("should deserialize bare ant task") {
      JSON.read[AntTask]("""{"type":"ant"}""") should equal(new AntTask(null, null))
    }

    it("should serialize ant task with target") {
      val task = new AntTask("test", null)
      JSON.write(task) should equal("""{"type":"ant","target":"test"}""")
    }

    it("should deserialize ant task with target") {
      JSON.read[AntTask]("""{"type":"ant","target":"test"}""") should equal(new AntTask("test", null))
    }

    it("should serialize ant task with buildfile") {
      val task = new AntTask(null, "build.xml")
      JSON.write(task) should equal("""{"type":"ant","buildfile":"build.xml"}""")
    }

    it("should serialize ant task with target and buildfile") {
      val task = new AntTask("test", "build.xml")
      JSON.write(task) should equal("""{"type":"ant","target":"test","buildfile":"build.xml"}""")
    }

    it("should deserialize ant task with target and buildfile") {
      JSON.read[AntTask]("""{"type":"ant","target":"test","buildfile":"build.xml"}""") should equal(new AntTask("test", "build.xml"))
    }

  }

  describe("Agent message serialization") {
    it("should serialize job assignment") {
      val material = new Material(new Subversion("svn_url"), "dest")
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")))
      val assignment = JobAssignment("pipeline", Map(material -> Some(5)), job)
      JSON.write(assignment) should equal("""{"type":"assignment","pipeline":"pipeline","materials":[{"material":{"type":"svn","url":"svn_url","dest":"dest"},"workset":{"revision":5}}],"job":{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}}""")
    }

    it("should serialize job assignment for git") {
      val material = new Material(new Git("git_url"), "dest")
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")))
      val assignment = JobAssignment("pipeline", Map(material -> Some("abc")), job)
      JSON.write(assignment) should equal("""{"type":"assignment","pipeline":"pipeline","materials":[{"material":{"type":"git","url":"git_url","dest":"dest"},"workset":{"commit":"abc"}}],"job":{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}}""")
    }

    it("should deserialize job assignment for git") {
      val json = """{"type":"assignment","pipeline":"pipeline","materials":[{"material":{"type":"git","url":"git_url","dest":"dest"},"workset":{"commit":"abc"}}],"job":{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"/target/**/*.jar","tags":["artifact"]}]}}"""
      val assignment = JSON.read[JobAssignment](json)
      assignment.materials should have size (1)
      val (material, workset) = assignment.materials.head
      material.source match {
        case git: Git =>
          git.url should equal("git_url")
        case _ => fail
      }
      workset match {
        case Some("abc") =>
        case _ => fail
      }
    }


    it("should deserialize job assignment") {
      val json = """{"type":"assignment","pipeline":"pipeline","materials":[{"material":{"type":"svn","url":"svn_url","dest":"dest"},"workset":{"revision":5}}],"job":{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}],"artifacts":[{"path":"/target/**/*.jar","tags":["artifact"]}]}}"""
      JSON.read[AgentMessage](json) match {
        case JobAssignment(pipeline, materials, job) =>
          pipeline should equal("pipeline")
          materials should have size (1)
          val (material, workset) = materials.head
          material.source match {
            case subversion: Subversion =>
              subversion.url should equal("svn_url")
            case _ => fail
          }
          material.destination should equal("dest")
          workset match {
            case Some(5) =>
            case _ => fail
          }
        case _ => fail
      }
    }

    it("should serialize job complete") {
      val message = JobCompleted()
      JSON.write(message) should equal("""{"type":"jobcompleted"}""")
    }

    it("should deserialize job complete") {
      val json = """{"type":"jobcompleted"}"""
      JSON.read[AgentMessage](json) match {
        case message: JobCompleted =>
        case _ => fail
      }
    }

    it("should serialize console output") {
      val message = ConsoleOutput("Output")
      JSON.write(message) should equal("""{"type":"consoleoutput","content":"Output"}""")
    }

    it("should deserialize console output") {
      val json = """{"type":"consoleoutput","content":"Output"}"""
      JSON.read[AgentMessage](json) match {
        case ConsoleOutput(content) =>
          content should equal("Output")
        case _ => fail
      }
    }
  }
}
