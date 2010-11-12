package org.megrez.task

import org.megrez.Task
import org.megrez.util.ShellCommand
import java.lang.String
import scala.io.Source
import java.io.{OutputStream, InputStream, File}

class CommandLineTask(val command: String) extends Task with ShellCommand {
  private var process: Process = _

  def execute(workingDir: File):String = {
    process = run(command, workingDir)
    return Source.fromInputStream(process.getInputStream).getLines.mkString(".")
  }

  def cancel() {
    process.destroy
  }
}