package org.megrez.task

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import java.io.File

class CommandLineTaskTest extends Spec with ShouldMatchers {
  describe("Command line task") {
    it("should return screen output when executed successfully") {
      val task = new CommandLineTask("echo HELLO")
      val output = task.execute(new File(System.getProperty("user.dir")))
      output should equal("HELLO")
    }

    it("should return notify listener when screen output changes") {
      val task = new CommandLineTask("echo HELLO")
      task
      val output = task.execute(new File(System.getProperty("user.dir")))
      output should equal("HELLO")
    }


    it("should not throw exception if command execution succeed") {
      val commandLineTask = new CommandLineTask("echo")
      try {
        commandLineTask.execute(new File("."))
      }catch {
        case e: Exception => fail
      }
    }

    it("should throw exception if command execution failed") {
      val commandLineTask = new CommandLineTask("unknow-command")
      try {
        commandLineTask.execute(new File("."))
        fail
      } catch {
        case e: Exception =>
      }
    }

  }
}