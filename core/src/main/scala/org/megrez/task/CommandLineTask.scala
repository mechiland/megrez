package org.megrez.task

import org.megrez.Task
import java.io.File
import org.megrez.util.ShellCommand
import io.Source

class CommandLineTask(val command: String) extends Task with ShellCommand {
  private var process: Process = _

  def execute(workingDir: File):String = {
    process = run(commandLine, workingDir)
    return Source.fromInputStream(process.getInputStream).getLines.mkString(".")
  }

  protected def commandLine() = command
  
  def cancel() {
    process.destroy
  }
}