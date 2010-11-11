package org.megrez.util

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez._
import task.CommandLineTask
import org.megrez.Pipeline.Stage
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



    it("should serialize task") {
      val task = new CommandLineTask("ls")
      JSON.write(task) should equal("""{"type":"cmd","command":"ls"}""")
    }

    it("should deserialize task") {
      val json = """{"type":"cmd","command":"ls"}"""
      val task = JSON.read[Task](json)
      task.isInstanceOf[CommandLineTask] should equal(true)
      task.asInstanceOf[CommandLineTask].command should equal("ls")
    }

    it("should serialize job") {
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")))
      JSON.write(job) should equal("""{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}""")
    }

    it("should deserialize job") {
      val json = """{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}"""
      val job = JSON.read[Job](json)
      job.name should equal("job")
      job.resources should equal(Set("LINUX"))
      job.tasks should have size (1)
      val task = job.tasks.head
      task.isInstanceOf[CommandLineTask] should equal(true)
      task.asInstanceOf[CommandLineTask].command should equal("ls")
    }

    it("should serialize stage") {
      val job = new Job("job", Set("LINUX"), List(new CommandLineTask("ls")))
      val stage = new Stage("stage", Set(job))
      JSON.write(stage) should equal("""{"name":"stage","jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}]}""")
    }

    it("should deserialize stage") {
      val json = """{"name":"stage", "jobs":[{"name":"job","resources":["LINUX"],"tasks":[{"type":"cmd","command":"ls"}]}]}"""
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
  }
}
