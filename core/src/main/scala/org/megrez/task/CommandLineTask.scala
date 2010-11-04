package org.megrez.task

import org.megrez.Task
import org.megrez.runtime.ShellCommand

class CommandLineTask(val command : String) extends Task with ShellCommand {
  
  def execute() = {
    false
  }
  
  def cancel() {
    
  }
}