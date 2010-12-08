package org.megrez.util

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez._
import task.{AntTask, CommandLineTask}
import vcs.{Git, Subversion}
class JsonTest extends Spec with ShouldMatchers {
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
      val assignment = JobAssignmentFuture(1, "pipeline", Map((new Subversion("svn_url"), "dest") -> Some(5)), List(new CommandLineTask("ls")))
      JSON.write(assignment) should equal("""{"materials":[{"material":{"type":"svn","url":"svn_url","dest":"dest"},"workset":{"revision":5}}],"pipeline":"pipeline","tasks":[{"type":"cmd","command":"ls"}],"type":"assignment","buildId":1}""")
    }

    it("should serialize job assignment for git") {
      val assignment = JobAssignmentFuture(1, "pipeline", Map((new Git("git_url"), "dest") -> Some("abc")), List(new CommandLineTask("ls")))
      JSON.write(assignment) should equal("""{"materials":[{"material":{"type":"git","url":"git_url","dest":"dest"},"workset":{"commit":"abc"}}],"pipeline":"pipeline","tasks":[{"type":"cmd","command":"ls"}],"type":"assignment","buildId":1}""")
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

    it("should serialize job failed") {
      val message = JobFailed("file not exist")
      JSON.write(message) should equal("""{"type":"jobfailed","reason":"file not exist"}""")
    }

    it("should deserialize job faild") {
      val json = """{"type":"jobfailed","reason":"file not exist"}"""
      JSON.read[AgentMessage](json) match {
        case message: JobFailed => message.reason should be equals "file not exist"
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
