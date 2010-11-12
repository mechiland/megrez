package org.megrez.task

import org.megrez.Task
import java.io.File
import org.megrez.util.ShellCommand

class CommandLineTask(val command : String) extends Task with ShellCommand {
  def execute(workingDir : File)  {
    run(command, workingDir)
  }
  
  def cancel() {
    
  }
}