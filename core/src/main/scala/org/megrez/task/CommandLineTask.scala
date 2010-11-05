package org.megrez.task

import org.megrez.Task
import org.megrez.runtime.ShellCommand
import java.io.File

class CommandLineTask(val command : String) extends Task with ShellCommand {  
  def execute(workingDir : File)  {
  }
  
  def cancel() {
    
  }
}